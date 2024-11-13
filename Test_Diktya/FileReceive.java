import java.io.*;
import java.net.*;

// Class to handle file reception
public class FileReceive extends Thread {
    private String fileName;
    private int port;
    private int id;
    private Peer peer;

    FileReceive(String filename, int prt, int Id, Peer per) {
        this.fileName = filename;
        this.port = prt;
        this.id = Id;
        this.peer = per;
    }

    @Override
    public void run() {
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        PrintWriter out = null;
        Socket peerSocket = null;
        try {
            peerSocket = new Socket("localhost", port);
            // System.out.println("Socket created: " + peerSocket);
            // System.out.println("Connected to peer...");

            
            out = new PrintWriter(peerSocket.getOutputStream(), true);
            out.println("simpleDownload " + fileName);
            //out.println("fileRequest " + fileName);

            String path = "shared_folder" + id + File.separator + fileName;
            //System.out.println("The path is : " + path );
            fileOutputStream = new FileOutputStream(path);
            inputStream = peerSocket.getInputStream();

            byte[] buffer = new byte[4 * 1024];
            int bytes;
            while ((bytes = inputStream.read(buffer)) != -1) {
                // Check for the end of file marker
                String data = new String(buffer, 0, bytes);
                if (data.equals("END_OF_FILE")) {
                    break;
                }
                fileOutputStream.write(buffer, 0, bytes);
            }
            // System.out.println("File is Received");
            peer.changeReceived(port);

        } catch (Exception e) {
            System.out.println("File is not Received: " + e);
            e.printStackTrace(); // Detailed stack trace
            // peer.addFailures();
        } finally {
            // Close resources in the finally block to ensure they are closed even if an exception occurs
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
                if (peerSocket != null && !peerSocket.isClosed()) {
                    peerSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e);
                e.printStackTrace(); // Detailed stack trace for closing errors
            }
        }
    }
}
