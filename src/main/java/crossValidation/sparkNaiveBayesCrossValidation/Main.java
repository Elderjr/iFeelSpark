package crossValidation.sparkNaiveBayesCrossValidation;

import scala.Tuple2;
import scala.tools.scalap.scalax.rules.scalasig.ScalaSig.Entry;
import spark.Spark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;

import metrics.ConfusionMatrix;
import metrics.ConfusionMatrixSet;
import net.jpountz.util.Utils;


public class Main {
	
	/*
	public static void createFile(String dataSetPath, String classificationPath, HashMap<String, Integer> map) {
		LinkedList<String> dataSet = util.Utils.readFileLinesProcessed(dataSetPath);
		LinkedList<String> classification = util.Utils.readFileLines(classificationPath);
		Vector[] vectors = new Vector[dataSet.size()];
		for(int i=0; i<vectors.length; i++) {
			vectors[i] = Vector.createVector(dataSet.get(i), map, classification.get(i));
		}
		try {
			FileWriter fw = new FileWriter(new File("target/tmp/inputVector.txt"));
			BufferedWriter bw = new BufferedWriter(fw);
			for(Vector vector: vectors) {
				bw.write(vector.toString());
				bw.newLine();
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	public static void main(String[] args) {
		Spark.getInstance().initSpark("Spark Sentiments", "local");
		JavaSparkContext jsc = Spark.getInstance().getContext();
		String dataSetPath = "/home/elderjr/Documents/RI/datasets/stanford_tweets.txt";
		String classificationPath = "/home/elderjr/Documents/RI/datasets/stanford_tweets_so_score.txt";
		Set<String> uniqueWordsSet = util.Utils.readFileWordsToSet(dataSetPath);
		HashMap<String, Integer> map = new HashMap<String,Integer>();
		int index = 1;
		for(String word: uniqueWordsSet) {
			map.put(word, index);
			index++;
		}
		String trainingDir = "/home/elderjr/Documents/RI/naiveBayesCrossValidation/trainning/";
		String testDir = "/home/elderjr/Documents/RI/naiveBayesCrossValidation/test/";
		NaiveBayesCrossValidation.createCrossValidationFiles(dataSetPath, classificationPath, map, trainingDir, testDir);
		ConfusionMatrixSet results = new ConfusionMatrixSet();
		for(int i = 0; i < 10; i++) {
			JavaRDD<LabeledPoint> training = MLUtils.loadLibSVMFile(jsc.sc(), trainingDir+"training"+i+".txt").toJavaRDD();
			JavaRDD<LabeledPoint> test = MLUtils.loadLibSVMFile(jsc.sc(), testDir+"test"+i+".txt").toJavaRDD();
			NaiveBayesModel model = NaiveBayes.train(training.rdd(), 1.0);
			JavaPairRDD<Double, Double> predictionAndLabel =
					test.mapToPair(p -> new Tuple2<>(model.predict(p.features()), p.label()));
			results.addConfusionMatrix(ConfusionMatrix.create(predictionAndLabel.collect()));
		}
		jsc.stop();
		results.printResults();
	}
}