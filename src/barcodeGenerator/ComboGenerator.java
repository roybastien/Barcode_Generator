package barcodeGenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class ComboGenerator {

	public static void writeCombos (int k) throws FileNotFoundException{
        
//        int k = 10;
        PrintWriter writer = new PrintWriter("./allCombinationsOf" + k + ".txt");
        int count = 0;
        char set1[] = {'A', 'T', 'C', 'G'};
        
        System.out.println("Count should be: " + Math.pow(4, k));
//        System.out.println("Count: " + count);
        printAllKLength(set1, k, writer, count);
        writer.close();
	}
	
	// The method that prints all possible strings of length k.  It is
    //  mainly a wrapper over recursive function printAllKLengthRec()
    static void printAllKLength(char set[], int k, PrintWriter writer, int count) throws FileNotFoundException {
        int n = set.length;        
        printAllKLengthRec(set, "", n, k, writer, count);
    }
 
    // The main recursive method to print all possible strings of length k
    static void printAllKLengthRec(char set[], String prefix, int n, int k, PrintWriter writer, int count) throws FileNotFoundException {
    	
    	
        
    	// Base case: k is 0, print prefix
        if (k == 0) {
//            System.out.println(prefix);
            writer.write(prefix + "\n");
            count++;
//            writer.close();
            return;
        }
 
        // One by one add all characters from set and recursively 
        // call for k equals to k-1
        for (int i = 0; i < n; ++i) {
             
            // Next character of input added
            String newPrefix = prefix + set[i]; 
             
            // k is decreased, because we have added a new character
            printAllKLengthRec(set, newPrefix, n, k - 1, writer, count); 
        }
    }
    
	
	
}
