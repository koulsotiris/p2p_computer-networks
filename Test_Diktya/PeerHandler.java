import java.io.*;
import java.net.*;
import java.util.*;

//--------Available actions-------//
//register username password
//login username password
//list
//details id file 
//collaborativeDownload file 
//simpleDownload file
//logout id 
//-----------------------------//


class PeerHandler extends Thread {
    private Socket socket;
    private static List<String> logginedUsers = new ArrayList<>();
    int id;
    public PeerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                try{
                System.out.println("Received from peer: " + message);

                if (message.equals("checkActive")) {
                    Tracker.activePeers.add(socket.getInetAddress().toString());
                    System.out.println("Peer is active: " + socket.getInetAddress());
                } else if (message.startsWith("register")) {
                    String[] parts = message.split(" ");
                    if (parts.length == 3) {
                        String username = parts[1];
                        String password = parts[2];
                        if (!Tracker.isUsernameRegistered(username)) {
                            Tracker.registerUser(username, password);
                            out.println("Registration successful.");
                        } else {
                            out.println("Username already exists.");
                        }
                    }
                    else {out.println("Wrong Message");}
                } else if (message.startsWith("login")) {
                    String[] parts = message.split(" ");
                    if (parts.length == 3) {
                        
                        String username = parts[1];
                        String password = parts[2];
                        if(logginedUsers.contains(username)){
                            out.println("User "+username+" has already logged in");
                        }else{
                            int id = Tracker.loginUser(username,password,socket);
                            if (id == -1){
                                out.println("Wrong credentials!");
                            }
                            else{
                                Tracker.IncreaseID();
                                out.println("id: "+id);
                                logginedUsers.add(username);
                            }
                        }
                    }
                    else {out.println("Wrong Message");}
                } else if (message.startsWith("logout")) {
                    String[] parts = message.split(" ");
                    if (parts.length == 2) {
                        if(Tracker.findUserById(Integer.valueOf(parts[1])).getId() == Integer.valueOf(parts[1])){
                            logginedUsers.remove(logginedUsers.indexOf(Tracker.findUserById(Integer.valueOf(parts[1])).getUsername()));
                        }
                        if(Tracker.logoutUser(Integer.valueOf(parts[1]))){
                            out.println("Logout successful");                            
                        }
                        else{
                            out.println("Incorrect Id");
                        }
                    }
                    else {out.println("Wrong Message");}
                } else if (message.startsWith("inform")) { // parts[1] = id , part[2] = ip , part[3] = port , parts[4] <= txt files
                    String[] parts = message.split(" ");
                    Tracker.GiveIP_Port(Integer.valueOf(parts[1]),parts[2],Integer.valueOf(parts[3]));
                    Tracker.FileIdMatch(parts);
                    //Edw tha orisoume to pieces kai to seeder-bit 
                    Tracker.GivePiecesSeed(Integer.valueOf(parts[1]),parts);
                    if (Tracker.isSeeder(Integer.valueOf(parts[1]))){
                        // Call the extractFileNames method to get the unique file names
                        Set<String> fileNames = Tracker.extractFileNames(parts);
                        System.out.println("Peer is a seeder for files : ");
                        // Print or use the unique file names
                        for (String fileName : fileNames) {
                            //System.out.println(fileName);
                            Tracker.AddToSeederList(Integer.valueOf(parts[1]) , fileName);
                        }
                        Tracker.GenSeederList(); 
                    }
                } else if (message.equals("list")) {
                    out.println(Tracker.reply_list());
                }
                else if (message.startsWith("details")) {
                    String[] parts = message.split(" ");
                    if (parts.length == 3) {
                        // int id = Integer.valueOf(parts[1]);
                        // String filename = parts[2];
                        List<Integer> trackedids = Tracker.reply_details(parts[2], Integer.valueOf(parts[1]));
                        List<Integer> seederids = Tracker.reply_details_seeder(parts[2], Integer.valueOf(parts[1]));
                        //System.out.println(trackedids);
                        if (trackedids.isEmpty()){
                            out.println("File not tracked by any peer");
                        }
                        else {
                            StringBuilder userInfoBuilder = new StringBuilder();

                            double minWeightedTime = Double.MAX_VALUE;
                            int minid = -1;

                            for (int id : trackedids) {
                                Socket peerSocket = Tracker.findUserById(id).getPeerSocketById(id);
                                PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
                                BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));

                                long startTime = System.currentTimeMillis();
                                peerOut.println("checkActive");

                                // Read the response from the peer
                                String response = null;
                                try {
                                    response = peerIn.readLine();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // Calculate elapsed time
                                long elapsedTime = System.currentTimeMillis() - startTime;

                                // Calculate weighted time
                                double weightedTime = elapsedTime * Math.pow(0.75, Tracker.findUserById(id).getCountDownloads()) * Math.pow(1.25, Tracker.findUserById(id).getCountFailures());

                                // Update minimum weighted time and corresponding ID
                                if (weightedTime < minWeightedTime) {
                                    minWeightedTime = weightedTime;
                                    minid = id;
                                }

                                // Process response
                                if (response != null && response.equals("yes")) {
                                    System.out.println("Peer with ID " + id + " is active.");
                                    userInfoBuilder.append(Tracker.findUserById(id).getUserInfo());
                                } else {
                                    System.out.println("Peer with ID " + id + " is not active.");
                                    Tracker.logoutUser(id);
                                }
                            }

                            for (int seedid : seederids){
                                out.println("Seeder-port: "+ Tracker.findUserById(seedid).getPort() + " File: " + parts[2]);
                            }

                            // Process the peer with minimum weighted time
                            if (minid != -1) {
                                out.println("File " + parts[2] + " BestPeerSocket: " + Tracker.findUserById(minid).getPort() + " Min time is: " + minWeightedTime );
                            }

                            // String allUserInfo = userInfoBuilder.toString();
                            // out.println(allUserInfo);
                        }
                    }else{
                        out.println("Wrong command");
                    }
                }else if(message.startsWith("seeder-inform")){
                    //System.out.println(message);
                    String[] parts = message.split(" ");
                    Tracker.AddToSeederList(Integer.valueOf(parts[2]),parts[4]);
                    Tracker.AddFileIdMatch(Integer.valueOf(parts[2]),parts[4]);
                    System.out.println("Peer with id : "+ parts[2]+ " is now a seeder for file : "+ parts[4]);
                    //It may does not run correctly
                    Tracker.AddPiecesToUser(Integer.valueOf(parts[2]),Tracker.generateFileNames(parts[4]));
                }
                else if(message.startsWith("notify")){
                    String[] parts = message.split(" ");
                    //System.out.println(message);
                    if(parts[1].equals("success")){
                        System.out.println("User "+Tracker.findUserById(Integer.valueOf(parts[9-1]))+" received part of file: "+parts[5-1]+ " by user: "+Tracker.findUserByPort(Integer.valueOf(parts[1+1])));
                        Tracker.findUserByPort(Integer.parseInt(parts[2])).addCountDownloads();
                        System.out.println(Tracker.findUserByPort(Integer.parseInt(parts[2])).getUserInfo());
                    }else{
                        Tracker.findUserByPort(Integer.parseInt(parts[2])).addCountFailures();
                    }
                }
                else if (message.startsWith("Find Best")){
                    List<Integer> ids = Tracker.extractNumbers(message);
                    double minWeightedTime = Double.MAX_VALUE;
                    int minid = -1;
                    StringBuilder userInfoBuilder = new StringBuilder();

                    for (int id : ids) {
                        Socket peerSocket = Tracker.findUserById(id).getPeerSocketById(id);
                        PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
                        BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));

                        long startTime = System.currentTimeMillis();
                        peerOut.println("checkActive");

                        // Read the response from the peer
                        String response = null;
                        try {
                            response = peerIn.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Calculate elapsed time
                        long elapsedTime = System.currentTimeMillis() - startTime;

                        // Calculate weighted time
                        double weightedTime = elapsedTime * Math.pow(0.75, Tracker.findUserById(id).getCountDownloads()) * Math.pow(1.25, Tracker.findUserById(id).getCountFailures());

                        // Update minimum weighted time and corresponding ID
                        if (weightedTime < minWeightedTime) {
                            minWeightedTime = weightedTime;
                            minid = id;
                        }

                        // Process response
                        if (response != null && response.equals("yes")) {
                            System.out.println("Peer with ID " + id + " is active.");
                            userInfoBuilder.append(Tracker.findUserById(id).getUserInfo());
                        } else {
                            System.out.println("Peer with ID " + id + " is not active.");
                            Tracker.logoutUser(id);
                        }
                    }


                    // Process the peer with minimum weighted time
                    if (minid != -1) {
                        out.println("BestPeerSocket: " + Tracker.findUserById(minid).getPort() + " Min time is: " + minWeightedTime );
                    }

                }
            }catch(Exception e){
                System.out.println("An error occured");
                System.out.println(e);
            }
            }
        
        
        } catch (Exception e) {
            System.out.println("An error occured");
        }
    }
}


//
    // if (peerSocket.getInetAddress().equals(Tracker.findUserById(id).getIpAddress())) {
    //     // Send "check active" message to the peer
    //     PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
    //     peerOut.println("checkActive");
        
    //     // Read the response from the peer
    //     BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
    //     String response = peerIn.readLine();
        
    //     if (response != null && response.equals("yes")) {
    //         // Peer is active
    //         System.out.println("Peer with ID " + id + " is active.");
    //     } else {
    //         // Peer is not active or did not respond with "yes"
    //         System.out.println("Peer with ID " + id + " is not active.");
    //     }
        
    //     break; // Exit the loop after sending the message
    