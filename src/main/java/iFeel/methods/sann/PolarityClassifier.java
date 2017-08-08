package iFeel.methods.sann;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PolarityClassifier {
    private Stemmer stm = new Stemmer();    
    private Tagger swn;
    private Map<String, Properties> lexicon;
    private String sentence = "";
    private List<String> words = new ArrayList<String>();
    private Map<String, Double> featureWords = new HashMap<String, Double>();
    private List<String> polarExpressions = new ArrayList<String>();
    private Map<String, String> polarWithTags = new HashMap<String, String>();
    private Map<String, Double> polarWithScore = new HashMap<String, Double>();
    private List<String> strongPolarExpressions = new ArrayList<String>();
    private List<String> negationWords = Arrays.asList("not", "no", "but");
    private List<String> wordsPosTags = new ArrayList<String>();
    private List<String> stokens = new ArrayList<String>();                    
    private List<Double> emotionScore = new ArrayList<Double>();
    private List<String> emoticons = new ArrayList<String>();

    public void start(Map<String, Properties> wdict, Tagger swn) throws IOException{
        lexicon = wdict;
        this.swn = swn;
    }

    public List<String> tokenizeWords(String sentence){
        this.stokens = Arrays.asList(sentence.split(" "));
        String[] tempW = sentence.toLowerCase().replaceAll("[^a-zA-Z ']*", "").split(" ");
        List<String> words = Arrays.asList(tempW);        
        this.wordsPosTags = swn.formatingString(sentence);
        for(int i = 0; i < wordsPosTags.size(); i++){
            String[] wTemp = wordsPosTags.get(i).split("#");
            String word = wTemp[0].toLowerCase();
            String tag = wTemp[1];
            if(tag.contains("v") && words.contains(word)){
                words.set(words.indexOf(word), stm.stemWord(word));
            }
        }
        return words;
    }                

    public boolean intensifiedPolar(String polar){
        if(words.indexOf(polar) > 0){
            String previousWord = words.get(words.indexOf(polar) - 1);            
            if(lexicon.containsKey(previousWord) && lexicon.get(previousWord).checkType("strongsubj") &&
                    lexicon.get(previousWord).checkPos1("adj")){
                return true;
            }
        }
        return false;
    }

    public void applyEmotions(){
        double score = 0;
        for(int i = 0; i < this.stokens.size(); i++){
            if(lexicon.containsKey(stokens.get(i)) && lexicon.get(stokens.get(i)).checkEmoticon()){
                if(lexicon.get(stokens.get(i)).checkPriorpolarity("negative")){
                    score = -2.0;
                }else{
                    score = 2.0;
                }
                emoticons.add(stokens.get(i));
                emotionScore.add(score);    
            }
        }
    }

    public void applyWeights(){
        double strongEmotion = 1;
        if(sentence.charAt(sentence.length()-1)=='!'){
            strongEmotion = 1.5;
        }else if(sentence.charAt(sentence.length()-1)=='?'){
            strongEmotion = 0.5;
        }

        for(int i = 0; i < polarExpressions.size(); i++){
            Double temp = 0.0;
            String polar = polarExpressions.get(i);
            if(strongPolarExpressions.contains(polar)){
                temp = polarWithScore.get(polar)*2;
                polarWithScore.put(polar, temp);
            }

            if(intensifiedPolar(polar)){
                temp = polarWithScore.get(polar)*2;
                polarWithScore.put(polar, temp);
            }

            if(polarWithTags.containsKey(polar) && polarWithTags.get(polar).equals("adj")){
                temp = polarWithScore.get(polar)*2;
                polarWithScore.put(polar, temp);
            }

            if(i+1 == polarExpressions.size()){
                temp = polarWithScore.get(polar) * strongEmotion;
                polarWithScore.put(polar, temp);
            }
        }
    }

    public boolean checkPrecedings(String polar){
        if (words.contains(polar) && words.indexOf(polar) >= 6){
            return true;
        }else{
            return false;
        }
    }

    public void extractFeatures(){
        this.words = tokenizeWords(this.sentence);
        double n = 0;
        for(int i = 0; i < words.size(); i++){
            String word = words.get(i).toLowerCase();
            if(lexicon.containsKey(word)){
                if(lexicon.get(word).checkPriorpolarity("positive")){
                    if(featureWords.containsKey(word)){
                        n = featureWords.get(word) + 1.0;
                        featureWords.put(word, n);
                    }else{
                        featureWords.put(word, 1.0);
                    }
                }else if(lexicon.get(word).checkPriorpolarity("negative")){
                    if(featureWords.containsKey(word)){
                        n = featureWords.get(word) - 1.0;
                        featureWords.put(word, n);
                    }else{
                        featureWords.put(word, -1.0);
                    }
                }else{
                    featureWords.put(word, 0.0);
                }
            }
        }
    }

    public String matchTags(String posTag){
        if(posTag.equals("v")){
            return "verb";
        }else if(posTag.equals("a")){
            return "adj";
        }else if(posTag.equals("n")){
            return "noun";
        }else if(posTag.equals("r")){
            return "adverb";
        }else{
            return "anypos";
        }
    }

    public boolean polarityShifting(String polar, List<String> words){
        for(int i = 0; i < words.size(); i++){
            String word = words.get(i);
            if((lexicon.containsKey(word) && lexicon.get(word).checkType("strongsubj") 
                    && !lexicon.get(word).checkPriorpolarity(lexicon.get(polar).getPriorpolarity()) 
                    && !lexicon.get(word).checkPriorpolarity("neutral")) || 
                    (word.length()>2 && word.substring(word.length()-3, word.length()).equals("n't"))){
                return true;
            }
        }
        return false;
    }

    public void negationModeling(){
        for(int i = 0; i < polarExpressions.size(); i++){
            String polar = polarExpressions.get(i);
            boolean hasPrecedings = checkPrecedings(polar);
            for(int j = 0; j < negationWords.size(); j++){
                String neg = negationWords.get(j);
                if(hasPrecedings){
                    if(words.subList(words.indexOf(polar)-5, words.indexOf(polar)+1).contains(neg) || 
                            polarityShifting(polar, words.subList(words.indexOf(polar)-5, words.indexOf(polar)+1))){
                        double tmp = polarWithScore.get(polar) * -1;
                        polarWithScore.put(polar, tmp);
                        break;
                    }
                }else if(words.subList(0, words.indexOf(polar)+1).contains(neg) || 
                        polarityShifting(polar, words.subList(0, words.indexOf(polar)+1))){
                    double tmp = polarWithScore.get(polar) * -1;
                    polarWithScore.put(polar, tmp);
                }
            }
        }
    }

    public String predictClass(){
        double summary = 0;
        List<Double> lS = new ArrayList<Double>(polarWithScore.values());
        for(int i = 0; i < lS.size(); i++){
            summary += lS.get(i);
        }
        for(int i = 0; i < emotionScore.size(); i++){
            summary += Double.valueOf(emotionScore.get(i));
        }
        if(summary > 0){
            return "positive "+summary;
        }else if(summary < 0){
            return "negative "+summary;
        }else{
            return "neutral "+summary;
        }    
    }    

    public void wordSenseDisambiguation(){ 
        if(!polarExpressions.isEmpty()){
            polarExpressions.clear();
        }        
        if(!polarWithScore.isEmpty()){
            polarWithScore.clear();
        }

        for(int i = 0; i < wordsPosTags.size(); i++){   
            String[] temp = wordsPosTags.get(i).split("#");
            String word = temp[0].replaceAll("[^a-zA-Z ']*", "");
            String tag = temp[1];
            String matchedTag = matchTags(tag);
            word = word.toLowerCase();
            String[] words = {word, stm.stemWord(word)};
            for(int j = 0; j < 2; j++){ 
                if(featureWords.containsKey(words[j]) && ((lexicon.containsKey(words[j]) && (lexicon.get(words[j]).checkPos1(matchedTag) || 
                        lexicon.get(words[j]).checkPos1("anypos")) || matchedTag.equals("anypos")))){ 
                    polarExpressions.add(words[j]);
                    polarWithScore.put(words[j], featureWords.get(words[j]));
                    polarWithTags.put(words[j], matchedTag);
                    if(lexicon.get(words[j]).checkType("strongsubj")){
                        strongPolarExpressions.add(words[j]);
                    }
                }
            }
        }
        Set<String> bkp = new HashSet<String>(polarExpressions);
        polarExpressions.clear();
        polarExpressions.addAll(bkp);
    }

    public String classify(String sentence, Map<String, Properties> wdict, Tagger swn) throws IOException{
        start(wdict, swn);
        this.sentence = sentence;
        extractFeatures();
        wordSenseDisambiguation();
        negationModeling();
        applyWeights(); 
        applyEmotions();
        String[] ps = predictClass().split(" ");
        String prediction = ps[0];
        Double score = Double.valueOf(ps[1]);
        //System.out.println("Has Polarity:\nScore: "+score+"\nNormalized Score: "+score/words.size());
        return ""+prediction+" "+score+" "+(score/words.size());
    }
}