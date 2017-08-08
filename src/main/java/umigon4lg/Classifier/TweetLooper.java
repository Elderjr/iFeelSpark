/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package umigon4lg.Classifier;

import java.io.IOException;
import java.util.List;

//import javax.ejb.Stateless;
//import javax.inject.Inject;

import umigon4lg.LanguageDetection.Cyzoku.util.LangDetectException;
import umigon4lg.Twitter.Tweet;

/**
 *
 * @author C. Levallois
 */
//@Stateless [4LG]
public class TweetLooper {

    List<Tweet> listTweets;
    //@Inject [4LG]
    ClassifierMachine cm;


    public TweetLooper() {
    	this.cm = new ClassifierMachine(); //[4LG]
    }

    public List<Tweet> applyLevel1(List<Tweet> listTweets) throws LangDetectException, IOException {
        this.listTweets = listTweets;

        listTweets = cm.classify(listTweets);
        return listTweets;
    }
}
