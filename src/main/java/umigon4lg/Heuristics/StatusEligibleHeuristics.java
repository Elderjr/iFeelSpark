/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package umigon4lg.Heuristics;

//import javax.ejb.Stateless;

import umigon4lg.Twitter.Tweet;

/**
 *
 * @author C. Levallois
 */
//@Stateless [4LG]
public class StatusEligibleHeuristics {

    private String status;
    private Tweet tweet;

    public StatusEligibleHeuristics() {
    }

    public Tweet applyRules(Tweet tweet, String status) {
        this.status = status;
        this.tweet = tweet;
        isStatusEmpty();
        return tweet;
    }

    private void isStatusEmpty() {
        if (status.isEmpty()) {
            tweet.addToListCategories("002", -1);
        }
    }

}
