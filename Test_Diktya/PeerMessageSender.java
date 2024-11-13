import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class PeerMessageSender extends Thread {
    private BufferedReader trackerIn;
    private PrintWriter trackerOut;
    private Peer peer;
    // private Map<String, List<String>> fileSegmentsReceived; // Tracking received segments

    public PeerMessageSender(BufferedReader trackerIn, PrintWriter trackerOut, Peer peer) {
        this.trackerIn = trackerIn;
        this.trackerOut = trackerOut;
        this.peer = peer;
        // this.fileSegmentsReceived = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        try {
            // Thread to get user's input to perform operations 
            Scanner scanner = new Scanner(System.in);
            Thread inputThread = new Thread(() -> {
                while (true) {
                    if (scanner.hasNextLine()) {
                        String messageToSend = scanner.nextLine();
                        if (messageToSend.startsWith("simpleDownload")) {
                            handleSimpleDownload(messageToSend);
                        } else if (messageToSend.startsWith("collaborativeDownload")) {
                            handleCollaborativeDonwload(messageToSend);
                        }
                        else if (peer.checkFlRequests()){
                            List<String> requests = peer.GetList();
                            trackerOut.println("Find Best "+ peer.handleToSendRequests(requests));
                            peer.changeFlRequests();
                        } 
                        else {
                            trackerOut.println(messageToSend);
                        }
                    }
                }
            });
            inputThread.start();

            while (true) {
                Thread.sleep(100);
                if (peer.getActive()) {
                    trackerOut.println("Sending Check Active response from peer with id " + peer.getID());
                    trackerOut.println("yes");
                    peer.changeActive();
                }
                if (peer.LoggedIn() && peer.InformDone()) {
                    informTracker();
                    Partition p = new Partition(peer);
                    p.start();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }catch(Exception e){
            System.out.println("An error occured.");
        }
    }

    void handleCollaborativeDonwload(String message){
        CollaborativeDownloadHandler dwnl = new CollaborativeDownloadHandler(message,peer,trackerOut);
        dwnl.start();
        try{
            dwnl.join();
        }catch(Exception e){
            System.out.println(e);
        }
        if(peer.checkCollaborativeReceived()){

            peer.changeCollaborativeReceived();
        }
    }

    private void handleSimpleDownload(String message) {
        String[] parts = message.split(" ");
        int port = peer.getPortByFilename(parts[1]);
        if (port != -1) {
            FileReceive fileReceive = new FileReceive(parts[1], port, peer.getID(), peer);
            fileReceive.start();
            try {
                fileReceive.join();
            } catch (Exception e) {
                System.out.println(e);
            }
            if (peer.checkReceived()) {
                trackerOut.println("notify success " + peer.receivedPort());
                peer.changeReceived(0);
            } else {
                trackerOut.println("notify unsuccessful " + peer.receivedPort());
            }
        } else {
            System.out.println("File is not analyzed. Do details operation first.");
        }
    }


    private void informTracker() throws UnknownHostException {
        File folder = new File("shared_folder" + peer.getID());
        File[] listOfFiles = folder.listFiles();

        StringBuilder informMessage = new StringBuilder("inform ");
        informMessage.append(peer.getID()).append(" ").append(peer.getIP()).append(" ").append(peer.getPort()).append(" ");

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    peer.AddlistOfFiles(file.getName());
                    informMessage.append(file.getName()).append(" ");
                }
            }
        }
        trackerOut.println(informMessage.toString().trim());
        peer.ChangeFL();
    }
}

