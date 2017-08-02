package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import main.Vector;

public class CrossValidation {

	public static List<Vector[]> createPartitionsFromVectors(Vector[] vectors, int n){
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
	
	public static void createTrainingFilesOfVector(List<Vector[]> partitions) {
		for(int i = 0; i < partitions.size(); i++) {
			String fileName = "training"+i;
			try {
				FileWriter fw = new FileWriter(new File("target/tmp/training_naivebayes/"+fileName+".txt"));
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
	
	public static void createTestFilesOfVector(List<Vector[]> partitions) {
		for(int i = 0; i < partitions.size(); i++) {
			String fileName = "test"+i;
			try {
				FileWriter fw = new FileWriter(new File("target/tmp/test_naivebayes/"+fileName+".txt"));
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
	
	public static List<List<String>> createPartitionsFromText(List<String> texts, int n){
		List<List<String>> partitions = new LinkedList<>();
		int partitionSize = texts.size() / n;
		int resto = texts.size() - (partitionSize * n);
		int inicio = 0, fim = partitionSize;
		for(int i = 0; i < n; i++) {
			if(i < resto) {
				fim++;
			}
			List<String> partition = new LinkedList<>();
			for(int j = inicio; j < fim; j++) {
				partition.add(texts.get(j));
			}
			inicio = fim;
			fim += partitionSize;
			partitions.add(partition);
		}
		return partitions;
	}
	
	public static void createTrainingFilesOfText(List<List<String>> partitions) {
		for(int i = 0; i < partitions.size(); i++) {
			String fileName = "training"+i;
			try {
				FileWriter fw = new FileWriter(new File("target/tmp/training_text/"+fileName+".txt"));
				BufferedWriter bw = new BufferedWriter(fw);
				for(int j = 0; j < partitions.size(); j++) {
					if(i != j) {
						for(String text : partitions.get(j)) {
							bw.write(text.toString());
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
	
	public static void createTestFilesOfText(List<List<String>> partitions) {
		for(int i = 0; i < partitions.size(); i++) {
			String fileName = "test"+i;
			try {
				FileWriter fw = new FileWriter(new File("target/tmp/test_text/"+fileName+".txt"));
				BufferedWriter bw = new BufferedWriter(fw);
				for(String text : partitions.get(i)) {
					bw.write(text.toString());
					bw.newLine();
				}
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
