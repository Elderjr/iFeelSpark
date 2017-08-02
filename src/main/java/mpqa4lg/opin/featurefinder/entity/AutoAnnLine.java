package mpqa4lg.opin.featurefinder.entity;

public class AutoAnnLine
{
    public static final String MATCHFEAT = "matchfeat";
    public static final String TYPEATTRIBUTE = "type";
    public static final String PATTERNATTRIBUTE = "pattern";
    public static final String ORIGPATSATTRIBUTE = "origpats";
    public static final String MPQAPOLARITYATTRIBUTE = "mpqapolarity";
    private String type;
    private String pattern;
    private String origpats;
    private String mpqapolarity;
    private int spanS;
    private int spanE;
    
    public AutoAnnLine(final String type, final String pattern, final String origpats, final String mpqapolarity, final int spanS, final int spanE) {
        this.type = type;
        this.pattern = pattern;
        this.origpats = origpats;
        this.mpqapolarity = mpqapolarity;
        this.spanS = spanS;
        this.spanE = spanE;
    }
    
    public AutoAnnLine(final String type, final String pattern, final String mpqapolarity, final int spanS, final int spanE) {
        this.type = type;
        this.pattern = pattern;
        this.mpqapolarity = mpqapolarity;
        this.spanS = spanS;
        this.spanE = spanE;
    }
    
    public AutoAnnLine(final String type, final String mpqapolarity, final int spanS, final int spanE) {
        this.type = type;
        this.mpqapolarity = mpqapolarity;
        this.spanS = spanS;
        this.spanE = spanE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.spanS);
        sb.append(",");
        sb.append(this.spanE);
        sb.append("\t");
        sb.append("matchfeat");
        sb.append("\t");
        sb.append("type");
        sb.append("=");
        sb.append("\"");
        sb.append(this.type);
        sb.append("\"");
        sb.append(" ");
        if (this.pattern != null) {
            sb.append("pattern");
            sb.append("=");
            sb.append("\"");
            sb.append(this.pattern);
            sb.append("\"");
            sb.append(" ");
        }
        if (this.mpqapolarity != null) {
            sb.append("mpqapolarity");
            sb.append("=");
            sb.append("\"");
            sb.append(this.mpqapolarity);
            sb.append("\"");
        }
        if (this.origpats != null) {
            sb.append("origpats");
            sb.append("=");
            sb.append("\"");
            sb.append(this.origpats);
            sb.append("\"");
        }
        return sb.toString();
    }
}
