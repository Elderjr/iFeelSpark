package iFeel.methods;
import iFeel.methods.sann.*;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import iFeel.Method;

public class Sann extends Method{
    private Replacer rp;
    private Lexicon lx = new Lexicon();
    private Tagger swn;
    private Bootstrapping bts = new Bootstrapping();
    private PolarityClassifier plc = new PolarityClassifier();
    private Map<String, Properties> wdict;
    private Set<String> wlist;
    private String dict0 = "";
    private String dict1 = "";
    private String dict2 = "";

    public Sann(String models, String emotic, String subjclust, String wordnetlist){
        swn = new Tagger(models);
        dict0 = emotic;
        dict1 = subjclust;
        dict2 = wordnetlist;
        loadDictionaries();
        rp = new Replacer(wdict, wlist);
    }

    public Set<String> loadWordList(String filename) throws IOException{
        Set<String> wordList = new HashSet<String>();
        File f = new File(filename);			
		BufferedReader br = new BufferedReader(new FileReader(f));
        String aux = br.readLine();
        while(aux!=null){
            if(aux.contains(" ")){
                String[] splits = aux.split(" ");
                for(String s : splits){
                    wordList.add(s);
                }
            }
            else{
                wordList.add(aux);
            }
            aux = br.readLine();
        }
        br.close();
        return wordList;
    }

    public String normalize(String text) throws IOException{
        String txt = "";
        String[] words = text.split(" ");
        for(int i = 0; i < words.length; i++){
            String normal = rp.repeatReplacer(words[i]);
            if(!normal.equals("") && Character.isUpperCase(normal.charAt(0))){
                char[] aux = normal.toCharArray();
                aux[0] = Character.toUpperCase(aux[0]);
                normal = String.valueOf(aux);
            }
            txt = txt+" "+normal;
        }
        return txt;
    }

    public String analyze(String text) throws IOException{
        Reader reader = new StringReader(text);
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);
        List<String> sentences = new ArrayList<String>();

        for (List<HasWord> sentence : dp) {
            String sentenceString = Sentence.listToString(sentence);
            String tempp = sentenceString.toString();
            int len = tempp.length();
            String points = "!.:;?";
            if(points.contains(String.valueOf(tempp.charAt(tempp.length()-1))) 
                    && tempp.length() > 1 && String.valueOf(tempp.charAt(tempp.length()-2)).equals(" ")){
                tempp = tempp.substring(0, len-2)+String.valueOf(tempp.charAt(tempp.length()-1));
            }
            tempp = normalize(tempp);
            sentences.add(tempp);
        }

        int len = sentences.size();
        String final_sent = "";
        if(len > 0){
            Map<String, Properties3> results = new HashMap<String, Properties3>();
            List<String> sentiments = new ArrayList<String>();
            List<Double> scores = new ArrayList<Double>();
            List<Double> nscores = new ArrayList<Double>();
            Properties3 positive = new Properties3();
            Properties3 neutral = new Properties3();
            Properties3 negative = new Properties3();
            results.put("positive", positive);
            results.put("negative", negative);
            results.put("neutral", neutral);
            for(int i = 0; i < len; i++){
                String sentiment = "";
                String previous = "";
                String next = "";
                double score = 0.0;
                double nscore = 0.0;

                if(i == 0 && i+1 < len){
                    next = sentences.get(i+1);
                }
                else if(i != 0 && i < len){
                    if(i + 1 != len){
                        next = sentences.get(i+1);
                    }
                    previous = sentences.get(i-1);
                }

                String result = bts.classify(sentences.get(i), previous, next, wdict, swn);
                if(result.equals("subjective") || result.equals("")){
                    String[] temp = plc.classify(sentences.get(i), wdict, swn).split(" ");
                    sentiment = temp[0];
                    score = Double.valueOf(temp[1]);
                    nscore = Double.valueOf(temp[2]);
                }
                else if(result.equals("objective")){
                    sentiment = "neutral";
                }

                sentiments.add(sentiment);
                scores.add(score);
                nscores.add(nscore);

                if(results.containsKey(sentiment)){
                    Properties3 tppt = results.get(sentiment); 
                    tppt.addNscore(nscore);
                    tppt.addScore(score);
                    tppt.addCount(1);
                }
            }

            double ssum = 0.0;
            for(int i = 0; i < scores.size(); i++){
                ssum += scores.get(i);
            }
            boolean pos = true;
            if(results.get("negative").getCount() > len*1/3){
                pos = false;
            }
            if(ssum > 0 && pos){
                final_sent = "positive";
            }
            else if(ssum == 0){
                final_sent = "neutral";
            }
            else{
                final_sent = "negative";
            }            
        }
        return final_sent;
    }

    public void analyze_file_text(String file_name) throws IOException{
        InputStream is = new FileInputStream(file_name);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String aux = br.readLine();
        double positive = 0.0;
        double negative = 0.0;
        double neutral = 0.0;
        double total = 0.0;
        while(aux!=null){
            String result = analyze(aux);
            switch(result){
            case "positive":
                positive++;
                break;
            case "negative":
                negative++;
                break;
            case "neutral":
                neutral++;
                break;
            }
            aux = br.readLine();
        }
        
        br.close();
        total = positive + negative + neutral;
        System.out.println("\nEnd of File, Finished:");
        if(total > 0){
            System.out.println("Positive: "+positive/total+"%\nNegative: "+negative/total+"%\nNeutral: "+neutral/total+"%");
        }
        else{
            System.out.println("Positive: "+positive+"\nNegative: "+negative+"\nNeutral: "+neutral);
        }
    }
    
    public void loadDictionaries() {
        try {
            wdict = lx.load(dict1, dict0);
            wlist = loadWordList(dict2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int analyseText(String text){
        if(text.length() > 20){ return NEUTRAL; } //Sann takes too long with long sentences
        try {
            String result = analyze(text);
            if(result.equals("positive")){
                return POSITIVE;
            }
            else if(result.equals("negative")){
                return NEGATIVE;
            }
            else{
                return NEUTRAL;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return NEUTRAL;
    }
    
    @Override
    public String getName(){
    	return "Sann";
    }
}