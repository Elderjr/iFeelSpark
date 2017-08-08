package mpqa4lg.opin.preprocessor.entity;

public class SentenceLine
{
    private static final String COMMA = ",";
    private int spanS;
    private int spanE;
    
    @Override
    public String toString() {
        return String.valueOf(this.spanS) + "," + this.spanE;
    }
    
    public void setSpanS(final int spanS) {
        this.spanS = spanS;
    }
    
    public void setSpanE(final int spanE) {
        this.spanE = spanE;
    }
}
