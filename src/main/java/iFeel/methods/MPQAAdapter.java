package iFeel.methods;

import java.util.List;


import java.util.Map;

import iFeel.Method;
import mpqa4lg.opin.config.Config;
import mpqa4lg.opin.entity.Annotation;
import mpqa4lg.opin.entity.Sentence;
import mpqa4lg.opin.featurefinder.ClueFind;
import mpqa4lg.opin.logic.AnnotationHandler;
import mpqa4lg.opin.preprocessor.PreProcess;
import mpqa4lg.opin.supervised.ExpressionPolarityClassifier;

/**
 * @author jpaulo
 * Adapter class to run sentiment analysis from Opinion Finder/MPQA
 * and convert its output to standard implementation.
 */
public class MPQAAdapter extends Method {
	
	private final Config conf;
	private final PreProcess preProcessor;
	private final ClueFind clueFinder;
	private final AnnotationHandler annHandler;
	private final ExpressionPolarityClassifier polarityClassifier;

	/**
	 * 
	 */
	public MPQAAdapter(String lexiconsFolderPath, String modelsFolderPath) {

		this.conf = new Config();
		this.conf.parseCommandLineOptions(new String[] { "", //file with sentences not used
				"-l", lexiconsFolderPath, "-m", modelsFolderPath } );
		this.preProcessor = new PreProcess(conf);
		this.clueFinder = new ClueFind(this.conf);
		this.annHandler = new AnnotationHandler(this.conf);
		this.polarityClassifier = new ExpressionPolarityClassifier(this.conf);
	}
	
	/**
	 * based on original source code
	 * @param text sentence
	 * @return result achieved by code adapted by us, where map's values are polarities
	 */
	private Map<String, String> achievePolarityPredictions(String text) {

		List<Annotation> gateDefaultAnnotations = this.preProcessor.process(text);

		Map<String, List<Annotation>> mapClueAnnotations = this.clueFinder.process(gateDefaultAnnotations);

		List<Sentence> sentences = this.annHandler.buildSentencesFromGateDefault(gateDefaultAnnotations);
		this.annHandler.readInRequiredAnnotationsForPolarityClassifier(sentences, mapClueAnnotations);

		Map<String, String> polarityResult = this.polarityClassifier.process(sentences);
		
		return polarityResult;
	}
	
	@Override
	public int analyseText(String text) {

		Map<String, String> polarityResult = this.achievePolarityPredictions(text);
		//System.out.println(polarityResult);
		
		/*
		 * MPQA/Opinion Finder original code achieves polarity for many separated parts of the text, not whole text. 
		 * Approach used on our research: text is classified with the polarity that occurs more times. 
		 */
		int finalResult = 0;
		for (String p : polarityResult.values()) {			
			if (p.equals("positive")) {
				++finalResult;
			}
			else if (p.equals("negative")) {
				--finalResult;
			}
		}

		if (finalResult > 0) {
			return POSITIVE;
		}
		else if (finalResult < 0) {			
			return NEGATIVE;
		}

		return NEUTRAL;
	}

	/**
	 * do nothing, it's an Adapter class
	 */
	@Override
	public void loadDictionaries() {
	}
	
	@Override
	public String getName(){
		return "MPQAAAdapter";
	}
}
