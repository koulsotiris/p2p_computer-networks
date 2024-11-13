import java.net.Socket;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class FileCollaborativeSender extends Thread {
    private Socket peerSocket;
    private Peer peer ;
    private int id;
    PrintWriter peerOut;
    BufferedReader peerIn;
    private List<String> pendingRequests = new ArrayList<>();
    private static final Object lock = new Object();
    // private Lock lock = new ReentrantLock();
    // private Condition newRequestCondition = lock.newCondition();

    FileCollaborativeSender(Peer perr, Socket perSocket, int ID) {
        peer = perr;
        peerSocket = perSocket;
        id = ID;
    }

    @Override
    public void run() {
        
        try {
            peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
            peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
            // System.out.println("Sending the File to the Server");
            String message = peerIn.readLine();
            // System.out.println(message);

            if (message.startsWith("Request")) {
                //System.out.println("Reading the request");
                handleFileRequest(message);
            }
            // else if (message.startsWith("BestPeerSocket")){
            //     handleBestPeer(message);
            // }
            else if(message.startsWith("simpleDownload")){
                String[] parts = message.split(" ");
                // System.out.println("My program got the message with parts[1] : " + parts[1]);
                // System.out.println("-----Got into collaborative sender simpledownload-----");
                FileSender fileSender = new FileSender(peerSocket,id , parts[1]);
                fileSender.start();
            }

            // System.out.println("Start the thread");
            // Start a separate thread to process pending requests
            Thread requestProcessor = new Thread(this::processPendingRequests);
            requestProcessor.start();



            
           
        } catch (Exception e) {
            System.out.println(e+" peer-id: "+peer.getID());
        }
    }

    //seeder-serve
    private void handleFileRequest(String message) {
        // lock.lock();
        //try {
            pendingRequests.add(message);
            // System.out.println("Get in list");
            // newRequestCondition.signal();
        //} finally {
        //    lock.unlock();
        //}
    }

    private void handleBestPeer(String message) {
        String[] parts = message.split(" ");
        int peerport=Integer.valueOf(parts[1]);
        for (String request : pendingRequests){
            String[] part = request.split(" ");
            if (peerport == Integer.valueOf(part[3])){
                String segment = getRandomSegment(part[1]);
                sendFileSegment(peerport, segment);

            }
            else{
                sendNegativeResponse(Integer.valueOf(part[3]));
            }
        }
    }

    private void processPendingRequests() {
        while (true) {
            List<String> requestsToProcess;
            // System.out.println("Got into thread");
            // lock.lock();
            
                // while (pendingRequests.isEmpty()) {
                //     newRequestCondition.await();
                // }
            synchronized (lock) {
                try {
                    // Wait for requests to accumulate or a certain time to pass
                    lock.wait(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                
                requestsToProcess = new ArrayList<>(pendingRequests);
                pendingRequests.clear();
                
            }
             
            // finally {
            //     lock.unlock();
            // }

            if (requestsToProcess.size() == 1) {
                //System.out.println("entered here = 1");
                processSingleRequest(requestsToProcess.get(0));
                requestsToProcess.clear();
                // Thread.currentThread().interrupt();
            } 
            else if (requestsToProcess.isEmpty()){
                // System.out.println("Waiting for requests");
            }
            else {
                //System.out.println("entered here multiple");
                processMultipleRequests(requestsToProcess);
                requestsToProcess.clear();
                // Thread.currentThread().interrupt();
            }
        }
    }

    private void processSingleRequest(String request) {
        String[] parts = request.split(" ");
        String filename = parts[1];
        String segment = getRandomSegment(filename);
        //System.out.println("Segment is : "+ segment);
        if (segment != null) {
            //System.out.println("Going to send segment");
            sendFileSegment(Integer.valueOf(parts[3]), segment); // parts[3] is the peer's port to send to
            // requestMissingSegments(parts[3], filename);
        } else {
            sendNegativeResponse(Integer.valueOf(parts[3]));
        }
    }


    //Select
    private void processMultipleRequests(List<String> requests) {
        Random rand = new Random();
        double p = rand.nextDouble();

        if (p <= 0.2) {
            processSingleRequest(requests.get(rand.nextInt(requests.size())));
            System.out.println("Selected option : 1");
        } else if (p <= 0.6) {
            processBestPeerRequest(requests);
            System.out.println("Selected option : 2");
        } else {
            processMostSegmentsPeerRequest(requests);
            System.out.println("Selected option : 3");
        }
    }

     private void processBestPeerRequest(List<String> requests) {
        
        peer.SetList(requests);
        peer.changeFlRequests();
        handleBestPeer(peer.getBestPeer());

    }

    private void processMostSegmentsPeerRequest(List<String> requests) { 
        if (peer.isPortCountersEmpty()) {
            processSingleRequest(requests.get(new Random().nextInt(requests.size())));
        } else {
            int bestPort = peer.findBestPort();
            String filename = findFilenameForPort(requests, bestPort);
            String segment = getRandomSegment(filename);
            if (segment != null) {
                sendFileSegment(bestPort, segment);}
            else{
                sendNegativeResponse(bestPort);
            }
            
        }
    }

    private void sendNegativeResponse(int peerPort) {
        //System.out.println("Going to send negative ");
        peerOut.println("Cannot Serve Port: " + peer.getPort());
        //System.out.println("Sent negative ");
    }

    private String getRandomSegment(String filename) {
        String sharedDirectoryPath = "shared_folder" + peer.getID() + File.separator;
        //System.out.println("Shared Directory: "+sharedDirectoryPath);
        File directory = new File(sharedDirectoryPath);
        File[] files = directory.listFiles();
        ArrayList<String> segments = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.getName().contains("-")) {
                    segments.add(file.getName());
                }
            }
        }

        if (segments.isEmpty()) {
            return null; // No segments found
        }

        Random random = new Random();
        int randomIndex = random.nextInt(segments.size());
        return segments.get(randomIndex);
    }

    private String findFilenameForPort(List<String> requests, int port) {
        for (String request : requests) {
            String[] parts = request.split(" ");
            if (Integer.parseInt(parts[3]) == port) {
                return parts[1]; // Assuming parts[1] contains the filename
            }
        }
        return null; // Return null if no matching port is found
    }

    private void sendFileSegment(int peerPort, String segment) {
        
        peerOut.println("Segment " + segment);
        
    }



}


