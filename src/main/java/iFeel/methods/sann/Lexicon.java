package iFeel.methods.sann;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Lexicon {

    public Map<String, Properties> load(String subj, String emotic)  throws IOException{
        Map<String, Properties> words = new HashMap<String, Properties>();
        //InputStream in = getClass().getResourceAsStream(subj);
        BufferedReader isr = new BufferedReader(new FileReader(new File(subj)));
        BufferedReader br = new BufferedReader(isr);
        String lines = br.readLine();

        while(lines!=null){
            String[] attributes = lines.split(" ");
            int index = attributes.length;
            String wordValue = "";
            for(int i = 0; i < index; i++){
                if(attributes[i].contains("word1")){
                    wordValue = attributes[i].split("=")[1];
                    attributes[i] = "";
                    break;
                }
            }
            if(words.containsKey(wordValue)){
                for(int i = 0; i < index; i++){
                    if(attributes[i] != ""){
                        String[] arr = attributes[i].split("=");
                        String key = arr[0];
                        if(key.equals("pos1")){
                            words.get(wordValue).setPos1(arr[1]);
                            break;
                        }                    
                    }
                }
            }
            else{
                Properties ppTemp = new Properties();
                for(int i = 0; i < index; i++){
                    if(attributes[i] != ""){
                        String[] arr = attributes[i].split("=");
                        switch(arr[0]){
                        case "type":
                            ppTemp.setType(arr[1]);
                            break;
                        case "len":
                            ppTemp.setLen(Integer.valueOf(arr[1]));
                            break;
                        case "pos1":
                            ppTemp.setPos1(arr[1]);
                            break;
                        case "stemmed1":
                            ppTemp.setStemmed1(arr[1]);
                            break;
                        case "priorpolarity":
                            ppTemp.setPriorpolarity(arr[1]);
                            break;
                        case "emoticon":
                            ppTemp.setEmoticon(arr[1]);
                            break;
                        default:
                            System.out.println("Case doesn't match! >> "+arr[0]+" "+wordValue);
                        }                  
                    }
                    words.put(wordValue, ppTemp);
                }
            }
            lines = br.readLine();
        }
        br.close();
        //InputStream is = getClass().getResourceAsStream(emotic);
        //br = new BufferedReader(new InputStreamReader(is));
        br = new BufferedReader(new FileReader(new File(emotic)));
        lines = br.readLine();
        lines = br.readLine();
        
        while(!lines.equals("negative")){
            String[] parts = lines.split(" ");
            int size = parts.length;
            for(int i = 0; i < size; i++){
                Properties ppTemp = new Properties();
                ppTemp.setType("strongsubj");
                ppTemp.setPos1("anypos");
                ppTemp.setEmoticon("true");
                ppTemp.setPriorpolarity("positive");
                words.put(parts[i], ppTemp);
            }
            lines = br.readLine();
        }
        lines = br.readLine();
        while(!lines.equals("bad_words")){
            String[] parts = lines.split(" ");
            int size = parts.length;
            for(int i = 0; i < size; i++){
                Properties ppTemp = new Properties();
                ppTemp.setType("strongsubj");
                ppTemp.setPos1("anypos");
                ppTemp.setEmoticon("true");
                ppTemp.setPriorpolarity("negative");
                words.put(parts[i], ppTemp);
            }
            lines = br.readLine();
        }

        while(lines != null){
            Properties ppTemp = new Properties();
            ppTemp.setType("strongsubj");
            ppTemp.setPos1("anypos");
            ppTemp.setPriorpolarity("negative");
            words.put(lines, ppTemp);
            lines = br.readLine();
        }
        br.close();
        return words;
    }
}