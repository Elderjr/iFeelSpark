package crossValidation.iFeelCrossValidation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import crossValidation.sparkNaiveBayesCrossValidation.Vector;

public class NonSupervisionalCrossValidation {

	public static List<List<String>> createPartitionsFromText(List<String> texts, int n) {
		List<List<String>> partitions = new LinkedList<>();
		int partitionSize = texts.size() / n;
		int resto = texts.size() - (partitionSize * n);
		int inicio = 0, fim = partitionSize;
		for (int i = 0; i < n; i++) {
			if (i < resto) {
				fim++;
			}
			List<String> partition = new LinkedList<>();
			for (int j = inicio; j < fim; j++) {
				partition.add(texts.get(j));
			}
			inicio = fim;
			fim += partitionSize;
			partitions.add(partition);
		}
		return partitions;
	}

	public static void createFilesOfText(List<List<String>> texts, List<List<String>> classifications, String outPath)
			throws IOException {
		for (int i = 0; i < texts.size(); i++) {
			FileWriter fwTest = new FileWriter(new File(outPath + "test" + i + ".txt"));
			BufferedWriter bwTest = new BufferedWriter(fwTest);
			FileWriter fwClassification = new FileWriter(new File(outPath + "test" + i + "_score.txt"));
			BufferedWriter bwClassification = new BufferedWriter(fwClassification);
			for (String  text : texts.get(i)) {
				bwTest.write(text);
				bwTest.newLine();
			}
			for (String classification : classifications.get(i)) {
				bwClassification.write(classification);
				bwClassification.newLine();
			}
			bwTest.close();
			fwTest.close();
			bwClassification.close();
			fwClassification.close();
		}
	}

	public static void createCrossValidationFiles(String dataSetPath, String classificationPath, String outPath)
			throws IOException {
		LinkedList<String> dataset = util.Utils.readFileLinesProcessed(dataSetPath);
		LinkedList<String> classification = util.Utils.readFileLines(classificationPath);
		List<List<String>> datasetPartitions = NonSupervisionalCrossValidation.createPartitionsFromText(dataset, 10);
		List<List<String>> classificationPartitions = NonSupervisionalCrossValidation
				.createPartitionsFromText(classification, 10);
		NonSupervisionalCrossValidation.createFilesOfText(datasetPartitions, classificationPartitions, outPath);
	}

}
