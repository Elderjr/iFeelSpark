package iFeel.methods.sann;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bootstrapping {
    private HpClassifiers hpc = new HpClassifiers();
    private PbSubj pbs = new PbSubj();
    private boolean subjective = false;
    private boolean objective = false;
    private Map<String, Properties2> learnedPatterns = new HashMap<String, Properties2>();
    private Map<String, List<String>> syntacticForms;
    private List<String> BE = Arrays.asList("was","were","be","being","am","been","are","is");
    private List<String> HAVE = Arrays.asList("has","have","had");
    private List<String> s = Arrays.asList("BE v*", "HAVE BE v*", "v*", "v* * n*", "v* TO", "HAVE TO BE", "HAVE n*");
    private List<String> d = Arrays.asList("v*", "TO v*", "v* TO");
    private List<String> n = Arrays.asList("n", "v* n", "BE v n", "TO v");

    public String matchUntilNextNN(int i, List<String> tags, List<String> words, String form, String key){
        int matched = 0;
        List<Integer> positionsMatched = new ArrayList<Integer>();
        List<String> learnedPattern = new ArrayList<String>();
        boolean star = false;
        String[] aux = form.split(" "); 

        for(int j = 0; j < aux.length ; j++){
            String ctag = aux[j];
            int next = i + j + 1;
            int inner = 0;
            boolean found = false;

            while(!found && next < tags.size()){
                next += inner;
                if(next < words.size() && ctag.equals("v") && HAVE.contains(words.get(next))){
                    next++;
                    if(next < words.size() && ctag.equals("v") && BE.contains(words.get(next))){
                        next++;
                    }
                }else if(next < words.size() && ctag.equals("v") && BE.contains(words.get(next))){
                    next++;
                }
                if(ctag.equals("*")){
                    star = true;
                }else if(ctag.contains("*")){
                    List<String> ortags = new ArrayList<String>();
                    if(!ctag.contains("|")){
                        ortags.add(ctag);
                    }
                    else{
                        ortags = Arrays.asList(ctag.split("|"));
                    }
                    for(String ortag : ortags){
                        ortag = ortag.replaceAll("[*]", "");
                        if(next < tags.size() && tags.get(next).contains(ortag) && 
                                !positionsMatched.contains(next)){
                            if(star && inner < 2){
                                matched++;
                            }
                            matched++;
                            positionsMatched.add(next);
                            found = true;
                        }
                    }
                }else if(ctag.equals("BE")){
                    if(next < tags.size() && (tags.get(next).contains("v") || tags.get(next).contains("BE")) 
                            && BE.contains(words.get(next)) && !positionsMatched.contains(next) ){
                        matched++;
                        positionsMatched.add(next);
                        found = true;
                    }

                }else if(ctag.equals("HAVE")){
                    if(next < tags.size() && (tags.get(next).contains("v") || tags.get(next).contains("v")) 
                            && HAVE.contains(words.get(next)) && !positionsMatched.contains(next)){
                        matched++;
                        positionsMatched.add(next);
                        found = true;
                    }
                }else if(next < tags.size() && tags.get(next).contains(ctag) && !positionsMatched.contains(next)){
                    matched++;
                    positionsMatched.add(next);
                    found = true;
                }else{
                    found = true;
                }                
                inner++;
            }
        }

        if(key.equals("subj")){
            if(learnedPattern.size()==0){
                learnedPattern.add("<subj>");
            }else{
                learnedPattern.clear();
                learnedPattern.add("<subj>");
            }
        }
        
        for(int i2 = 0; i2 < positionsMatched.size(); i2++){
            learnedPattern.add(words.get(i2));
        }
        if(!key.equals("subj")){
            learnedPattern.add("<"+key+">");
        }
        String pattern = learnedPattern.get(0);
        
        for(int i3 = 1; i3 < learnedPattern.size(); i3++){
            pattern = pattern+" "+learnedPattern.get(i3);
        }

        if(matched == form.length()){
            return pattern;
        }
        else{
            return "";
        }
    }

    public void triggerPatterns(List<String> tags, List<String> words){
        syntacticForms = new HashMap<String, List<String>>();
        syntacticForms.put("subj", s);
        syntacticForms.put("dobj", d);
        syntacticForms.put("np", n);

        List<String> patterns = new ArrayList<String>();
        List<String> lTemp = new ArrayList<String>(syntacticForms.keySet());
        List<String> l2Temp;
        for(int i = 0; i < lTemp.size(); i++){
            String key = lTemp.get(i);
            l2Temp = syntacticForms.get(key);
            for(int j = 0; j < l2Temp.size(); j++){  
                String form = l2Temp.get(j);
                for(int k = 0; k < tags.size(); k++){
                    if(tags.get(k).contains("n")){ 
                        String pattern = matchUntilNextNN(k, tags, words, form, key);
                        if(!pattern.equals("") && !patterns.contains(pattern)){
                            patterns.add(pattern);
                        }
                    }
                }
            }
        }
        for(int i = 0; i < patterns.size(); i++){
            proccessLearnedPattern(patterns.get(i));
        }
    }

    public void learnPatternsFrom(String sentence, Tagger swn){
        List<String> taggedSentence = swn.formatingString(sentence);
        List<String> tags = new ArrayList<String>();
        List<String> words = new ArrayList<String>();
        for(int i = 0; i < taggedSentence.size(); i++){
            String[] tmp = taggedSentence.get(i).split("#");
            words.add(tmp[0]);
            tags.add(tmp[1]);
        }
        triggerPatterns(tags, words);
    }

    public void proccessLearnedPattern(String pattern){
        String key;
        int subjFreq;
        int freq;
        double prob;
        int curSubjFreq = 0;
        
        if(pattern.contains("subj")){
            key = "subj";
        }else if(pattern.contains("dobj")){
            key = "dobj";
        }else{
            key = "np";
        }
        if (subjective){
            curSubjFreq = 1;
        }
        String pkey = pattern.replaceAll("<subj>","");
        pkey = pattern.replaceAll("<dobj>","");
        pkey = pattern.replaceAll("<np>","");
        if(learnedPatterns.containsKey(pattern)){
            subjFreq = learnedPatterns.get(pattern).getSubjFreq() + curSubjFreq;
            freq = learnedPatterns.get(pattern).getFreq() + 1;
            prob = subjFreq/freq;
            learnedPatterns.get(pattern).setValues(subjFreq, freq, prob);
        }else{
            subjFreq = 0;
            freq = 1;
            subjFreq += curSubjFreq;
            prob = subjFreq/freq;
            Properties2 tmp = new Properties2(pattern, key, pkey, freq, subjFreq, prob);
            learnedPatterns.put(pattern, tmp);
        }    
    }

    public void clearLearnedData(){
        this.learnedPatterns.clear();
    }

    public String classify(String sentence, String previous, String next, Map<String, Properties> wdict, Tagger swn) throws IOException{
        subjective = hpc.hpSubjClassify(sentence, wdict);
        if(!subjective){
            pbs.train(learnedPatterns);
            String[] temp = pbs.classify(sentence, swn).split(" ");
            subjective = Boolean.valueOf(temp[0]);
        }

        if(!subjective && !objective){
            objective = hpc.hpObjClassify(sentence, previous, next, wdict);
        }

        if(subjective || objective){
            learnPatternsFrom(sentence, swn);
        }
        else{
            String[] temp = pbs.classify(sentence, swn).split(" ");
            subjective = Boolean.valueOf(temp[0]);
            objective = Boolean.valueOf(temp[1]);
        }
        if(subjective){
            return "subjective";
        }
        else if(objective){
            return "objective";
        }
        else{
            return "";
        }
    }
}
