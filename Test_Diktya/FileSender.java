import java.io.*;
import java.net.Socket;

public class FileSender extends Thread {
    private Socket peerSocket;
    private int id;
    private String filename;

    FileSender(Socket perSocket, int ID , String filename) {
        peerSocket = perSocket;
        id = ID;
        this.filename=filename;

    }

    @Override
    public void run() {
        try {
            // BufferedReader in = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
            System.out.println("Sending the File to the Server . Also got filename : " + filename);
            // String message = in.readLine();
            // System.out.println("Mysterious message is : " + message);
            // String[] parts = message.split(" ");
            String path = "shared_folder" + id + File.separator + filename;
            //System.out.println("Path is : " + path);
            // sendFile
            DataOutputStream dataOutputStream = new DataOutputStream(peerSocket.getOutputStream());
            int bytes = 0;
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            // System.out.println(file.getAbsolutePath());

            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }

            // Mark the end of the file transfer
            dataOutputStream.write("END_OF_FILE".getBytes());
            dataOutputStream.flush();

            fileInputStream.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}


