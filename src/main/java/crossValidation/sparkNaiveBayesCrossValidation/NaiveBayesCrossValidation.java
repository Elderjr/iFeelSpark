package crossValidation.sparkNaiveBayesCrossValidation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class NaiveBayesCrossValidation {
	
	private static List<Vector[]> createPartitionsFromVectors(Vector[] vectors, int n){
		List<Vector[]> partitions = new LinkedList<>();
		int partitionSize = vectors.length / n;
		int resto = vectors.length - (partitionSize * n);
		int inicio = 0, fim = partitionSize;
		for(int i = 0; i < n; i++) {
			if(i < resto) {
				fim++;
			}
			Vector v[] = new Vector[fim - inicio];
			int index = 0;
			for(int j = inicio; j < fim; j++) {
				v[index] = vectors[j];
				index++;
			}
			inicio = fim;
			fim += partitionSize;
			partitions.add(v);
		}
		return partitions;
	}
	
	private static void createTrainingFilesOfVector(List<Vector[]> partitions, String outpath) {
		for(int i = 0; i < partitions.size(); i++) {
			try {
				FileWriter fw = new FileWriter(new File(outpath + "training" + i + ".txt"));
				BufferedWriter bw = new BufferedWriter(fw);
				for(int j = 0; j < partitions.size(); j++) {
					if(i != j) {
						for(Vector v : partitions.get(j)) {
							bw.write(v.toString());
							bw.newLine();
						}
					}
				}
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void createTestFilesOfVector(List<Vector[]> partitions, String outPath) {
		for(int i = 0; i < partitions.size(); i++) {
			try {
				FileWriter fw = new FileWriter(new File(outPath+"test"+i+".txt"));
				BufferedWriter bw = new BufferedWriter(fw);
				for(Vector v : partitions.get(i)) {
					bw.write(v.toString());
					bw.newLine();
				}
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void createCrossValidationFiles(String dataSetPath, String classificationPath, HashMap<String, Integer> map, String testDir, String trainningDir) {
		LinkedList<String> dataSet = util.Utils.readFileLinesProcessed(dataSetPath);
		LinkedList<String> classification = util.Utils.readFileLines(classificationPath);
		Vector[] vectors = new Vector[dataSet.size()];
		for(int i=0; i<vectors.length; i++) {
			vectors[i] = Vector.createVector(dataSet.get(i), map, classification.get(i));
		}
		List<Vector[]> partitions = NaiveBayesCrossValidation.createPartitionsFromVectors(vectors, 10);
		NaiveBayesCrossValidation.createTestFilesOfVector(partitions, trainningDir);
		NaiveBayesCrossValidation.createTrainingFilesOfVector(partitions, testDir);
	}
}
