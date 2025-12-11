package spiderman;

import java.util.*;

class Graph {
    private int startDim;
    private int numfDim;
    private ArrayList<Integer> dimIndices;

    private HashMap<Integer, ArrayList<Integer>> adjacencyList;

    Graph() {
        startDim = -1;
        numfDim = 0;
        dimIndices = new ArrayList<Integer>();
        adjacencyList = new HashMap<Integer, ArrayList<Integer>>();
    }

    void addVert(int dim) {
        if (!dimIndices.contains(dim)) {
            dimIndices.add(dim);
            numfDim += 1;
            if (!adjacencyList.containsKey(dim))
                adjacencyList.put(dim, new ArrayList<Integer>());
        }
    }

    void setOrg(int homeDim) {
        startDim = homeDim;
    }

    void addEdge(int dim1, int dim2) {
        ArrayList<Integer> list1 = adjacencyList.get(dim1);
        ArrayList<Integer> list2 = adjacencyList.get(dim2);
        list1.add(dim2);
        list2.add(dim1);
    }

    String getAdjacencyList() {
        String output = "";
        for (int key : adjacencyList.keySet()) {
            output += key + " ";
            for (int value : adjacencyList.get(key)) {
                output += value + " ";
            }
            output += "\n";
        }
        return output;
    }

    private void dfs(boolean[] marked, int[] edgeTo, int vert, ArrayList<Integer> path) {
        marked[dimIndices.indexOf(vert)] = true;
        for (int neighbor : adjacencyList.get(vert)) {
            if (!marked[dimIndices.indexOf(neighbor)]) {
                edgeTo[dimIndices.indexOf(neighbor)] = vert;
                path.add(neighbor);
                dfs(marked, edgeTo, neighbor, path);
            }
        }
    }

    String findPath(int endDim) {
        boolean[] marked = new boolean[dimIndices.size()];
        int[] edgeTo = new int[dimIndices.size()];
        ArrayList<Integer> path = new ArrayList<Integer>();
        dfs(marked, edgeTo, startDim, path);
        String pathString = "";
        pathString += startDim + " ";
        for (int i = 0; i < path.indexOf(endDim); i++) {
            pathString += path.get(i) + " ";
        }
        pathString += endDim;
        return pathString;
    }

    void breadthFirstSearch(boolean[] marked, int[] edgeTo) {
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(startDim);
        marked[dimIndices.indexOf(startDim)] = true;

        while (!queue.isEmpty()) {
            int vert = queue.remove();
            for (int neighbor : adjacencyList.get(vert)) {
                if (!marked[dimIndices.indexOf(neighbor)]) {
                    queue.add(neighbor);
                    marked[dimIndices.indexOf(neighbor)] = true;
                    edgeTo[dimIndices.indexOf(neighbor)] = vert;
                }
            }
        }
    }

    String findShortestPath(int endDim, boolean spiderMode) {
        boolean[] marked = new boolean[dimIndices.size()];
        int[] edgeTo = new int[dimIndices.size()];
        breadthFirstSearch(marked, edgeTo);
        ArrayList<Integer> path = new ArrayList<Integer>();
        String pathString = "";
        int curr = endDim;
        while (edgeTo[dimIndices.indexOf(curr)] != 0) {
            path.add(curr);
            if (curr != endDim && !spiderMode)
                path.add(0, curr);
            curr = edgeTo[dimIndices.indexOf(curr)];
        }
        if (!spiderMode)
            pathString += startDim + " ";
        for (int x : path) {
            pathString += x + " ";
        }
        pathString += startDim;
        return pathString;
    }

    void dijkstraAlgorithm(HashMap<Integer, Integer> weights, HashMap<Integer, Integer> pred,
            HashMap<Integer, Integer> dist) {
        ArrayList<Integer> done = new ArrayList<Integer>();
        ArrayList<Integer> fringe = new ArrayList<Integer>();

        for (int x : dimIndices) {
            if (x != startDim) {
                dist.put(x, -1);
                pred.put(x, null);
            }
        }
        dist.put(startDim, 0);
        fringe.add(startDim);

        while (fringe.size() > 0) {
            int min = fringe.get(0);
            for (int i = 0; i < fringe.size(); i++) {
                int dimension = fringe.get(i);
                if (dist.get(dimension) < dist.get(min)) {
                    min = dimension;
                }
            }
            done.add(min);
            fringe.remove(fringe.indexOf(min));
            for (int neighbor : adjacencyList.get(min)) {
                if (!done.contains(neighbor)) {
                    if (dist.get(neighbor) == -1) {
                        dist.put(neighbor, dist.get(min) + weights.get(min) + weights.get(neighbor));
                        fringe.add(neighbor);
                        pred.put(neighbor, min);
                    } else if (dist
                            .get(neighbor) > (dist.get(min) + weights.get(min) + weights.get(neighbor))) {
                        dist.put(neighbor, dist.get(min) + weights.get(min) + weights.get(neighbor));
                        pred.put(neighbor, min);
                    }
                }
            }
        }

    }

    String findOptimalPath(HashMap<Integer, Integer> weights, HashMap<Integer, Integer> canons, int dim,
            String name,
            int time) {
        HashMap<Integer, Integer> pred = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> dist = new HashMap<Integer, Integer>();
        dijkstraAlgorithm(weights, pred, dist);
        ArrayList<Integer> path = new ArrayList<Integer>();
        String output = "";
        if (dist.get(dim) > time) {
            output += canons.get(dim) - 1 + " " + name;
            output += " FAILED ";
        } else {
            output += canons.get(dim) + " " + name;
            output += " SUCCESS ";
        }

        int curr = dim;
        while (pred.get(curr) != null) {
            path.add(0, curr);
            curr = pred.get(curr);
        }
        output += startDim + " ";
        for (int x : path) {
            output += x + " ";
        }
        return output;
    }
}

class DimClusters{
    public static HashMap<Integer, Integer> dimCanons = new HashMap<Integer, Integer>();
    public static HashMap<Integer, Integer> dimWeights = new HashMap<Integer, Integer>();

    public static ArrayList<ArrayList<Integer>> getDimClusters() {
        int numDim = StdIn.readInt();
        int numBuckets = StdIn.readInt();
        double loadFactor = StdIn.readDouble();
        int tableSize = numBuckets;

        int dimAdded = 0;
        int clustersTotal = 0;

        ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < tableSize; i++) {
            clusters.add(null);
        }
        for (int i = 0; i < numDim; i++) {
            int dim = StdIn.readInt();
            int canon = StdIn.readInt();
            int weight = StdIn.readInt();
            dimCanons.put(dim, canon);
            dimWeights.put(dim, weight);

            StdIn.readLine();
            int index = dim % tableSize;
            dimAdded += 1;
            if (clusters.get(index) == null) {
                clusters.set(index, new ArrayList<Integer>());
                clustersTotal += 1;
            }
            clusters.get(index).add(0, dim);
            if (dimAdded / clustersTotal >= loadFactor) {
                clustersTotal = 0;
                tableSize *= 2;

                ArrayList<ArrayList<Integer>> newClusters = new ArrayList<ArrayList<Integer>>();

                for (int j = 0; j < tableSize; j++)
                    newClusters.add(null);

                for (ArrayList<Integer> cluster : clusters) {
                    if (cluster != null) {
                        for (int dimValue : cluster) {
                            int hashValue = dimValue % tableSize;
                            if (newClusters.get(hashValue) == null) {
                                newClusters.set(hashValue, new ArrayList<Integer>());
                                clustersTotal += 1;
                            }
                            newClusters.get(hashValue).add(0, dimValue);
                        }
                    }
                }
                clusters = newClusters;

            }
        }

        for (int i = 0; i < clusters.size(); i++) {
            int prev1 = i - 1;
            int prev2 = i - 2;

            if (prev1 < 0)
                prev1 += clusters.size();
            if (prev2 < 0)
                prev2 += clusters.size();

            int dim1 = clusters.get(prev1).get(0);
            int dim2 = clusters.get(prev2).get(0);

            clusters.get(i).add(dim1);
            clusters.get(i).add(dim2);
        }
        return clusters;
    }
}

class SpiderversePeople{
    public static HashMap<String, ArrayList<Integer>> getSpiderversePeople() {
        HashMap<String, ArrayList<Integer>> people = new HashMap<String, ArrayList<Integer>>();
        int numPeople = StdIn.readInt();
        for (int i = 0; i < numPeople; i++) {
            int dim = StdIn.readInt();
            String name = StdIn.readString();
            int dimSig = StdIn.readInt();
            ArrayList<Integer> values = new ArrayList<Integer>();
            values.add(dim);
            values.add(dimSig);
            people.put(name, values);
        }
        return people;
    }
}

class Anomalies {
    public static HashMap<String, ArrayList<Integer>> getAnomalies(int hubDim,
            HashMap<String, ArrayList<Integer>> people) {
        HashMap<String, ArrayList<Integer>> anomalies = new HashMap<String, ArrayList<Integer>>();
        for (String name : people.keySet()) {
            int dim = people.get(name).get(0);
            int dimSig = people.get(name).get(1);
            if (dim != dimSig && dim != hubDim) {
                anomalies.put(name, people.get(name));
            }
        }
        return anomalies;
    }
}

class SpiderName{
    public static String getSpiderName(int dim, HashMap<String, ArrayList<Integer>> people) {
        for (String name : people.keySet()) {
            int d = people.get(name).get(0);
            int dimSig = people.get(name).get(1);
            if (d == dimSig && d == dim) {
                return name;
            }
        }
        return null;
    }
}
public class GoHomeMachine {
    public static void main(String[] args) {

        if (args.length < 5) {
            StdOut.println(
                    "Execute: java -cp bin spiderman.GoHomeMachine <dimension INput file> <spiderverse INput file> <hub INput file> <anomalies INput file> <report OUTput file>");
            return;
        }
        Graph graph = new Graph();
        StdIn.setFile(args[0]);
        ArrayList<ArrayList<Integer>> clusters = DimClusters.getDimClusters();
        for (int j = 0; j < clusters.size(); j++) {
            ArrayList<Integer> cluster = clusters.get(j);
            graph.addVert(cluster.get(0));
            for (int i = 1; i < cluster.size(); i++) {
                graph.addVert(cluster.get(i));
                graph.addEdge(cluster.get(0), cluster.get(i));
            }
        }
        StdIn.setFile(args[2]);
        int hubDim = StdIn.readInt();
        graph.setOrg(hubDim);

        StdIn.setFile(args[1]);
        HashMap<String, ArrayList<Integer>> people = SpiderversePeople.getSpiderversePeople();
        StdIn.setFile(args[3]);
        StdOut.setFile(args[4]);
        int numQueries = StdIn.readInt();
        for (int i = 0; i < numQueries; i++) {
            String name = StdIn.readString();
            int time = StdIn.readInt();
            StdOut.println(
                    graph.findOptimalPath(DimClusters.dimWeights, DimClusters.dimCanons, people.get(name).get(1), name, time));

        }

    }
}