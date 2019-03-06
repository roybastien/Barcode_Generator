package barcodeGenerator;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class adapterMaker {



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
		int pairs = 0;
		double gcContentMin = 0.35;
		double gcContentMax = 0.65;
		int minHammingDistance = 3;
		int maxPolymers = 3;  // Must be set to 2, 3, or 4
		String i5threePrime = "AATGATACGGCGACCACCGAGATCTACAC";
		String i5fivePrime = "ACACTCTTTCCCTACACGACGCTCTTCCGATC*T";
		String i7threePrime = "CAAGCAGAAGACGGCATACGAGAT";
		String i7fivePrime = "GTGACTGGAGTTCAGACGTGTGCTCTTCCGATC*T";
		
		while(pairs < 375){
		String fileIn = "./allCombinationsOf" + barcodeLength + ".txt";
		LinkedList<String> allBarcodes = new LinkedList<String>();
		LinkedList<String> goodBarcodes = new LinkedList<String>();
		LinkedList<String> goodNewBarcodes = new LinkedList<String>();
		LinkedList<String> goodResults = new LinkedList<String>();

		Scanner scan = new Scanner(new File(fileIn));

		while(scan.hasNextLine()){
			allBarcodes.add(scan.nextLine());
		}

		Collections.shuffle(allBarcodes);

		String seedFile = "./ARUP_set2.txt";
		
		Scanner seedScan = new Scanner(new File(seedFile));

		while(seedScan.hasNextLine()){
			goodBarcodes.add(seedScan.nextLine());
		}
		
		scan.close();
		seedScan.close();

		int goodCounter = 0;
		for(String barcode : allBarcodes){
			boolean goodBarcode = validateBarcode(goodBarcodes, barcode, gcContentMin, gcContentMax, minHammingDistance, maxPolymers);
			if (goodBarcode == true && !goodBarcodes.contains(barcode)){
				goodCounter++;
				goodBarcodes.add(barcode);
				String formatted = String.format("%04d", goodCounter);
				goodNewBarcodes.add("IND_" + formatted  + "\t" + barcode);
			}
		}
		
		String header = "Index#\tIndex Sequence\n";
		String fileOut = "goodBarcodesLength" + barcodeLength + "withSampleID_MinGC" + gcContentMin + 
				"_MaxGC" + gcContentMax + "_MinHamm" + minHammingDistance + "_MaxPoly" + maxPolymers;
				
		writeStringLinkedListToFile(header, goodNewBarcodes, fileOut);
		
		int halfSize = goodNewBarcodes.size()/2;
		
		for(int i = 0; i < halfSize; i++){
			goodResults.add(goodNewBarcodes.get(i) + "\t" + goodNewBarcodes.get(i + halfSize) + "\t" + 
					"Unmatched_" + String.format("%04d", i + 1) + "\t" +
					i5threePrime + goodNewBarcodes.get(i).split("\t")[1].toLowerCase() + i5fivePrime + "\t" +
					i7threePrime + goodNewBarcodes.get(i + halfSize).split("\t")[1].toLowerCase() + i7fivePrime);
		}
		
		header = "i5 Index#\ti5 Barcode Sequence\ti7 Index#\ti7 Barcode Sequence\tAdapter#\ti5 Adapter Sequence\ti7 Adapter Sequence\n";
		fileOut =  "Dual_UnMatched_Adapter_Sequences";
		writeStringLinkedListToFile(header, goodResults, fileOut);
		
		System.out.println("Good new adapter pairs: " + goodCounter/2 + "\tGood new barcodes of length " + barcodeLength + 
				" = " + goodCounter + "\t maxPolymers: " + maxPolymers + "\t Hamming Distance:  " +
		 		minHammingDistance + "\t minGC: " + gcContentMin + "\t maxGC: " + gcContentMax);
		
		pairs = goodCounter/2;
		}
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
					return false;
				}
			}
			else if (maxPolymers == 3){
				if (barcode.charAt(i) == barcode.charAt(i-1) && barcode.charAt(i) == barcode.charAt(i-2) ){
					return false;
				}
			}
			else if (maxPolymers == 4){
				if (barcode.charAt(i) == barcode.charAt(i-1) && barcode.charAt(i) == barcode.charAt(i-2) && barcode.charAt(i) == barcode.charAt(i-3) ){
					return false;
				}
			}
		}
		
		return pass;
	}

	public static boolean validateHammingDistance(LinkedList<String> barcodes, String barcode, int minHammingDistance){
		boolean pass = true;
		int distance;

		for (String bar : barcodes){
			distance = calcHammingDistance(bar, barcode);

			if (distance < minHammingDistance){
				return false;
			}
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
	

	public static void writeStringLinkedListToFile(String header, LinkedList<String> barcodes, String filename) throws FileNotFoundException{
		PrintWriter writer = new PrintWriter(filename + ".txt");
		writer.write(header);
		for (String bar : barcodes){
			writer.write(bar + "\n");
		}
		writer.close();
	}

}
