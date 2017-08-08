package spark;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;

public class Spark {

	private static Spark instance = new Spark();
	private JavaSparkContext sc;
	
	private Spark(){
		
	}
	
	public static Spark getInstance(){
		return instance;
	}
	
	public JavaSparkContext getContext(){
		return this.sc;
	}
	public void initSpark(String appName, String master){
		String jars[] = {"target/sparkSentiment-0.0.1-SNAPSHOT.jar",
				"lib/javacsv-2.01.jar",
				"lib/twitter4j-core-4.0.1.jar",
				"lib/jsonic-1.2.7.jar",
				"lib/weka.jar",
				"lib/ejml-0.23.jar",
				"lib/SentiStrength.jar",
				"lib/stanford-corenlp-3.5.2.jar",
				"lib/stanford-corenlp-models-current-4LG.jar"};
		SparkConf conf = new SparkConf().
				setAppName(appName).
				setMaster(master)
				.setJars(jars);
		this.sc = new JavaSparkContext(conf);
	}
}
