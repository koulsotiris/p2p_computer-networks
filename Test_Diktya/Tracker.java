import java.io.*;
import java.net.*;
import java.util.*;

public class Tracker {
    //tha prepei na menoun kapoies static 
    private static FileTracker filetracker = new FileTracker();
    private static FileTracker fileseedertracker = new FileTracker();
    private static List<User> registeredUsers = new ArrayList<>();
    // private static List<Socket> peerSockets = new ArrayList<>();
    //private static List<String> tokenId = new ArrayList<>();
    static List<String> activePeers = new ArrayList<>();
    // private static int trackerPort;
    private static int id = 1;
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Tracker started on port 8080...");

            while (true) {
                Socket peerSocket = serverSocket.accept();
                // peerSockets.add(peerSocket);
                System.out.println("Peer connected: " + peerSocket);

                PeerHandler peerHandler = new PeerHandler(peerSocket);
                peerHandler.start();
                // peerSocket.getLocalAddress().toString()
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // //method to get the peerSockets list
    // public static List<Socket> getPeerSockets() {
    //     return peerSockets;
    // }
    
    // public static int getTrackerPort() {
    //     return trackerPort;
    // }


    public static boolean isUsernameRegistered(String username) {
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
    //lock sychronized
    public static void registerUser(String username, String password) {
        registeredUsers.add(new User(username, password));
        System.out.println("Registered user: " + username);
    }

    public static User findUserById(int id) {
        for (User user : registeredUsers) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null; // Return null if user with specified ID is not found
    }
    public static User findUserByPort(int port) {
        for (User user : registeredUsers) {
            if (user.getPort() == port) {
                return user;
            }
        }
        return null; // Return null if user with specified ID is not found
    }

    public static Integer loginUser(String username, String password, Socket socket){
        boolean f = false;
        //System.out.println("Credentials to check " + username +" "+ password);
        for (User user : registeredUsers) {
            if (user.nequals(username,password)) {
                f = true;
                user.setId(id);
                user.setPeerSocket(socket);
            }
        }
        if(f){
            //String idUser = username+" "+Integer.toString(id);
            //tokenId.add(idUser);
            System.out.println("id is : " + id);
            return id;
        }
        else{
            System.out.println("Not found ");
            return -1;
        }
    }
    
    //lock 
    public static void IncreaseID(){
        id++;
    }

    public static boolean logoutUser(int id){
        for (User user : registeredUsers) {
            if (user.getId()== id ) {
                user.setId(0);
                user.setPort(0);
                user.setIpAddress("");
                user.setPeerSocket(null);
                filetracker.removePeerFromAllFiles(id);
                return true;
            }
        }
        return false;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // public static boolean checkActive(){
    //       for (int ids : trackedids) {
    //         Socket peerSocket = Tracker.findUserById(ids).getPeerSocketById(ids); //finding peersocket that is saved on user with id = ids ;

    //         PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true); 
    //         peerOut.println("checkActive");
    //         // Read the response from the peer
    //         BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
    //         String response = peerIn.readLine();
    //         System.out.println(response);
    //         if (response != null && response.equals("yes")) {
    //             // Peer is active
    //             System.out.println("Peer with ID " + ids + " is active.");
    //         } else {
    //             // Peer is not active or did not respond with "yes"
    //             System.out.println("Peer with ID " + ids + " is not active.");
    //         }
    //         userInfoBuilder.append(Tracker.findUserById(ids).getUserInfo());
    //     } 
    // }
    ////////////////////////////////////////////////////////////////////////////////////////////////////// 
    public static void GiveIP_Port (int id, String ip , int port){
        for (User user : registeredUsers) {
            if (user.getId()==id) {
                user.setId(id);
                user.setIpAddress(ip);
                user.setPort(port);
            }
        }
    }

    //Operation reply_details 

    //This method will return a list with all the ids that track the filename 
    public static List<Integer> reply_details (String filename , int id ){
        //Firstly check whether or not a peer except our peer tracks the filename
        if (filetracker.isFileTracked(filename ,id)){
            System.out.println(filetracker.getTrackedPeersForFile(filename , id ));
            return filetracker.getTrackedPeersForFile(filename , id );
        }
        else {
            //If no peer except our peer tracks then we return an empty list
            return new ArrayList<>();
        }
        
    }

    //This method will return a list with all the ids that track the filename 
    public static List<Integer> reply_details_seeder (String filename , int id ){
        //Firstly check whether or not a peer except our peer tracks the filename
        if (fileseedertracker.isFileTracked(filename ,id)){
            System.out.println(fileseedertracker.getTrackedPeersForFile(filename , id ));
            return fileseedertracker.getTrackedPeersForFile(filename , id );
        }
        else {
            //If no peer except our peer tracks then we return an empty list
            return new ArrayList<>();
        }
        
    }



    public static void sendCheckActiveMessage(Socket peerSocket) {
        try {
            PrintWriter out = new PrintWriter(peerSocket.getOutputStream(), true);
            out.println("checkActive");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void handleAddFile(Socket peerSocket, String message) {
        // Handle adding file logic
        System.out.println("Handling add file for peer: " + message);
    }

    public static void handleSearch(Socket peerSocket, String message) {
        // Handle search logic
        System.out.println("Handling search for peer: " + message);
    }

    public static void handleDownload(Socket peerSocket, String message) {
        // Handle download logic
        System.out.println("Handling download for peer: " + message);
    }

    //lock 
    public static void FileIdMatch(String[] parts){
        for (int i = 2 ; i < parts.length; i++){
            filetracker.addFileIDMatch(parts[i],Integer.parseInt(parts[1]));
        }
        // System.err.println(filetracker.generateMappingString());
    }
    
    public static void AddFileIdMatch(int id, String filename){
        filetracker.addFileIDMatch(filename,id);
    }

    

    public static String reply_list() {
        
        StringBuilder ListOperationMessage = new StringBuilder("Available files in p2p: ");

        try (BufferedReader br = new BufferedReader(new FileReader("fileDownloadList.txt"))) {
            String fileName;
            while ((fileName = br.readLine()) != null) {
                ListOperationMessage.append(fileName).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle or log the exception appropriately
        }

        return ListOperationMessage.toString();
    }

    public static void GivePiecesSeed(int id, String[] parts) {
        for (User user : registeredUsers) {
            if (user.getId()==id) {
                // Check if there are any pieces beyond index 3
                if (parts.length > 3) {
                    boolean isSeeder = true;
                    // Start checking pieces from index 4
                    for (int i = 4; i < parts.length; i++) {
                        // Check if the piece is empty or null
                        if (parts[i] == null || parts[i].isEmpty()) {
                            isSeeder = false;
                            break; // If any piece is empty, peer is not a seeder
                        }
                    }
                    // Set pieces and seeder status for the user
                    user.setPieces(Arrays.copyOfRange(parts, 4, parts.length));
                    user.setSeeder(isSeeder);
                } else {
                    // If no pieces beyond index 3, peer has no pieces
                    user.setPieces(new String[0]); // Empty array for no pieces
                    user.setSeeder(false); // Not a seeder
                }
            }
        }
    }
    //It may does not run correctly
    public static void AddPiecesToUser(int id, String[] newPieces) {
        for (User user : registeredUsers) {
            if (user.getId() == id) {
                List<String> existingPieces = new ArrayList<>(Arrays.asList(user.getPieces()));
                for (String piece : newPieces) {
                    if (piece != null && !piece.isEmpty()) {
                        existingPieces.add(piece);
                    }
                }
                user.setPieces(existingPieces.toArray(new String[0]));
                
                // Re-check if the user is a seeder
                boolean isSeeder = true;
                for (String piece : user.getPieces()) {
                    if (piece == null || piece.isEmpty()) {
                        isSeeder = false;
                        break;
                    }
                }
                user.setSeeder(isSeeder);
            }
        }
    }

    //It may does not run correctly
    public static String[] generateFileNames(String baseFileName) {
        String baseNameWithoutExtension = baseFileName.substring(0, baseFileName.lastIndexOf('.'));
        String extension = baseFileName.substring(baseFileName.lastIndexOf('.'));
        String[] fileNames = new String[11]; // 1 for the original file + 10 generated files
        
        // Add the original file as the first element
        fileNames[0] = baseFileName;
        
        // Generate additional files
        for (int i = 1; i <= 10; i++) {
            fileNames[i] = baseNameWithoutExtension + "-" + i + extension;
        }
        
        return fileNames;
    }

    public static boolean isSeeder(int id){
        boolean f = false ;
        for (User user : registeredUsers) {
            if (user.getId()==id) {
                // Check if there are any pieces beyond index 3
                if (user.GetSeeder()){
                    f=true ;
                }
            }  
        }
        return f;
    }



    public static Set<String> extractFileNames(String[] parts) {
        Set<String> fileNames = new HashSet<>();
        for (int i = 4; i < parts.length; i++) {
            String piece = parts[i];
            if (piece != null && !piece.isEmpty()) {
                // Split the piece by "-" and get the first part
                String[] partsOfPiece = piece.split("-");
                if (partsOfPiece.length > 0) {
                    fileNames.add(partsOfPiece[0]);
                }
            }
        }
        return fileNames;
    }

    public static void AddToSeederList(int id, String filename) {
        fileseedertracker.addFileIDMatch(filename , id  );
    }

    public static void GenSeederList() {
        System.err.println(fileseedertracker.generateMappingString());
    }


    public static List<Integer> extractNumbers(String request) {
        List<Integer> numbers = new ArrayList<>();
        String[] parts = request.split(" ");
        for (String part : parts) {
            try {
                int number = Integer.parseInt(part);
                numbers.add(number);
            } catch (NumberFormatException e) {
                // Ignore parts that are not numbers
            }
        }
        return numbers;
    }
}
