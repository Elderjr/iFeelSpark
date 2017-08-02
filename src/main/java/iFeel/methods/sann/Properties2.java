package iFeel.methods.sann;
public class Properties2 implements Comparable<Properties2> {
    private String word;
    private String type;
    private String display;
    private int subjFreq;    
    private int freq;
    private double prob;
    
    public interface Comparable<T>{
        int compareTo(T outro);
    }
    public int compareTo(Properties2 outro){
        if(this.prob < outro.prob){
            return 1;
        }else if(this.prob > outro.prob){
            return -1;
        }else{
            return 0;
        }
    }
    
    public Properties2(String word, String type, String display, int subjFreq, int freq, double prob){
        this.word = word;
        this.type = type;
        this.display = display;
        this.subjFreq = subjFreq;
        this.freq = freq;
        this.prob = prob;   
    }
    
    public void setValues(int subjFreq, int freq, double prob){
        this.subjFreq = subjFreq;
        this.freq = freq;
        this.prob = prob;       
    }
    
    public int getSubjFreq(){
        return subjFreq;
    }
    
    public int getFreq(){
        return freq;
    }
    
    public double getProb(){
        return prob;
    }
    
    public String getDisplay(){
        return display;
    }
    
    public String getType(){
        return type;
    }
    
    public String getWord(){
        return word;
    }
}