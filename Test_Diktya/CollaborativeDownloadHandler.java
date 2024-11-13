import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class CollaborativeDownloadHandler extends Thread {
    private String message;
    private Peer peer;
    private PrintWriter trackerOut;
    private Map<String, Set<String>> fileSegmentsReceived; // Tracking received segments
    String segmentResponse;

    public CollaborativeDownloadHandler(String message, Peer peer, PrintWriter tracker) {
        this.message = message;
        this.peer = peer;
        this.fileSegmentsReceived = new ConcurrentHashMap<>();
        this.trackerOut = tracker;
    }

    @Override
    public void run() {
        String[] parts = message.split(" ");
        String fileName = parts[1];
        Set<String> receivedSegments = fileSegmentsReceived.computeIfAbsent(fileName, k -> Collections.synchronizedSet(new HashSet<>()));
        int retryLimit = 3; // Maximum number of retries for a request

        try {
            List<Integer> seederPorts = peer.getPortByFilenameSeeders(fileName);

            while (receivedSegments.size() < 10) {
                for (int i = 0; i < seederPorts.size(); i++) {
                    int seederPort = seederPorts.get(i);
                    String segmentRequest = "Request " + fileName + " Port: " + peer.getPort() + " Id: " + peer.getID();

                    boolean success = false;
                    int attempts = 0;

                    while (!success && attempts < retryLimit) {
                        try (Socket peerSocket = new Socket("localhost", seederPort);
                             PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
                             BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()))) {

                            peerOut.println(segmentRequest);
                            peerOut.flush();  // Ensure the request is sent
                            //System.out.println("Segment Request: " + segmentRequest);

                            
                            sleep(500); // set 500ms for next request
                            segmentResponse = peerIn.readLine();

                            if (segmentResponse != null && segmentResponse.startsWith("Segment ")) {
                                String segmentName = segmentResponse.split(" ")[1];

                                // Check if the segment is already received
                                if (!receivedSegments.contains(segmentName)) {
                                    receivedSegments.add(segmentName);
                                    FileReceive fileReceive = new FileReceive(segmentName, seederPort, peer.getID(), peer);
                                    fileReceive.start();
                                    // Simulate receiving the segment
                                    System.out.println("Received segment: " + segmentName + " from port: " + seederPort);
                                    peer.addOrIncrementPortCounter(seederPort);
                                    trackerOut.println("notify success " + seederPort + " " + segmentRequest);
                                }
                                success = true; // Exit retry loop on success
                            } else {
                                System.out.println("Segment response: " + segmentResponse);
                                attempts++;
                            }
                        } catch (SocketTimeoutException e) {
                            attempts++;
                            if (attempts >= retryLimit) {
                                trackerOut.println("notify timeout " + seederPort);
                                System.err.println("Timeout waiting for response from seeder at port: " + seederPort);
                            }
                        } catch (IOException e) {
                            attempts++;
                            if (attempts >= retryLimit) {
                                trackerOut.println("notify error " + seederPort);
                                System.err.println("Error connecting to seeder at port: " + seederPort);
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            System.out.println("All segments received for file: " + fileName);
            String folderString = "shared_folder" + peer.getID();
            assemble(fileName, receivedSegments, folderString, peer.getID());
            peer.changeCollaborativeReceived();
            trackerOut.println("seeder-inform " + "id: " + peer.getID() + " Filename: " + fileName); 
        } catch (Exception e) {
            System.err.println("Error in CollaborativeDownloadHandler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void assemble(String fileName, Set<String> receivedSegments, String sharedFolder, int peerID) {
        try {
            File combinedFile = new File(sharedFolder + File.separator +  fileName.replaceAll("-", ""));
            FileOutputStream dataOutputStream = new FileOutputStream(combinedFile);

            String baseName = fileName.substring(0, fileName.lastIndexOf("."));

            for (int i = 1; i <= 10; i++) {
                String segmentName = baseName + "-" + i +".txt"; // Assuming segment names are like "filename-1", "filename-2", ..., "filename-10"
                //System.out.println(segmentName);
                if (receivedSegments.contains(segmentName)) {
                    File segmentFile = new File(sharedFolder + File.separator + segmentName);
                    FileInputStream fileInputStream = new FileInputStream(segmentFile);
                    
                    byte[] buffer = new byte[4 * 1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        dataOutputStream.write(buffer, 0, bytesRead);
                        dataOutputStream.flush();
                    }
                    fileInputStream.close();
                }
            }

            dataOutputStream.close();

            // Delete individual segment files
            // for (int i = 1; i <= 10; i++) {
            //     String segmentName = fileName + "-" + i;
            //     File segmentFile = new File(sharedFolder + File.separator + segmentName);
            //     segmentFile.delete();
            // }

            System.out.println("Combined file saved: " + combinedFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error assembling segments: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
