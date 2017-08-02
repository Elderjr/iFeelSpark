package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;

import com.fasterxml.jackson.core.JsonFactory;
import methods.Method;
import methods.MethodCreator;
import methods.MethodTest;
import scala.Tuple2;
import util.CrossValidation;

public class Main {
	
	public static void main(String[] args) {
		
		Method method = MethodCreator.getInstance().createMethod(Method.SASA_ID);
		//String path = "datasets/stanford_tweets.txt";
		//Spark.getInstance().initSpark("Spark Sentiments", "local");
		//List<String> texts = Spark.getInstance().getContext().textFile(path).collect();
		
		//List<List<String>> partitions = CrossValidation.createPartitionsFromText(texts, 10);
		//CrossValidation.createTestFilesOfText(partitions);
		
		String testPath = "target/tmp/test_text/test";
		double meanMacroF1 = 0.0;
		for(int i = 0; i < 10; i++) {
			double macroF1 = sequencialAnalysis(method, testPath + i + ".txt");
			meanMacroF1 += macroF1;
		}
		meanMacroF1 /= 10.0;
		System.out.println("Mean MacroF1: "+meanMacroF1);
		
		//System.out.println(sequencialAnalysis(method, "datasets/stanford_tweets.txt"));
		//analyseViaSpark(method, path, "spark://192.168.43.72:7077");
		//analyseViaSpark(method, path, "local");
	}
	
	private static double sequencialAnalysis(Method method, String path){
		long init = System.currentTimeMillis();
		List<Integer> results = method.analyseFile(path);
		int pos = 0, neg = 0, neu = 0;
		int correct;
		int[][] matrix = {{0,0,0},{0,0,0},{0,0,0}};
		try {
			FileReader fr = new FileReader(new File("datasets/stanford_tweets_so_score.txt"));
			BufferedReader br = new BufferedReader(fr);
			for(Integer r : results) {
				correct = Integer.parseInt(br.readLine());
				if(r == -1 && correct == -1)
					matrix[0][0] += 1;
				else if(r == -1 && correct == 0)
					matrix[0][1] += 1;
				else if(r == -1 && correct == 1)
					matrix[0][2] += 1;
				else if(r == 0 && correct == -1)
					matrix[1][0] += 1;
				else if(r == 0 && correct == 0)
					matrix[1][1] += 1;
				else if(r == 0 && correct == 1)
					matrix[1][2] += 1;
				else if(r == 1 && correct == -1)
					matrix[2][0] += 1;
				else if(r == 1 && correct == 0)
					matrix[2][1] += 1;
				else 
					matrix[2][2] += 1;
				if(r == Method.POSITIVE) {
					pos++;
				}else if(r == Method.NEGATIVE) {
					neg++;
				}else {
					neu++;
				}
			}
			br.close();
			fr.close();
		}catch(IOException ex) {
			ex.printStackTrace();
		}
		//System.out.println("(POSITIVE, " +pos + ")");
		//System.out.println("(NEGATIVE, " + neg + ")");
		//System.out.println("(NEUTRAL, " + neu + ")");
		//System.out.println("Time: "+ (System.currentTimeMillis() - init));
		for(int i=0; i<3; i++) {
			for(int j=0; j<3; j++) {
				System.out.print(matrix[i][j] + "\t");
			}
			System.out.println();
		}
		double[] recall = {0.0, 0.0, 0.0};
		for(int i=0; i<3; i++) {
			recall[i] = ((double) matrix[i][i]) / ((double) (matrix[0][i] + matrix[1][i] + matrix[2][i]));
		}
		double[] precision = {0.0, 0.0, 0.0};
		for(int i=0; i<3; i++) {
			precision[i] = (double) (matrix[i][i]) / ((double) (matrix[i][0] + matrix[i][1] + matrix[i][2]));
		}
		
		double[] f1 = {0.0, 0.0, 0.0};
		double macroF1 = 0;
		int counter = 0;
		for(int i=0; i<3; i++) {
			f1[i] = (2*precision[i]*recall[i]) / (precision[i] + recall[i]);
			if(!Double.isNaN(f1[i])) {
				macroF1 += f1[i];
				counter++;
			}
		}
		macroF1 /= counter;
		
		System.out.println("Precision: -1: " + precision[0] + ", 0: " + precision[1] + ", 1: " + precision[2]);
		System.out.println("Recall: -1: " + recall[0] + ", 0: " + recall[1] + ", 1: " + recall[2]);
		System.out.println("MacroF1: " + macroF1);
		return macroF1;
	}		
	
	private static void analyseViaSpark(Method method, String path, String master){
		Spark.getInstance().initSpark("Spark Sentiments", master);
		try {
			long init = System.currentTimeMillis();
			List<Tuple2<Integer, Integer>> count = method.analyseFile(Spark.getInstance().getContext(),
					path);
			for(Tuple2<Integer, Integer> tuple : count){
				if (tuple._1 == Method.POSITIVE) {
					System.out.println("(POSITIVE, " + tuple._2 + ")");
				} else if (tuple._1 == Method.NEGATIVE) {
					System.out.println("(NEGATIVE, " + tuple._2 + ")");
				} else {
					System.out.println("(NEUTRAL, " + tuple._2 + ")");
				}
			}
			System.out.println("Time: "+ (System.currentTimeMillis() - init));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
