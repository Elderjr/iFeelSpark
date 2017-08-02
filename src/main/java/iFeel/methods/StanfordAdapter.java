package iFeel.methods;

import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import iFeel.Method;

/**
 * @author jpaulo
 * Adapter class to run sentiment analysis from stanford-corenlp-3.5.2.jar
 * (and others) and convert its output to standard implementation.
 */
public class StanfordAdapter extends Method {

	private StanfordCoreNLP stanfordNLP;

	/**
	 * 
	 */
	public StanfordAdapter() {

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment"); //from SentimentPipeline.main

		this.stanfordNLP = new StanfordCoreNLP(props);
	}
	
	@Override
	public int analyseText(String text) {

		//from SentimentPipeline.main: Map<Class, List<Map<Class, Object>. List has size = 1 and Object in this key is String.
		Annotation ann = this.stanfordNLP.process(text);

		//output possible values: Very positive, Positive, Neutral, Very Negative, Negative
		String out;
		try {
			out = ann.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(SentimentCoreAnnotations.SentimentClass.class);
		} catch (java.lang.IndexOutOfBoundsException E){
			out = "Neutral";
		}

//using StanfordNLP.prettyPrint didn't work		
/*		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		this.stanford.prettyPrint(ann, pw);
		out = sw.toString();
		pw.close();		
*/
// iteractive solution
/*		try {
			SentimentPipeline.help();
			SentimentPipeline.main(new String[] {"-stdin"});
		} catch (IOException e) {
			e.printStackTrace();
		}
*/
//		System.out.println("Out: " + out);
		if (out.contains("ositive")) {
			return POSITIVE;
		}
		else if (out.contains("egative")) {
			return NEGATIVE;
		}
		else if (out.contains("eutral")) {
			return NEUTRAL;
		}

		return NEUTRAL;
	}

	/**
	 * do nothing, it's an Adapter class
	 */
	@Override
	public void loadDictionaries() {
	}

	/**
	 *
	@Override
	public void analyseFile(String filePath) {
		
		//super.analyseFile(filePath);

//output in xml file, taking so much time
		java.util.List<File> fileList = new java.util.ArrayList<>();
		fileList.add(new File(filePath));
		
		try {
			this.stanfordNLP.processFiles(fileList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 */
	
	@Override
	public String getName(){
		return "StanfordAdapter";
	}
}
