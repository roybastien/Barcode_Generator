package barcodeGenerator;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class barcodeMaker {

	static double gcContentMin = 0.35;
	static double gcContentMax = 0.65;
	static int minHammingDistance = 3;
	static int maxPolymers = 3;  // Must be set to 2, 3, or 4
//	static String fivePrime = "AAACTGATTTCGGTTTGGTTACCTTAATGAAGCTTCAACTAGGTCCCTGTCGCACAATTCTTGTGGGCAGTATTTAGTGCTGTAGG";
//	static String threePrime = "CCAAACCTCTCTTGGACATTTTGTCAACACCATGGAGCTTGATAAAGAGGAGCCAGAGAAACCACTGCCTTTTACTTTAAGGAG";
	static String i5threePrime = "AATGATACGGCGACCACCGAGATCTACAC";
	static String i5fivePrime = "ACACTCTTTCCCTACACGACGCTCTTCCGATC*T";
	static String i7threePrime = "CAAGCAGAAGACGGCATACGAGAT";
	static String i7fivePrime = "GTGACTGGAGTTCAGACGTGTGCTCTTCCGATC*T";

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub

		// Calls ComboGenerator to write files of all combinations of barcodes of i length
		// No need to run again
		// File lengths are correct
//		for (int i = 9; i < 13; i++){
//			ComboGenerator.writeCombos(i);
//		}

//		ComboGenerator.writeCombos(8);
		chooseBarcodes(8);

	}


	public static void chooseBarcodes(int barcodeLength) throws FileNotFoundException{
		String filename = "./allCombinationsOf" + barcodeLength + ".txt";
		LinkedList<String> allBarcodes = new LinkedList<String>();
		LinkedList<String> goodBarcodes = new LinkedList<String>();

		Scanner scan = new Scanner(new File(filename));

		while(scan.hasNextLine()){
			allBarcodes.add(scan.nextLine());
		}

		Collections.shuffle(allBarcodes);

		PrintWriter writer = new PrintWriter("./goodBarcodesLength" + barcodeLength + "withSampleID_MinGC" + gcContentMin + 
				"_MaxGC" + gcContentMax + "_MinHamm" + minHammingDistance + "_MaxPoly" + maxPolymers + ".txt");

		int good = 0;
		int bad = 0;

//		String seed = seedGoodBarcodes(allBarcodes);
//
//		if (!seed.equals("")){
//			goodBarcodes.add(seed);
//		}
//		else{
//			System.out.println("Seeding Error.");
//		}
		
		String seedFile = "./ARUP_set2.txt";
		
		Scanner seedScan = new Scanner(new File(seedFile));

		while(seedScan.hasNextLine()){
			goodBarcodes.add(seedScan.nextLine());
		}
		
		System.out.println(goodBarcodes.size());
		String header = "Index#\ti5 Index\t";
		writer.write(header);

		for(String barcode : allBarcodes){
			boolean goodBarcode = validateBarcode(goodBarcodes, barcode, gcContentMin, gcContentMax, minHammingDistance, maxPolymers);
			if (goodBarcode == true && !goodBarcodes.contains(barcode)){
				good++;
				goodBarcodes.add(barcode);
//				writer.write(barcode + "\n");
				String formatted = String.format("%04d", good);
//				writer.write("ARUP_TracerID_" + formatted  + "\t" + fivePrime + barcode + threePrime + "\n");
				writer.write("IND_" + formatted  + "\t" + barcode + "\n");
			}
			else if (goodBarcode == false){
				bad++;
			}
		}

		System.out.println("Good barcodes of length " + barcodeLength + " = " + good + "\t maxPolymers: " + maxPolymers + "\t Hamming Distance:  "
		+ minHammingDistance + "\t minGC: " + gcContentMin + "\t maxGC: " + gcContentMax);
//		System.out.println("Bad barcodes of length " + barcodeLength + " = " + bad);
		writer.close();
		scan.close();
	}


	public static String seedGoodBarcodes(LinkedList<String> allBarcodes){
		String goodBarcode = "";

		for (String barcode : allBarcodes){
			if (validateGCContent(barcode, gcContentMin, gcContentMax) && validatePolymers(barcode, maxPolymers)){
				System.out.println("Seed: " + barcode);
				return barcode;
			}
		}

		return goodBarcode;
	}


	public static boolean validateBarcode(LinkedList<String> barcodes, String barcode, double gcContentMin, double gcContentMax, int minHammingDistance, int maxPolymers){
		boolean pass = false;

		boolean gcGood = validateGCContent(barcode, gcContentMin, gcContentMax);
		boolean polyGood = validatePolymers(barcode, maxPolymers);
		boolean hammingGood = validateHammingDistance(barcodes, barcode, minHammingDistance);

		if(gcGood && hammingGood && polyGood){
			pass = true;
		}
		return pass;
	}

	public static boolean validatePolymers(String barcode, int maxPolymers){
		boolean pass = true;

		if(!(maxPolymers >= 2 && maxPolymers <= 4)){
			System.out.println("maxPolymers not in range 2-4!");
		}

		for (int i = maxPolymers-1; i < barcode.length(); i++){
			if (maxPolymers == 2){
				if (barcode.charAt(i) == barcode.charAt(i-1)){
//					System.out.println(barcode + " has " + maxPolymers + " consecutive bases");
					return false;
				}
			}
			else if (maxPolymers == 3){
				if (barcode.charAt(i) == barcode.charAt(i-1) && barcode.charAt(i) == barcode.charAt(i-2) ){
//					System.out.println(barcode + " has " + maxPolymers + " consecutive bases");
					return false;
				}
			}
			else if (maxPolymers == 4){
				if (barcode.charAt(i) == barcode.charAt(i-1) && barcode.charAt(i) == barcode.charAt(i-2) && barcode.charAt(i) == barcode.charAt(i-3) ){
//					System.out.println(barcode + " has " + maxPolymers + " consecutive bases");
					return false;
				}
			}
			
		}

//		System.out.println(barcode + " doesn't have " + maxPolymers + " consecutive bases");

		return pass;
	}

	public static boolean validateHammingDistance(LinkedList<String> barcodes, String barcode, int minHammingDistance){
		boolean pass = true;
		int distance;

		for (String bar : barcodes){
			distance = calcHammingDistance(bar, barcode);

			if (distance < minHammingDistance){
//				System.out.println("FAILED Hamming Distance of " + distance + " for " + barcode + " and " + bar);
				return false;
			}

//			System.out.println("Hamming Distance of " + distance + " for " + barcode + " and " + bar);
		}

		return pass;
	}

	public static int calcHammingDistance(String a, String b){
		int distance = 0;
		
		for (int i = 0; i < a.length(); i++){
			if (a.charAt(i) != b.charAt(i)){
				distance++;
			}
		}
		
		return distance;
	}

	public static boolean validateGCContent(String barcode, double gcContentMin, double gcContentMax){
		boolean pass = false;
		double gcCount = 0;
		double atCount = 0;

		String[] bases = barcode.split("");
		for (int i = 0; i < barcode.length(); i++){
			if (bases[i].equals("A") || bases[i].equals("T")){
				atCount++;
			}
			else if (bases[i].equals("G") || bases[i].equals("C")){
				gcCount++;
			}
		}

		double gcContent = gcCount / barcode.length();

		if (gcContent < gcContentMax && gcContent > gcContentMin){
			pass = true;
//			System.out.println(barcode + " passing gcContent = " + gcContent);
		}
		else{
//			System.out.println(barcode + " failing gcContent = " + gcContent);
		}

		return pass;
	}

	public static String generateBarcode(double barcodeLength, double gcContentMin, double gcContentMax){
		String[] bases = {"A", "T", "C", "G"};
		int randomNum;
		String barcode = "";

		for (int j = 0; j < barcodeLength; j++){
			randomNum = ThreadLocalRandom.current().nextInt(0, 3 + 1);
			barcode = barcode.concat(bases[randomNum]);
		}

		return barcode;
	}
	
	public static void runExperiments() throws FileNotFoundException{
		for (int i = 9; i < 12; i++){
			chooseBarcodes(i);
			minHammingDistance = 4;
			chooseBarcodes(i);
			minHammingDistance = 3;
			maxPolymers = 3;
			chooseBarcodes(i);
			minHammingDistance = 4;
			chooseBarcodes(i);
			maxPolymers = 2;
			minHammingDistance = 3;
		}


		for (int i = 9; i < 12; i++){
			gcContentMin = 0.2;
			gcContentMax = 0.8;
			chooseBarcodes(i);
			minHammingDistance = 4;
			chooseBarcodes(i);
			minHammingDistance = 3;
			maxPolymers = 3;
			chooseBarcodes(i);
			minHammingDistance = 4;
			chooseBarcodes(i);
			maxPolymers = 2;
			minHammingDistance = 3;
		}
	}

	public static void writeBarcodesToFile(LinkedList<String> barcodes, String filename) throws FileNotFoundException{
		PrintWriter writer = new PrintWriter("/Users/roybastien/Desktop/" + filename + ".txt");
		for (String bar : barcodes){
			writer.write(bar + "\n");
		}
		writer.close();
	}

}
