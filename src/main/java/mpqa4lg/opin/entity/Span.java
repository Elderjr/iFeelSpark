package mpqa4lg.opin.entity;

public class Span implements Comparable<Span>
{
    private int start;
    private int end;
    private String type;
    private String source;
    private String pol;
    
    public Span(final int start, final int end) {
        this.start = start;
        this.end = end;
    }
    
    public Span(final int start, final int end, final String source) {
        this.start = start;
        this.end = end;
        this.source = source;
    }
    
    public int getStart() {
        return this.start;
    }
    
    public void setStart(final int start) {
        this.start = start;
    }
    
    public int getEnd() {
        return this.end;
    }
    
    public void setEnd(final int end) {
        this.end = end;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public void setSource(final String source) {
        this.source = source;
    }
    
    public String getPol() {
        return this.pol;
    }
    
    public void setPol(final String pol) {
        this.pol = pol;
    }
    
    @Override
    public String toString() {
        return String.valueOf(this.start) + ":" + this.end;
    }
    
    @Override
    public int compareTo(final Span o) {
        if (this.start == o.start && this.end == o.end) {
            return 0;
        }
        if (this.start < o.start) {
            return -1;
        }
        if (this.start > o.start) {
            return 1;
        }
        return o.end - o.start - (this.end - this.start);
    }
    
    @Override
    public boolean equals(final Object obj) {
        final Span s = (Span)obj;
        return this.start == s.start && this.end == s.end;
    }
    
    @Override
    public int hashCode() {
        final String s = String.valueOf(String.valueOf(this.start)) + "," + String.valueOf(this.end);
        return s.hashCode();
    }
}
