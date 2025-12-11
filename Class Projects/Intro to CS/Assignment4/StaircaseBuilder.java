/*
 * Write your program inside the main method to build
 * a staicase in a 2D array of characters according
 * to the assignment description
 *
 * To compile:
 *        javac StaircaseBuilder.java
 * 
 * DO NOT change the class name
 * DO NOT use System.exit()
 * DO NOT change add import statements
 * DO NOT add project statement
 * 
 */
public class StaircaseBuilder {
    
    public static void main(String[] args) {

        int d = Integer.parseInt(args[0]); 
        int bricks = Integer.parseInt(args[1]); 
        char[][] staircase = new char[d][d];

        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                staircase[i][j] = ' ';
            }
        }

        for (int j = 0; j < d && bricks > 0; j++) {
            for (int i = d - 1; i >= d - 1 - j && bricks > 0; i--) {
                staircase[i][j] = 'X';
                bricks--;
            }
        }

        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                System.out.print(staircase[i][j]);
            }
            System.out.println();
        }

        System.out.println("Bricks remaining: " + bricks);
    }
}
