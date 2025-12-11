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
 * HubInputFile name is passed through the command line as args[2]
 * Read from the HubInputFile with the format:
 * One integer
 *      i.    The dimensional number of the starting hub (int)
 * 
 * Step 4:
 * CollectedOutputFile name is passed in through the command line as args[3]
 * Output to CollectedOutputFile with the format:
 * 1. e Lines, listing the Name of the anomaly collected with the Spider who
 *    is at the same Dimension (if one exists, space separated) followed by 
 *    the Dimension number for each Dimension in the route (space separated)
 * 
 * @author Seth Kelley
 */


class Person {
    String name;
    int sig;
    int currentDimension;


    public Person(int dimension, String name, int signature) {
        this.currentDimension = dimension;
        this.name = name;
        this.sig = signature;
    }
    public void updateDimension(int newDimension) {
        this.currentDimension = newDimension;
    }
}


class Dim {
    int dimNum;
    List<Person> peopleInDim;


    public Dim(int dimNum) {
        this.dimNum = dimNum;
        this.peopleInDim = new ArrayList<>();
    }


    public void addPerson(Person person) {
        peopleInDim.add(person);
    }


    public boolean hasAnomaly() {
        for (Person p : peopleInDim) {
            if (p.sig != dimNum) {
                return true;
            }
        }
        return false;
    }
}

class AnomalyUtils{
    private static Map<Integer, Dim> dims = new HashMap<>();
    private static Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
    private static ArrayList<LinkedList<Integer>> clusterTable = new ArrayList<>();
    private static int maxDimId = 0;


    public AnomalyUtils(int tableSize) {
        for (int i = 0; i < tableSize; i++) {
            clusterTable.add(new LinkedList<>());
        }
    }


    public static void addDimToCluster(int dimNum, double threshold) {
        int index = dimNum % clusterTable.size();
        clusterTable.get(index).addFirst(dimNum);
        dims.putIfAbsent(dimNum, new Dim(dimNum));
        maxDimId = Math.max(maxDimId, dimNum);
        if ((double) getDimCount() / clusterTable.size() >= threshold) {
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

    public static void readSpiderverse(String filename) {
        StdIn.setFile(filename);
        int numPeople = StdIn.readInt();


        for (int i = 0; i < numPeople; i++) {
            int dim = StdIn.readInt();
            String name = StdIn.readString();
            int sig = StdIn.readInt();


            Dim dimobj = dims.computeIfAbsent(dim, Dim::new);
            dimobj.addPerson(new Person(dim, name,sig));
        }
    }


    public static void bfsAndHandleAnomalies(int hubDim, String outputFile) {
        StdOut.setFile(outputFile);
        Queue<Integer> queue = new LinkedList<>();
        Map<Integer, Integer> parent = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
    
        queue.add(hubDim);
        visited.add(hubDim);
        parent.put(hubDim, null);
    
        while (!queue.isEmpty()) {
            int curr = queue.poll();
            for (int neighbor : adjacencyList.getOrDefault(curr, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, curr);
                    queue.add(neighbor);
                }
            }
        }
    
        for (Integer dimNum : dims.keySet()) {
            Dim currDim = dims.get(dimNum);
            if (dimNum != hubDim && currDim.hasAnomaly()) {
                List<Integer> path = getPath(parent, dimNum);
                Person spider = currDim.peopleInDim.stream()
                                        .filter(p -> p.sig == dimNum)
                                        .findFirst().orElse(null);
    
                for (Person person : currDim.peopleInDim) {
                    if (person.sig != dimNum) {  // This person is an anomaly
                        StringBuilder output = new StringBuilder();
                        output.append(person.name).append(" ");
                        if (spider != null) {
                            output.append(spider.name).append(" ");  // Output Spider's name if exists
    
                            // Only output the reversed path when Spider is present, starting from the anomaly's dimension
                            List<Integer> returnPath = new ArrayList<>(path);
                            Collections.reverse(returnPath);
                            returnPath.forEach(p -> output.append(p).append(" "));
                        } else {
                            // Normal output when Spider is not present
                            path.forEach(p -> output.append(p).append(" "));
                            List<Integer> returnPath = new ArrayList<>(path.subList(0, path.size() - 1));
                            Collections.reverse(returnPath);
                            returnPath.forEach(p -> output.append(p).append(" "));
                        }
                        // Trim the StringBuilder to remove any trailing space and print
                        StdOut.println(output.toString().trim());
                        
                        // Update dimension of the anomaly to the hub dimension
                        person.updateDimension(hubDim);
                    }
                }
                // Update the Spider's dimension if present
                if (spider != null) {
                    spider.updateDimension(hubDim);
                }
            }
        }
    
        StdOut.close();
    }

    public static List<Integer> getPath(Map<Integer, Integer> parent, Integer target) {
        LinkedList<Integer> path = new LinkedList<>();
        while (target != null) {
            path.addFirst(target);
            target = parent.get(target);
        }
        return path;
    }

}


public class CollectAnomalies {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java CollectAnomalies <dimension file> <spiderverse file> <hub file> <output file>");
            System.exit(1);
        }


        StdIn.setFile(args[0]);
        int numDim = StdIn.readInt();
        int initialTableSize = StdIn.readInt();
        double threshold = StdIn.readDouble();
        AnomalyUtils anomalyUtils = new AnomalyUtils(initialTableSize);


        for (int i = 0; i < numDim; i++) {
            int dimNum = StdIn.readInt();
            StdIn.readInt(); // Skip canon events
            StdIn.readInt(); // Skip dimension weight
            AnomalyUtils.addDimToCluster(dimNum, threshold);
        }

        

        AnomalyUtils.connectClusters();
        AnomalyUtils.readSpiderverse(args[1]);
        AnomalyUtils.buildAdjacencyList();



        StdIn.setFile(args[2]);
        int hubDim = StdIn.readInt();


        StdOut.setFile(args[3]);
        AnomalyUtils.bfsAndHandleAnomalies(hubDim,args[3]);
        StdOut.close(); 
    }
}
        