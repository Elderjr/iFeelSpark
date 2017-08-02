package mpqa4lg.opin.preprocessor.entity;

public class GateDefaultLine
{
    public static final String STRINGATTRIBUTE = "string";
    public static final String LEMMAATTRIBUTE = "lemma";
    public static final String POSATTRIBUTE = "category";
    public static final String GATETOKEN = "GATE_Token";
    public static final String GATESENTENCE = "GATE_Sentence";
    private String word;
    private String lemma;
    private String type;
    private String pos;
    private int spanS;
    private int spanE;
    
    public GateDefaultLine(final String word, final String lemma, final String pos, final int spanS, final int spanE) {
        this.word = word;
        this.lemma = lemma;
        this.pos = pos;
        this.spanS = spanS;
        this.spanE = spanE;
        this.type = "GATE_Token";
    }
    
    public GateDefaultLine(final int spanS, final int spanE) {
        this.spanS = spanS;
        this.spanE = spanE;
        this.type = "GATE_Sentence";
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.spanS);
        sb.append(",");
        sb.append(this.spanE);
        sb.append("\t");
        if (this.type.equals("GATE_Sentence")) {
            sb.append("GATE_Sentence");
        }
        else {
            sb.append("GATE_Token");
            sb.append("\t");
            sb.append("string");
            sb.append("=");
            sb.append("\"");
            sb.append(this.word);
            sb.append("\"");
            sb.append(" ");
            sb.append("lemma");
            sb.append("=");
            sb.append("\"");
            sb.append(this.lemma);
            sb.append("\"");
            sb.append(" ");
            sb.append("category");
            sb.append("=");
            sb.append("\"");
            sb.append(this.pos);
            sb.append("\"");
        }
        return sb.toString();
    }
}
