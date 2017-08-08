package iFeel.methods.sann;

public class Properties {
    private String type = "";    
    private int len = 0;
    private String pos1 = "";
    private String stemmed1 = "n";
    private String priorpolarity = "";
    private String emoticon = "false";

    public void setType(String type){
        this.type = type;
    }
    
    public String getType(){
        return type;
    }
    
    public boolean checkType(String type){
        return this.type.contains(type);
    }

    public void setLen(int len){
        this.len = len;
    }
    
    public int getLen(){
        return len;
    }

    public void setPos1(String pos1){
        this.pos1 = this.pos1.concat(pos1);
    }
    
    public String getPos1(){
        return pos1;
    }
    
    public boolean checkPos1(String pos1){
        return this.pos1.contains(pos1);
    }

    public void setStemmed1(String stemmed1){
        this.stemmed1 = stemmed1;
    }
    
    public String getStemmed1(){
        return stemmed1;
    }

    public void setPriorpolarity(String priorpolarity){
        this.priorpolarity = priorpolarity;
    }
    
    public String getPriorpolarity(){
        return priorpolarity;
    }
    
    public boolean checkPriorpolarity(String priorpolarity){
        return this.priorpolarity.equals(priorpolarity);
    }

    public void setEmoticon(String emoticon){
        this.emoticon = emoticon;
    }
    
    public String getEmoticon(){
        return emoticon;
    }
    
    public boolean checkEmoticon(){
        return this.emoticon.equals("true");
    }
}