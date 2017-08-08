package mpqa4lg.opin.supervised.entity;

public class SWSDClueInfo
{
    String clue;
    boolean anotherClue;
    
    public SWSDClueInfo(final String clue, final boolean anotherClue) {
        this.clue = clue;
        this.anotherClue = anotherClue;
    }
    
    public String getClue() {
        return this.clue;
    }
    
    public boolean isAnotherClue() {
        return this.anotherClue;
    }
}
