import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

class PeerMessageReceiver extends Thread {
    private BufferedReader trackerIn;
    private Peer peer;
    //private List<String> pendingRequests = new ArrayList<>();
    //private Lock lock = new ReentrantLock();
    //private Condition newRequestCondition = lock.newCondition();

    public PeerMessageReceiver(BufferedReader trackerIn, Peer peer) {
        this.trackerIn = trackerIn;
        this.peer = peer;
    }

    @Override
    public void run() {
        // Start a separate thread to process pending requests
        //Thread requestProcessor = new Thread(this::processPendingRequests);
        //requestProcessor.start();

        try {
            String message;
            while ((message = trackerIn.readLine()) != null) {
                System.out.println("Received from tracker: " + message);
                if (message.startsWith("id")) {
                    handleIdMessage(message);
                } else if (message.equals("Logout")) {
                    peer.Logout();
                    peer.ChangeFL();
                } else if (message.startsWith("files:")) {
                    handleFilesMessage(message);
                } else if (message.equals("checkActive")) {
                    peer.changeActive();
                    System.out.println("I send that i am active");
                } else if (message.startsWith("File")) {
                    String[] parts = message.split(" ");
                    if(parts[3].matches("\\d+")){
                        peer.removeByFilename(parts[1]); // remove every previous entry with the filename, because only one port can be the best
                        peer.AddToFileAndPorts(parts[1], Integer.valueOf(parts[3]));
                    }
                }else if(message.startsWith("Seeder")) {
                    handleSeederList(message);
                } 
                // else if (message.startsWith("Request")) {
                //     handleFileRequest(message);
                // }
                // else if (message.startsWith("BestPeerSocket")){
                //     handleBestPeer(message);
                // }
                else if (message.startsWith("BestPeerSocket")){
                    peer.setBestPeer(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // private void handleBestPeer(String message) {
    //     String[] parts = message.split(" ");
    //     int peerport=Integer.valueOf(parts[1]);
    //     for (String request : pendingRequests){
    //         String[] part = request.split(" ");
    //         if (peerport == Integer.valueOf(part[3])){
    //             String segment = getRandomSegment(part[1]);
    //             sendFileSegment(peerport, segment);

    //         }
    //         else{
    //             sendNegativeResponse(Integer.valueOf(part[3]));
    //         }
    //     }
    // }

    private void handleSeederList(String message) {
        
        String[] parts = message.split(" ");
        for (String part : parts){
            System.out.println(part);
        } 
        System.out.println(message);
        peer.AddToFileAndPortSeeders(parts[3], Integer.valueOf(parts[1]));
    }

    private void handleIdMessage(String message) {
        String[] parts = message.split(" ");
        if (parts.length == 2) {
            int id = Integer.parseInt(parts[1]);
            peer.SetID(id);
            createSharedFolder(id);
        }
    }

    private void handleFilesMessage(String message) {
        String[] parts = message.split(":");
        if (parts.length == 2) {
            String filesString = parts[1];
            String[] fileNames = filesString.split(",");
            processFileNames(fileNames);
        }
    }

    private void createSharedFolder(int id) {
        File sharedFolder = new File("shared_folder" + id);
        if (!sharedFolder.exists()) {
            sharedFolder.mkdirs();
        }
    }

    private void processFileNames(String[] fileNames) {
        for (String fileName : fileNames) {
            System.out.println("Available file: " + fileName);
        }
    }

    //seeder-serve
    // private void handleFileRequest(String message) {
    //     // lock.lock();
    //     //try {
    //         pendingRequests.add(message);
    //         newRequestCondition.signal();
    //     //} finally {
    //     //    lock.unlock();
    //     //}
    // }

    // private void processPendingRequests() {
    //     while (true) {
    //         List<String> requestsToProcess;
    //         lock.lock();
    //         try {
    //             while (pendingRequests.isEmpty()) {
    //                 newRequestCondition.await();
    //             }
    //             Thread.sleep(200); // Wait for 200 milliseconds
    //             requestsToProcess = new ArrayList<>(pendingRequests);
    //             pendingRequests.clear();
    //         } catch (InterruptedException e) {
    //             Thread.currentThread().interrupt();
    //             return;
    //         } finally {
    //             lock.unlock();
    //         }
    //         if (requestsToProcess.size() == 1) {
    //             processSingleRequest(requestsToProcess.get(0));
    //         } else {
    //             processMultipleRequests(requestsToProcess);
    //         }
    //     }
    // }

    // private void processSingleRequest(String request) {
    //     String[] parts = request.split(" ");
    //     String filename = parts[1];
    //     String segment = getRandomSegment(filename);

    //     if (segment != null) {
    //         sendFileSegment(Integer.valueOf(parts[3]), segment); // parts[3] is the peer's port to send to
    //         // requestMissingSegments(parts[3], filename);
    //     } else {
    //         sendNegativeResponse(Integer.valueOf(parts[3]));
    //     }
    // }


    // //Select
    // private void processMultipleRequests(List<String> requests) {
    //     Random rand = new Random();
    //     double p = rand.nextDouble();

    //     if (p <= 0.2) {
    //         processSingleRequest(requests.get(rand.nextInt(requests.size())));
    //     } else if (p <= 0.6) {
    //         processBestPeerRequest(requests);
    //     } else {
    //         processMostSegmentsPeerRequest(requests);
    //     }
    // }

    // private void processBestPeerRequest(List<String> requests) {
        
    //     peer.SetList(requests);
    //     peer.changeFlRequests();

    // }

    // private void processMostSegmentsPeerRequest(List<String> requests) { 
    //     if (peer.isPortCountersEmpty()) {
    //         processSingleRequest(requests.get(new Random().nextInt(requests.size())));
    //     } else {
    //         int bestPort = peer.findBestPort();
    //         String filename = findFilenameForPort(requests, bestPort);
    //         String segment = getRandomSegment(filename);
    //         if (segment != null) {
    //             sendFileSegment(bestPort, segment);}
    //         else{
    //             sendNegativeResponse(bestPort);
    //         }
            
    //     }
    // }

    // private void sendFileSegment(int peerPort, String segment) {
    //     try(Socket peerSocket = new Socket("localhost", peerPort);
    //     PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true)){
    //         peerOut.println("Segment " + segment);
    //     }
    //     catch (SocketTimeoutException e) {
    //         // Handle timeout (Seeder didn't respond in time)
    //         System.err.println("Timeout waiting for response from seeder at port: " + peerPort);
    //     } catch (IOException e) {
    //         System.err.println("Error connecting to seeder at port: " + peerPort);
    //         e.printStackTrace();
    //     }
    // }

    // private void requestMissingSegments(String peerId, String filename) {
    //     // Implement the logic to request missing file segments from the specified peerId
    // }

    // private void sendFilePart(String peerId, String fileName) {
    //     // Implement logic to send a part of the file to the specified peer
    //     System.out.println("Sending part of file " + fileName + " to peer " + peerId); 
    //     // Add your file sending logic here
    // }

    // private void sendNegativeResponse(int peerPort) {
    //     try (Socket peerSocket = new Socket("localhost", peerPort);
    //         PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
    //         BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()))) {

    //         peerOut.println("Cannot Serve Port: " + peer.getPort());
    //         } catch (SocketTimeoutException e) {
    //             // Handle timeout (Seeder didn't respond in time)
    //             System.err.println("Timeout waiting for response from seeder at port: " + peerPort);
    //         } catch (IOException e) {
    //             System.err.println("Error connecting to seeder at port: " + peerPort);
    //             e.printStackTrace();
    //         }
                
    // }

    // private String getRandomSegment(String filename) {
    //     String sharedDirectoryPath = "shared_folder" + peer.getID();
    //     File directory = new File(sharedDirectoryPath);
    //     File[] files = directory.listFiles();
    //     ArrayList<String> segments = new ArrayList<>();

    //     if (files != null) {
    //         for (File file : files) {
    //             if (file.getName().startsWith(filename + "-")) {
    //                 segments.add(file.getName());
    //             }
    //         }
    //     }

    //     if (segments.isEmpty()) {
    //         return null; // No segments found
    //     }

    //     Random random = new Random();
    //     int randomIndex = random.nextInt(segments.size());
    //     return segments.get(randomIndex);
    // }

    // private String findFilenameForPort(List<String> requests, int port) {
    //     for (String request : requests) {
    //         String[] parts = request.split(" ");
    //         if (Integer.parseInt(parts[3]) == port) {
    //             return parts[1]; // Assuming parts[1] contains the filename
    //         }
    //     }
    //     return null; // Return null if no matching port is found
    // }
}


