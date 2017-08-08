package iFeel.methods.sann;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HpClassifiers {
    private Stemmer stm = new Stemmer();

    public boolean hpSubjClassify(String sentence, Map<String, Properties> wdict) throws IOException{
        boolean subjective = false;
        String[] words = sentence.split("\\s+|(?=[^a-zA-Z '])|(?<=[^a-zA-Z '])");
        int index = words.length;
        int subjCount = 0;
        for(int i = 0; i < index; i++){
            List<String> check = new ArrayList<String>();
            check.add(words[i].toLowerCase());
            check.add(stm.stemWord(words[i]));
            int k = 0;
            while(k < 2){
                if(wdict.containsKey(check.get(k)) && wdict.get(check.get(k)).checkType("strongsubj")){
                    subjCount++;
                    if(subjCount >= 2){
                        subjective = true;
                        i = index;
                        break;
                    }
                }
                k++;
            }
        }
        return subjective;
    }

    public boolean hpObjClassify(String current, String previous, String next, Map<String, Properties> wdict) throws IOException{        
        int weakCount = 0;
        boolean objective = true;
        String sentence = current+" "+previous+" "+next;
        String[] words = sentence.split("\\s+|(?=\\p{Punct})|(?<=\\p{Punct})");
        int index = words.length;

        for(int i = 0; i < index; i++){
            List<String> check = new ArrayList<String>();
            check.add(words[i].toLowerCase());
            check.add(stm.stemWord(words[i]));
            int k = 0;
            
            while(k < 2){
                if(wdict.containsKey(check.get(k)) && wdict.get(check.get(k)).checkType("strongsubj")){
                    objective = false;
                    i = index;
                    break;
                }
                else if(wdict.containsKey(check.get(k)) && wdict.get(check.get(k)).checkType("weaksubj")){
                    weakCount++;
                    if(weakCount > 1){
                        objective = false;
                        i = index;
                        break;
                    }
                }
                k++;
            }
        }
        return objective;
    }
}
