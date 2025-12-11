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
 * SpotInputFile name is passed through the command line as args[2]
 * Read from the SpotInputFile with the format:
 * Two integers (line seperated)
 *      i.    Line one: The starting dimension of Spot (int)
 *      ii.   Line two: The dimension Spot wants to go to (int)
 * 
 * Step 4:
 * TrackSpotOutputFile name is passed in through the command line as args[3]
 * Output to TrackSpotOutputFile with the format:
 * 1. One line, listing the dimenstional number of each dimension Spot has visited (space separated)
 * 
 * @author Seth Kelley
 */

class TrackSpotUtils{
    static class Person {
        String name;
        int sig;
    
        public Person(String name, int sig) {
            this.name = name;
            this.sig = sig;
        }
    }
    
    static class Dimension {
        int dimNum;
        List<Person> peopleInDim;
    
        public Dimension(int dimNum) {
            this.dimNum = dimNum;
            this.peopleInDim = new ArrayList<>();
        }
    
        public void addPerson(Person person) {
            peopleInDim.add(person);
        }
    }
    public static Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
    public static ArrayList<LinkedList<Integer>> clusterTable = new ArrayList<>();
    public static Map<Integer, Dimension> dimsMap = new HashMap<>();
    public static boolean found = false;
    public static Set<Integer> visited = new HashSet<>();
    public static List<Integer> traversalPath = new ArrayList<>();

    public TrackSpotUtils(int tableSize) {
        for (int i = 0; i < tableSize; i++) {
            clusterTable.add(new LinkedList<>());
        }
    }

    public static void addDimToCluster(int dimNum, double threshold) {
        int index = dimNum % clusterTable.size();
        clusterTable.get(index).addFirst(dimNum);
        dimsMap.putIfAbsent(dimNum, new Dimension(dimNum));
        if (getDimCount() / (double) clusterTable.size() >= threshold) {
            rehash();
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

    public static void dfs(int curr, int end) {
        if (visited.contains(curr)) {
            return;
        }
        visited.add(curr);
        traversalPath.add(curr);

        if (curr == end) {
            found = true;
            return;
        }
        List<Integer> neighbors = adjacencyList.getOrDefault(curr, new ArrayList<>());
        for (int neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, end);
                if (found) return;
            }
        }
    }
    public static void readSpiderverse(String filename) {
        StdIn.setFile(filename);
        int numPeople = StdIn.readInt();

        for (int i = 0; i < numPeople; i++) {
            int dimension = StdIn.readInt();
            String name = StdIn.readString();
            int signature = StdIn.readInt();

            Dimension dim = dimsMap.computeIfAbsent(dimension, Dimension::new);
            dim.addPerson(new Person(name, signature));
        }
    }
    
    
}
public class TrackSpot {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java TrackSpot <dimension.in> <spiderverse.in> <spot.in> <trackspot.out>");
            return;
        }

        StdIn.setFile(args[0]);
        int numDim = StdIn.readInt();
        int initialTableSize = StdIn.readInt();
        double threshold = StdIn.readDouble();
        TrackSpotUtils trackSpotutils = new TrackSpotUtils(initialTableSize);
        
        for (int i = 0; i < numDim; i++) {
            int dimNum = StdIn.readInt();
            StdIn.readInt(); // Skip canon events
            StdIn.readInt(); // Skip dimension weight
            TrackSpotUtils.addDimToCluster(dimNum, threshold);
        }
        
        TrackSpotUtils.readSpiderverse(args[1]); // Load people into their dimensions
        TrackSpotUtils.connectClusters();
        TrackSpotUtils.buildAdjacencyList();

        StdIn.setFile(args[2]);
        int startDim = StdIn.readInt();
        int destinationDim = StdIn.readInt();

        StdOut.setFile(args[3]);
        TrackSpotUtils.dfs(startDim, destinationDim);
        if (!TrackSpotUtils.found) {
            StdOut.println("No path found");
        } else {
            // Output every visited node in the traversal order without repetition
            TrackSpotUtils.traversalPath.forEach(dim -> StdOut.print(dim + " "));
            StdOut.println();
        }
        StdOut.close();
    }
}
    