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
 * 
 * Step 2:
 * ClusterOutputFile name is passed in through the command line as args[1]
 * Output to ClusterOutputFile with the format:
 * 1. n lines, listing all of the dimension numbers connected to 
 *    that dimension in order (space separated)
 *    n is the size of the cluster table.
 * 
 * @author Seth Kelley
 */

public class Clusters {
    public static void main(String[] args) {
        if (args.length < 2) {
            StdOut.println("Execute: java -cp bin spiderman.Clusters <dimension INput file> <collider OUTput file>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        StdIn.setFile(inputFile);
        int numDimensions = StdIn.readInt();
        int initialTableSize = StdIn.readInt();
        double capacityThreshold = StdIn.readDouble();

        Cluster[] table = new Cluster[initialTableSize];
        for (int i = 0; i < initialTableSize; i++) {
            table[i] = new Cluster();
        }

        int numDimensionsAdded = 0;
        for (int i = 0; i < numDimensions; i++) {
            int dimensionNumber = StdIn.readInt();
            int canonEvents = StdIn.readInt();
            int dimensionWeight = StdIn.readInt();
            Dimension dimension = new Dimension(dimensionNumber, canonEvents, dimensionWeight);

            int index = dimensionNumber % table.length;
            table[index].addDimension(dimension);
            numDimensionsAdded++;

            if ((double) numDimensionsAdded / table.length >= capacityThreshold) {
                table = rehash(table);
            }
        }

        connectClusters(table);

        StdOut.setFile(outputFile);
        for (Cluster cluster : table) {
            StdOut.print(cluster.getFirstDimension().getdimensionNumber());
            for (Dimension dimension : cluster.dimensions) {
                if (dimension != cluster.getFirstDimension()) {
                    StdOut.print(" " + dimension.getdimensionNumber());
                }
            }
            StdOut.println();
        }
    }

    static Cluster[] rehash(Cluster[] oldTable) {
        Cluster[] newTable = new Cluster[oldTable.length * 2];
        for (int i = 0; i < newTable.length; i++) {
            newTable[i] = new Cluster();
        }

        for (Cluster cluster : oldTable) {
            for (Dimension dimension : cluster.dimensions) {
                int newIndex = dimension.getdimensionNumber() % newTable.length;
                newTable[newIndex].addDimension(dimension);
            }
        }

        return newTable;
    }

    static void connectClusters(Cluster[] table) {
        for (int i = 0; i < table.length; i++) {
            int prevIndex1 = (i - 1 + table.length) % table.length;
            int prevIndex2 = (i - 2 + table.length) % table.length;
    
            Dimension prevDimension1 = table[prevIndex1].getFirstDimension();
            Dimension prevDimension2 = table[prevIndex2].getFirstDimension();
    
            table[i].dimensions.addLast(prevDimension1);
            table[i].dimensions.addLast(prevDimension2);
        }
    }
    static class Dimension {
        private int dimensionNumber;
        private int canonEvents;
        private int dimensionWeight;
    
        public Dimension(int dimensionNumber, int canonEvents, int dimensionWeight) {
            this.dimensionNumber = dimensionNumber;
            this.canonEvents = canonEvents;
            this.dimensionWeight = dimensionWeight;
        }
    
        public int getdimensionNumber() {
            return dimensionNumber;
        }
    
        public int getCanonEvents() {
            return canonEvents;
        }
    
        public int getdimensionWeight() {
            return dimensionWeight;
        }
    }
    static class Cluster {
        LinkedList<Dimension> dimensions= new LinkedList<>();
       
    
        Cluster() {
            dimensions = new LinkedList<>();
        }
    
        void addDimension(Dimension dimension) {
            dimensions.addFirst(dimension);
        }
    
        Dimension getFirstDimension() {
            return dimensions.getFirst();
        }
    }
}

    
       
