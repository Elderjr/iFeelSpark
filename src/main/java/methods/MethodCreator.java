package methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author jpaulo
 * Design Patterns: Factory (Creator), Singleton
 */
public class MethodCreator {

	private static MethodCreator instance; //singleton

	public synchronized static MethodCreator getInstance() {
		if (instance == null) {
			instance = new MethodCreator();
		}
		return instance;
	}

	public Method createMethod(int methodId) {
		switch (methodId) {
		case Method.EMOTICON_ID:
			return new Emoticons("resources/lexicons/emoticons/positive.txt",
					"resources/lexicons/emoticons/negative.txt",
					"resources/lexicons/emoticons/neutral.txt");
		case Method.HAPPINESS_INDEX_ID:
			return new HappinessIndex("resources/lexicons/happinessindex/anew.csv");
		case Method.SENTI_WORD_NET_ID:
			return new SentiWordNet("resources/lexicons/sentiwordnet/SentiWordNet_3.0.0_20130122.txt",
					"resources/models/StanfordTagger/english-bidirectional-distsim.tagger");
		case Method.SASA_ID:
			return new Sasa("resources/lexicons/sasa/trainedset4LG.txt");
		case Method.SENTI_STRENGTH_ID:
			//return new SentiStrengthAdapter("resources/lexicons/sentistrength");
			return new SentiStrengthAdapter("resources/lexicons/portuguese/sentistrength");
		default:
			return null;
		}
	}
}
