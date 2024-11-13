import java.util.ArrayList;
import java.util.List;

// Custom class to hold file names and IDs
class FileAndPorts {
    private String fileName;
    private int port;

    public FileAndPorts(String fileName, int port) {
        this.fileName = fileName;
        this.port = port;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPort() {
        return port;
    }
    
}
