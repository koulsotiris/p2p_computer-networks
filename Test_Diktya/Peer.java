import java.io.*;
import java.net.*;
import java.util.*;


class Peer {
    
    //id number for user
    static int id = 0 ;
    private static boolean flFile= false;
    boolean fl = true;
    boolean active = false ;
    private boolean flReceived, flCollaborativeReceived, flRequests = false;
    private int receivedPort ;
    private String listOfFiles;
    // Generate a random port number within a specified range
    static int minPort = 8000;
    static int maxPort = 9000;
    static int port = new Random().nextInt(maxPort - minPort + 1) + minPort;
    //List that holds files and best port after details operation 
    private static List<FileAndPorts> fileandports = new ArrayList<>();
    //List that holds files and other seeders of the files the peer wants 
    private static List<FileAndPorts> fileandports_seeders = new ArrayList<>();
    //List to help me find the peer that served this peer more 
    private List<PortCounter> portCounters = new ArrayList<>();
    //List with the requests in order to send them to tracker and return the best 
    List<String> requests ;
    private String bestPeer ;

    public static void main(String[] args) {

        Peer peer = new Peer(); // Create an instance of Peer
        try {
            
            // Partition p = new Partition(peer);
            // p.start();

            
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Peer started on port " + port + "...");
            
            //In a new peer initialization a connection with tracker must first be established
            Socket trackerSocket = new Socket("localhost", 8080);
            System.out.println("Connected to tracker...");
            BufferedReader trackerIn = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream()));
            PrintWriter trackerOut = new PrintWriter(trackerSocket.getOutputStream(), true);
            //Message Sender for Peer  // passed the peer instance in order to edit the same peer 
            PeerMessageSender peerMessageSender = new PeerMessageSender(trackerIn,trackerOut,peer);
            peerMessageSender.start();
            //Message Receiver for Peer   // passed the peer instance in order to edit the same peer 
            PeerMessageReceiver peerMessageReceiver = new PeerMessageReceiver(trackerIn,peer);
            peerMessageReceiver.start();

            while (true) {
                Socket peerSocket = serverSocket.accept();
                System.out.println("Peer connected: " + peerSocket);

                FileCollaborativeSender fileCollaborativeSender = new FileCollaborativeSender(peer,peerSocket,id);
                fileCollaborativeSender.start();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SetList(List<String> requestlist){
        requests = requestlist;
    }

    public List<String> GetList(){
        return requests;
    }
    
    public void setBestPeer(String bstpeer){
        bestPeer = bstpeer;
    }

    public String getBestPeer(){
        return bestPeer;
    }

    public void SetID(int id){
        this.id= id;
    }

    public int getID(){
        return id ;
    }

    public void Logout(){
        id = 0;
    }

    public void changeFlRequests(){
        flRequests = !flRequests;
    }

    public boolean checkFlRequests(){
        return flRequests;
    }

    public void changeCollaborativeReceived(){
        flCollaborativeReceived = ! flCollaborativeReceived;
    }

    public boolean checkCollaborativeReceived(){
        return flCollaborativeReceived;
    }

    public void changeReceived (int prt){
        receivedPort = prt;
        flReceived = ! flReceived;
    }
    public int receivedPort(){
        return receivedPort;
    }
    public boolean checkReceived(){
        return flReceived;
    }
    
    //Those functions are for Inform , ChangeFl is used in Logout too to reinitialize fl .
    public boolean LoggedIn(){
        if (id!=0){
            return true ;}
        else{
            return false;
        }
    }
    public String listOfFiles(){
        return listOfFiles;
    }
    public void AddlistOfFiles(String fileName){
        listOfFiles = listOfFiles + fileName + " ";
    }
    public void ChangeFL(){
        if (fl){
            fl=false;}
        else{
            fl=true;
        }
    }
    public void changeNewFile(){
        flFile = !flFile;
    }
    public boolean checkNewFile(){
        return flFile;
    }
    public boolean InformDone(){
        if (fl){
            return true;
        }
        else{return false;}
    }

    public String getIP() throws UnknownHostException{
        return InetAddress.getLocalHost().getHostAddress();
    }

    public int getPort(){
        return port;
    }

    public void changeActive(){
        active = !active ;
    }

    public boolean getActive(){
        return active ;
    }

    public void AddToFileAndPorts(String filename , int port ){
        fileandports.add(new FileAndPorts(filename, port));
    }

    public int getPortByFilename(String filename) {
        for (FileAndPorts entry : fileandports) {
            if (entry.getFileName().equals(filename)) {
                return entry.getPort();
            }
        }
        // If filename not found, return -1 or throw an exception as needed
        return -1;
    }

    public void AddToFileAndPortSeeders(String filename , int port ){
        fileandports_seeders.add(new FileAndPorts(filename, port));
    }

    public void removeByFilename(String filename) {
        Iterator<FileAndPorts> iterator = fileandports.iterator();
        while (iterator.hasNext()) {
            FileAndPorts entry = iterator.next();
            if (entry.getFileName().equals(filename)) {
                iterator.remove();
            }
        }
    }

    public List<Integer> getPortByFilenameSeeders(String filename) {
        List<Integer> ports = new ArrayList<>();
        for (FileAndPorts entry : fileandports_seeders) {
            if (entry.getFileName().equals(filename)) {
                ports.add(entry.getPort());
            }
        }
        // If no ports found, you can return an empty list or throw an exception
        return ports;
    }

     // Method to add a new port counter if it doesn't exist or increment the counter if it does
     public void addOrIncrementPortCounter(int port) {
        boolean found = false;
        for (PortCounter pc : portCounters) {
            if (pc.getPort() == port) {
                pc.incrementCounter();
                found = true;
                break;
            }
        }
        if (!found) {
            portCounters.add(new PortCounter(port, 1));
        }
    }

    public void addPortCounter(int port) {
        portCounters.add(new PortCounter(port, 0));
    }

    public void incrementPortCounter(int port) {
        for (PortCounter pc : portCounters) {
            if (pc.getPort() == port) {
                pc.incrementCounter();
                return;
            }
        }
        // If port not found, add a new PortCounter object
        portCounters.add(new PortCounter(port, 1));
    }

    public List<PortCounter> getPortCounters() {
        return portCounters;
    }

    public int findBestPort() {
        if (portCounters == null || portCounters.isEmpty()) {
            throw new IllegalStateException("No ports available.");
        }

        PortCounter bestPort = portCounters.get(0);

        for (PortCounter portCounter : portCounters) {
            if (portCounter.getCounter() > bestPort.getCounter()) {
                bestPort = portCounter;
            }
        }

        return bestPort.getPort();
    }

    public boolean isPortCountersEmpty() {
        return portCounters == null || portCounters.isEmpty();
    }

    public String handleToSendRequests(List<String> request){
        StringBuilder idsBuilder = new StringBuilder();
        for (String req : request ){
            String[] parts = req.split(" "); //parts[1]=filename , parts[3]=port , parts[5]=id
            String id = parts[5];
            idsBuilder.append(id).append(" ");
        }
        return idsBuilder.toString();
    }
}
