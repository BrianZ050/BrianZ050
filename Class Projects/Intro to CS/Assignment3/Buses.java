/*
 *
 * Write the Buses program inside the main method
 * according to the assignment description.
 * 
 * To compile:
 *        javac Buses.java
 * To execute:
 *        java Buses 7302
 * 
 * DO NOT change the class name
 * DO NOT use System.exit()
 * DO NOT change add import statements
 * DO NOT add project statement
 * 
 */

public class Buses {
    public static void main(String[] args) {

        int num=Integer.parseInt(args[0]);
        int sum=0;
        boolean error=false;
        if (num<0)
        {
            System.out.print("ERROR");
            error=true;
        }
       
        if(error==false){
             while (num>0)
            {
            int digit=num%10;
             sum=sum+digit;
             num=num/10;
            }

            if (sum%2==0)
            {
                System.out.println("LX");
            }
            else 
            {
                System.out.println("H");
            }
        }

    }
}
