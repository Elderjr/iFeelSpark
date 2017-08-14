package performace;

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

import iFeel.Method;
import iFeel.MethodCreator;
import scala.Tuple2;
import spark.Spark;

public class Main {
	
	public static void main(String[] args) {
		Method method = MethodCreator.getInstance().createMethod(Method.EMOTICONDS);
		String datasetPath = "/tmp/guest-eidroz/big_dataset.txt";
		sequencialAnalysis(method, datasetPath);
		//analyseViaSpark(method, datasetPath, "spark://177.105.60.200:7077");
	}
	
	private static void sequencialAnalysis(Method method, String path){
		long init = System.currentTimeMillis();
		List<Integer> results = method.analyseFile(path);
		System.out.println("Time: "+(System.currentTimeMillis() - init));
		int pos = 0, neu = 0, neg = 0;
		for(int result : results){
			if(result == Method.POSITIVE){
				pos++;
			}else if(result == Method.NEUTRAL){
				neu++;
			}else if(result == Method.NEGATIVE){
				neg++;
			}
		}
		System.out.println("POSTIVE: "+pos);
		System.out.println("NEUTRAL: "+neu);
		System.out.println("NEGATIVE: "+neg);
	}		
	
	private static void analyseViaSpark(Method method, String path, String master){
		Spark.getInstance().initSpark("Spark Sentiments", master);
		try {
			long init = System.currentTimeMillis();
			List<Tuple2<Integer, Integer>> count = method.analyseFile(Spark.getInstance().getContext(),
					path);
			System.out.println("Time: "+ (System.currentTimeMillis() - init));
			for(Tuple2<Integer, Integer> tuple : count){
				if (tuple._1 == Method.POSITIVE) {
					System.out.println("(POSITIVE, " + tuple._2 + ")");
				} else if (tuple._1 == Method.NEGATIVE) {
					System.out.println("(NEGATIVE, " + tuple._2 + ")");
				} else {
					System.out.println("(NEUTRAL, " + tuple._2 + ")");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
