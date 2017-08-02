package iFeel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import iFeel.methods.Afinn;
import iFeel.methods.Emolex;
import iFeel.methods.EmoticonDS;
import iFeel.methods.Emoticons;
import iFeel.methods.HappinessIndex;
import iFeel.methods.MPQAAdapter;
import iFeel.methods.NRCHashtagSentimentLexicon;
import iFeel.methods.OpinionLexicon;
import iFeel.methods.PanasT;
import iFeel.methods.Sann;
import iFeel.methods.Sasa;
import iFeel.methods.SentiStrengthAdapter;
import iFeel.methods.SentiWordNet;
import iFeel.methods.SenticNet;
import iFeel.methods.Sentiment140Lexicon;
import iFeel.methods.SoCal;
import iFeel.methods.StanfordAdapter;
import iFeel.methods.UmigonAdapter;
import iFeel.methods.Vader;

/**
 * @author jpaulo Design Patterns: Factory (Creator), Singleton
 */
public class MethodCreator {

	private static MethodCreator instance; // singleton

	public synchronized static MethodCreator getInstance() {
		if (instance == null) {
			instance = new MethodCreator();
		}
		return instance;
	}

	public Method createMethod(int methodId) {
		switch (methodId) {
		case Method.AFINN:
			return new Afinn("resources/lexicons/afinn/AFINN-111.txt");
		case Method.EMOLEX:
			return new Emolex("resources/lexicons/emolex/NRC-emotion-lexicon-wordlevel-alphabetized-v0.92.txt");
		case Method.EMOTICONDS:
			return new EmoticonDS("resources/lexicons/emoticonds/emoticon.words.advanced");
		case Method.EMOTICONS:
			return new Emoticons("resources/lexicons/emoticons/positive.txt",
					"resources/lexicons/emoticons/negative.txt", "resources/lexicons/emoticons/neutral.txt");
		case Method.HAPPINESS_INDEX:
			return new HappinessIndex("resources/lexicons/happinessindex/anew.csv");
		case Method.MPQA:
			return new MPQAAdapter("resources/lexicons/mpqa_opinionfinder", "resources/models/mpqa_opinionfinder");
		case Method.NRC_HashTag:
			return new NRCHashtagSentimentLexicon("resources/lexicons/nrchashtagsentiment/unigrams-pmilexicon.txt",
					"resources/lexicons/nrchashtagsentiment/bigrams-pmilexicon.txt",
					"resources/lexicons/nrchashtagsentiment/pairs-pmilexicon.txt");
		case Method.OPINION:
			return new OpinionLexicon("resources/lexicons/opinionlexicon/positive-words.txt",
					"resources/lexicons/opinionlexicon/negative-words.txt");
		case Method.PANAST:
			return new PanasT("resources/lexicons/panas/positive.txt", "resources/lexicons/panas/negative.txt",
					"resources/lexicons/panas/neutral.txt");
		case Method.SANN:
			return new Sann("resources/models/StanfordTagger/english-bidirectional-distsim.tagger",
					"resources/lexicons/sann/emoticons.data", "resources/lexicons/sann/subjclust.tff",
					"resources/lexicons/sann/wordNetDictList.txt");
		case Method.SASA:
			return new Sasa("resources/lexicons/sasa/trainedset4LG.txt");
		case Method.SENTI_STRENGTH:
			return new SentiStrengthAdapter("resources/lexicons/sentistrength");
		case Method.SENTI_WORD_NET:
			return new SentiWordNet("resources/lexicons/sentiwordnet/SentiWordNet_3.0.0_20130122.txt",
					"resources/models/StanfordTagger/english-bidirectional-distsim.tagger");
		case Method.SENTIC_NET:
			return new SenticNet("resources/lexicons/senticnet/senticnet_v3_dataset.tsv");
		case Method.SENTIMENT_140:
			return new Sentiment140Lexicon("resources/lexicons/sentiment140/unigrams-pmilexicon.txt",
					"resources/lexicons/sentiment140/bigrams-pmilexicon.txt",
					"resources/lexicons/sentiment140/pairs-pmilexicon.txt");
		case Method.SO_CAL:
			return new SoCal("resources/lexicons/socal/adj_dictionary1.11.txt",
					"resources/lexicons/socal/adv_dictionary1.11.txt",
					"resources/lexicons/socal/int_dictionary1.11.txt",
					"resources/lexicons/socal/noun_dictionary1.11.txt",
					"resources/lexicons/socal/verb_dictionary1.11.txt", "resources/lexicons/socal/google_dict.txt",
					"resources/models/StanfordTagger/english-bidirectional-distsim.tagger");
		case Method.STANFORD:
			return new StanfordAdapter();
		case Method.UMIGON:
			return new UmigonAdapter("resources/lexicons/umigon");
		case Method.VADER:
			return new Vader("resources/lexicons/vader/vader_sentiment_lexicon.txt");
		default:
			return null;
		}
	}
}
