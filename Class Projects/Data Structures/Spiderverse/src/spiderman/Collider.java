package spiderman;

import java.util.*;


/**
 * Steps to implement this class main method:
 * 
 * Step 1:
 * DimensionInputFile name is passed through the command line as args[0]
 * Read from the DimensionsInputFile with the format:
 * 1. The first line with three numbers:
 *      i.    a (int): number of dimensions in the graph
 *      ii.   b (int): the initial size of the cluster table prior to rehashing
 *      iii.  c (double): the capacity(threshold) used to rehash the cluster table 
 * 2. a lines, each with:
 *      i.    The dimension number (int)
 *      ii.   The number of canon events for the dimension (int)
 *      iii.  The dimension weight (int)
 * 
 * Step 2:
 * SpiderverseInputFile name is passed through the command line as args[1]
 * Read from the SpiderverseInputFile with the format:
 * 1. d (int): number of people in the file
 * 2. d lines, each with:
 *      i.    The dimension they are currently at (int)
 *      ii.   The name of the person (String)
 *      iii.  The dimensional signature of the person (int)
 * 
 * Step 3:
 * ColliderOutputFile name is passed in through the command line as args[2]
 * Output to ColliderOutputFile with the format:
 * 1. e lines, each with a different dimension number, then listing
 *       all of the dimension numbers connected to that dimension (space separated)
 * 
 * @author Seth Kelley
 */

class ColliderUtils{
    public static Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
    public static ArrayList<LinkedList<Integer>> clusterTable = new ArrayList<>();
    public static Map<Integer, Dim> dimsMap = new HashMap<>();
    
        public ColliderUtils(int tableSize) {
            for (int i = 0; i < tableSize; i++) {
                clusterTable.add(new LinkedList<>());
            }
        }
        
    
        public static void addDimensionToCluster(int dimNumber, double threshold) {
            int index = dimNumber % clusterTable.size();
            clusterTable.get(index).addFirst(dimNumber);
    
            dimsMap.putIfAbsent(dimNumber, new Dim(dimNumber));
    
            if ((getDimCount() / (double) clusterTable.size()) >= threshold) {
                rehash();
            }
        }
        public static void connectClusters() {
            for (int i = 0; i < clusterTable.size(); i++) {
                LinkedList<Integer> curr = clusterTable.get(i);
                int prevIndex1 = (i - 1 + clusterTable.size()) % clusterTable.size();
                int prevIndex2 = (i - 2 + clusterTable.size()) % clusterTable.size();
    
                if (!clusterTable.get(prevIndex1).isEmpty()) {
                    curr.add(clusterTable.get(prevIndex1).getFirst());
                }
                if (!clusterTable.get(prevIndex2).isEmpty()) {
                    curr.add(clusterTable.get(prevIndex2).getFirst());
                }
            }
        }
    
        public static int getDimCount() {
            int count = 0;
            for (LinkedList<Integer> cluster : clusterTable) {
                count += cluster.size();
            }
            return count;
        }
    
        public static void rehash() {
            ArrayList<LinkedList<Integer>> newTable = new ArrayList<>(clusterTable.size() * 2);
            for (int i = 0; i < clusterTable.size() * 2; i++) {
                newTable.add(new LinkedList<>());
            }
    
            for (LinkedList<Integer> cluster : clusterTable) {
                for (int dim : cluster) {
                    int newIndex = dim % newTable.size();
                    newTable.get(newIndex).addFirst(dim);
                }
            }
    
            clusterTable = newTable;
        }
        public static void buildAdjacencyList() {
            for (LinkedList<Integer> cluster : clusterTable) {
                if (cluster.isEmpty()) continue;
                Integer head = cluster.getFirst();
                adjacencyList.putIfAbsent(head, new ArrayList<>());
        
                for (int dim : cluster) {
                    if (dim != head.intValue()) {
                        adjacencyList.get(head).add(dim);
                        adjacencyList.putIfAbsent(dim, new ArrayList<>());
                        adjacencyList.get(dim).add(head);
                    }
                }
            }
        }
    
        public static void printAdjacencyList(String outputFile) {
            StdOut.setFile(outputFile);
            for (Map.Entry<Integer, List<Integer>> entry : adjacencyList.entrySet()) {
                StdOut.print(entry.getKey() + " ");
                for (Integer connect : entry.getValue()) {
                    StdOut.print(connect + " ");
                }
                StdOut.println();
            }
            StdOut.close();
        }
    
        public static void readSpiderverse(String filename) {
            StdIn.setFile(filename);
            int numPeople = StdIn.readInt();
    
            for (int i = 0; i < numPeople; i++) {
                int dim = StdIn.readInt();
                String name = StdIn.readString();
                int signature = StdIn.readInt(); // Assuming you need this
    
                Dim dimObj = dimsMap.computeIfAbsent(dim, Dim::new);
                dimObj.addPerson(new Person(name, signature));
            }
        }
        static class Person {
            String name;
            int sig;
        
            public Person(String name, int sig) {
                this.name = name;
                this.sig = sig;
            }
        
        }
        static class Dim {
            int dimNum;
            List<Person> peopleInDim;
        
            public Dim(int dimNum) {
                this.dimNum = dimNum;
                this.peopleInDim = new ArrayList<>();
            }
        
            public void addPerson(Person person) {
                peopleInDim.add(person);
            }
        }
}
    

public class Collider {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Collider <dimension file> <spiderverse file> <output file>");
            return;
        }
    
        StdIn.setFile(args[0]);
        int numDim = StdIn.readInt();
        int initialTableSize = StdIn.readInt();
        double threshold = StdIn.readDouble();
    
        ColliderUtils colliderUtils = new ColliderUtils(initialTableSize);
            
        for (int i = 0; i < numDim; i++) {
            int dimNum = StdIn.readInt();
            StdIn.readInt(); // Skip canon events
            StdIn.readInt(); // Skip dimension weight
            ColliderUtils.addDimensionToCluster(dimNum, threshold);
        }
            
        ColliderUtils.readSpiderverse(args[1]); // Read people into their dimensions
        ColliderUtils.connectClusters();
        ColliderUtils.buildAdjacencyList();
        ColliderUtils.printAdjacencyList(args[2]);
    }
}