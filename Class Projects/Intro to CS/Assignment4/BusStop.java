/*
 * Write your program inside the main method to find the order
 * which the bus the student needs to take will arrive
 * according to the assignemnt description. 
 *
 * To compile:
 *        javac BusStop.java
 * 
 * DO NOT change the class name
 * DO NOT use System.exit()
 * DO NOT change add import statements
 * DO NOT add project statement
 * 
 */
public class BusStop {

    public static void main(String[] args) {

        int n = args.length;
        int [] busOrder = new int[n-1];
        for (int i = 0; i < n - 1; i++) {
            busOrder[i] = Integer.parseInt(args[i]);
        }
        int serenaBus = Integer.parseInt(args[n - 1]);
        boolean found=false;

        for (int i = 0; i < busOrder.length; i++) {
            if (busOrder[i] == serenaBus) {
                System.out.println(i+1);
                found=true;
                break;
            }
            }
        if (!found) {
            System.out.println(1000);    
        }
       
    }
       



    
}
