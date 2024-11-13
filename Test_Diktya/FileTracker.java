import java.util.*;

public class FileTracker {
    private HashMap<String, List<Integer>> fileToPeersMap;

    public FileTracker() {
        fileToPeersMap = new HashMap<>();
    }

    // Method to add a file and the peer ID who has it
    public void addFile(String fileName, int peerID) {
        if (!fileToPeersMap.containsKey(fileName)) {
            fileToPeersMap.put(fileName, new ArrayList<>());
        }
        fileToPeersMap.get(fileName).add(peerID);
    }

    public void addFileIDMatch( String fileName,int peerID) {
        // Check if the file already exists in the map
        if (!fileToPeersMap.containsKey(fileName)) {
            // If the file doesn't exist, create a new list and put it in the map
            fileToPeersMap.put(fileName, new ArrayList<>());
        }
        
        // Add the peerID to the list of peers for the specified file
        List<Integer> peers = fileToPeersMap.get(fileName);
        peers.add(peerID);
    }

    // Method to get the list of peer IDs for a given file
    public List<Integer> getPeersForFile(String fileName) {
        return fileToPeersMap.getOrDefault(fileName, new ArrayList<>());
    }

    // Method to remove a peer ID from the list of peers for each file
    public void removePeerFromAllFiles(int peerID) {
        for (List<Integer> peers : fileToPeersMap.values()) {
            peers.remove((Integer) peerID); // Remove the Integer object to ensure we're removing by value
        }
    }
    
    //Method that give us if file is tracked by other peer
    public boolean isFileTracked(String fileName, int excludedPeerID) {
        if (fileToPeersMap.containsKey(fileName)) {
            List<Integer> peers = fileToPeersMap.get(fileName);
            return !peers.isEmpty() && peers.stream().anyMatch(id -> id != excludedPeerID);
        }
        return false;
    }

    //Method that does almost the same as the above but returns ids that track the file given 
    public List<Integer> getTrackedPeersForFile(String fileName, int excludedPeerID) {
        List<Integer> trackedPeers = new ArrayList<>();
        if (fileToPeersMap.containsKey(fileName)) {
            List<Integer> peers = fileToPeersMap.get(fileName);
            for (int peerID : peers) {
                if (peerID != excludedPeerID) {
                    trackedPeers.add(peerID);
                }
            }
        }
        return trackedPeers;
    }

    // Method to generate a string representation of the file-to-peers mapping
    public String generateMappingString() {
        StringBuilder sb = new StringBuilder();
        sb.append("File-to-Peers Mapping:\n");
        for (Map.Entry<String, List<Integer>> entry : fileToPeersMap.entrySet()) {
            sb.append("File: ").append(entry.getKey()).append(", Peers: ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
    
}