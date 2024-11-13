import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class Partition extends Thread {
    private Peer peer; 

    Partition(Peer per){
        peer = per;
    }
    public void run(){
        try{
            for (String fileName : peer.listOfFiles().split(" ")){

                //System.out.println("List of files : "+fileName + "/n");
                fileName = "shared_folder" + peer.getID() + File.separator + "file"+peer.getID()+".txt";
                String outputDirectory = "shared_folder" + peer.getID();
                int numPartitions = 10;
                partitionTextFile(fileName, outputDirectory, numPartitions);

            }
        }
        catch(Exception e){
            System.out.println("An error occcured");
        }
    }

    public void partitionTextFile(String inputFile, String outputDirectory, int numPartitions) {
        try {
            // Create output directory if it doesn't exist
            Files.createDirectories(Paths.get(outputDirectory));

            // Read the lines of the input file
            List<String> lines = Files.readAllLines(Paths.get(inputFile));

            // Calculate approximate lines per partition
            int linesPerPartition = lines.size() / numPartitions;

            // Partition the lines and write to output files
            for (int i = 0; i < numPartitions; i++) {
                List<String> partition = new ArrayList<>();
                for (int j = i * linesPerPartition; j < (i + 1) * linesPerPartition && j < lines.size(); j++) {
                    partition.add(lines.get(j));
                }
                String partitionFilename = outputDirectory + File.separator + "file"+peer.getID()+"-" + (i + 1) + ".txt";
                BufferedWriter writer = new BufferedWriter(new FileWriter(partitionFilename));
                for (String line : partition) {
                    writer.write(line + "\n");
                }
                writer.close();
            }
            //System.out.println("File partitioning completed successfully.");
        } catch (Exception e) {
            System.out.println("An error occured");
            System.out.println(e);
        }
    }

}











