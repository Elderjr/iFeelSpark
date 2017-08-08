package iFeel.methods.sann;

public class Properties3{
    private int count = 0;
    private double score = 0.0;
    private double nscore = 0.0;
    public int getCount(){
        return count;
    }
    
    public double getScore(){
        return score;
    }
    
    public double getNscore(){
        return nscore;
    }
    
    public void addCount(int value){
        count = count + value;
    }
    
    public void addScore(double value){
        score = score + value;
    }
    
    public void addNscore(double value){
        nscore = nscore + value;
    }
}
