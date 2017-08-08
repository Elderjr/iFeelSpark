package iFeel.methods;

import java.util.List;

import iFeel.Method;
import umigon4lg.Classifier.ClassifierMachine;
import umigon4lg.LanguageDetection.Cyzoku.util.LangDetectException;
import umigon4lg.Singletons.HeuristicsLoader;
import umigon4lg.Twitter.ExternalSourceTweetLoader;
import umigon4lg.Twitter.Tweet;

/**
 * @author jpaulo
 * Adapter class to run sentiment analysis from Classifier.ClassifierMachine?
 * and convert its output to standard implementation.
 */
public class UmigonAdapter extends Method {

	private ClassifierMachine umigonClassifierMachine;
	private ExternalSourceTweetLoader comp;
	private HeuristicsLoader hLoader;
	
	/**
	 * 
	 */
	public UmigonAdapter(String dictionariesFolderPath) {

		this.hLoader = HeuristicsLoader.getInitialInstance(dictionariesFolderPath); //instantiate first
		this.comp = new ExternalSourceTweetLoader();
		this.umigonClassifierMachine = new ClassifierMachine();
	}

	@Override
	public int analyseText(String text) {
		
		List<Tweet> listTweets = comp.userInputTweets(text);

		try {
			List<Tweet> tweetsResult = this.umigonClassifierMachine.classify(listTweets);
			
			//parsing polarity
			for (Tweet tweet : tweetsResult) {
				if (tweet.getListCategories().contains("011")) {
					return POSITIVE;
				} else if (tweet.getListCategories().contains("012")) {
					return NEGATIVE;
				}
				break;
			}

		} catch (LangDetectException e) {
			e.printStackTrace();
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
		return "UmigonAdapter";
	}
}
