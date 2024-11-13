import java.net.Socket;

public class User {
    private String username;
    private String password;
    private int id;
    private String ipAddress;
    private int port;
    private int count_downloads;
    private int count_failures;
    private Socket peerSocket;
    private String[] pieces;
    private boolean seeder;

    public User(String username, String password) {
        this(username, password, 0, "", 0, 0, 0, null);
    }

    public User(String username, String password, int id) {
        this(username, password, id, "", 0, 0, 0, null);
    }

    public User(String username, String password, int id, String ipAddress, int port) {
        this(username, password, id, ipAddress, port, 0, 0, null);
    }

    public User(String username, String password, int id, String ipAddress, int port, int count_downloads, int count_failures) {
        this(username, password, id, ipAddress, port, count_downloads, count_failures, null);
    }

    public User(String username, String password, int id, String ipAddress, int port, int count_downloads, int count_failures, Socket peerSocket) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.count_downloads = count_downloads;
        this.count_failures = count_failures;
        this.peerSocket = peerSocket;
    }

    public User(String username, String password, int id, String ipAddress, int port, int count_downloads, int count_failures, Socket peerSocket, String[] pieces, boolean seeder) {
        this(username, password, id, ipAddress, port, count_downloads, count_failures, peerSocket);
        this.pieces = pieces;
        this.seeder = seeder;
    }
    

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void addCountDownloads() {
        count_downloads = count_downloads+1;
    }

    public int getCountDownloads() {
        return count_downloads;
    }

    public void addCountFailures() {
        count_failures = count_failures+1;
    }

    public int getCountFailures() {
        return count_failures;
    }

    public boolean nequals(String n_username, String n_password) {
        return username.equals(n_username) && password.equals(n_password);
    }

    public String getUserInfo() {
        StringBuilder userInfo = new StringBuilder();
        userInfo.append("Username: ").append(username).append("\n");
        userInfo.append("ID: ").append(id).append("\n");
        userInfo.append("IP Address: ").append(ipAddress).append("\n");
        userInfo.append("Port: ").append(port).append("\n");
        userInfo.append("Downloads Count: ").append(count_downloads).append("\n");
        userInfo.append("Failures Count: ").append(count_failures).append("\n");
        userInfo.append("Pieces: ").append(pieces).append("\n");
        userInfo.append("Seeder: ").append(seeder ? "Yes" : "No").append("\n");
        return userInfo.toString();
    }
    
    
    public void setPeerSocket(Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    public Socket getPeerSocket() {
        return peerSocket;
    }
    
    // Additional method to set peerSocket based on ID
    public void setPeerSocketById(int id, Socket peerSocket) {
        if (this.id == id) {
            this.peerSocket = peerSocket;
        }
    }

    public Socket getPeerSocketById(int id) {
        if (this.id == id) {
            return peerSocket;
        } else {
            return null;
        }
    }

    public void setPieces(String[] pieces) {
        this.pieces = pieces;
    }
    
    public String[] getPieces() {
        return pieces;
    }
    
    public void setSeeder(boolean seeder) {
        this.seeder = seeder;
    }
    
    public boolean GetSeeder() {
        return seeder;
    }
    
}


