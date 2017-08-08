package iFeel.methods.sann;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PbSubj {
    private Map<String, Properties2> learnedPatterns;
    private Map<String, Properties2> ssPatterns;
    private Map<String, Properties2> sortedPatterns = new HashMap<String, Properties2>();
    private double t1Threshold = 5.0;
    private double t2Threshold = 1.0;
    private int plThreshold = 25;
    private int limit = 1;

    public List<Integer> findNeedleInHaystack(List<String> needle, List<String> haystack){
        List<Integer> r = new ArrayList<Integer>();
        int l = needle.size() - 1;
        for(int i = 0; i < haystack.size()-l; i++){
            if(haystack.subList(i, i+l+1).equals(needle)){
                r.add(i);
            }
        }
        return r;
    }

    public boolean searchForObject(String pattern, List<String> words, List<String> tags){
        List<String> patternWords = Arrays.asList(pattern.split(" "));
        List<Integer> position = findNeedleInHaystack(patternWords, words);
        if(position.size()>0){
            int p = position.get(0) + patternWords.size();
            List<String> lTemp = tags.subList(p, tags.size());
            
            for(int i = 0; i < lTemp.size(); i++){
                if(i < limit && lTemp.get(i).contains("n")){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean searchForSubject(String pattern, List<String> words, List<String> tags){
        List<String> patternWords = Arrays.asList(pattern.split(" "));
        List<Integer> position = findNeedleInHaystack(patternWords, words);
        if(position.size()>0){
            int p = position.get(0) - 1;
            List<String> lTemp = tags.subList(p, tags.size());
            for(int i = 0; i < lTemp.size(); i++){
                if(i < limit && lTemp.get(i).contains("n")){
                    return true;
                }
            }
        }
        return false;
    }

    public void selectStrongSubjectivePatterns(){
        ssPatterns = new HashMap<String, Properties2>();
        List<String> lTemp = new ArrayList<String>(learnedPatterns.keySet());
        for(int i = 0; i < lTemp.size(); i++){
            String pattern = lTemp.get(i);
            int freq = learnedPatterns.get(pattern).getFreq();
            double prob = learnedPatterns.get(pattern).getProb();
            if(freq >= t1Threshold && prob >= t2Threshold){
                ssPatterns.put(pattern, learnedPatterns.get(pattern));
            }
            else if(freq > 5 && freq < (t1Threshold*3)/4){
                learnedPatterns.remove(pattern);
            }
        }
        List<Properties2> lp2 = new ArrayList<Properties2>(learnedPatterns.values());
        Collections.sort(lp2);
        for(int i = 0; i < lp2.size(); i++){
            sortedPatterns.put(lp2.get(i).getWord(), lp2.get(i));
        }
        if(lp2.size() > plThreshold){
            t1Threshold++;
        }
    }

    public void train(Map<String, Properties2> learnedPatterns){
        this.learnedPatterns = learnedPatterns;
        selectStrongSubjectivePatterns();
    }

    public String classify(String sentence, Tagger swn){
        boolean found = false;
        boolean subjective;
        boolean objective;
        List<String> taggedSentence = swn.formatingString(sentence);
        List<String> words = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();

        for(int i = 0; i < taggedSentence.size(); i++){
            String[] temp = taggedSentence.get(i).split("#");
            words.add(temp[0]);
            tags.add(temp[1]);
        }

        List<Properties2> lp2 = new ArrayList<Properties2>(sortedPatterns.values());
        double matchedPattern = 0.0;
        for(int i = 0; i < lp2.size(); i++){
            Properties2 value = lp2.get(i);
            String display = value.getDisplay();
            String patternType = value.getType();
            int posInSentence = sentence.indexOf(display);

            if(posInSentence > -1){
                matchedPattern = value.getProb();
                if(patternType.equals("subj")){
                    found = searchForSubject(display, words, tags);
                }
                else if(patternType.equals("dobj") || patternType.equals("np")){
                    found = searchForObject(display, words, tags);
                }
            }
            if(found){
                break;
            }
        }

        if(!found){
            objective = false;
            subjective = false;
        }
        else{
            Random gerador = new Random();
            double rand = gerador.nextDouble();
            if(rand <= matchedPattern){
                subjective = true;
                objective = false;
            }
            else{
                subjective = false;
                objective = true;
            }          
        }
        return ""+subjective+" "+objective;
    }
}
