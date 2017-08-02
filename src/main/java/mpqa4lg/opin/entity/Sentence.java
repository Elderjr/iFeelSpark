package mpqa4lg.opin.entity;

import java.util.*;

public class Sentence
{
    private String senID;
    private int spanS;
    private int spanE;
    private String hpC;
    private String subjC;
    private HashMap<String, TreeMap<Span, ArrayList<Annotation>>> autoAnns;
    private HashMap<String, TreeMap<Span, Annotation>> gatedefaultAnns;
    
    public Sentence(final int spanS, final int spanE, final String docID) {
        this.autoAnns = new HashMap<String, TreeMap<Span, ArrayList<Annotation>>>();
        this.gatedefaultAnns = new HashMap<String, TreeMap<Span, Annotation>>();
        this.senID = String.valueOf(docID) + "_" + spanS + "_" + spanE;
        this.spanS = spanS;
        this.spanE = spanE;
    }
    
    public HashMap<String, TreeMap<Span, ArrayList<Annotation>>> getAutoAnns() {
        return this.autoAnns;
    }
    
    public HashMap<String, TreeMap<Span, Annotation>> getGatedefaultAnns() {
        return this.gatedefaultAnns;
    }
    
    public void addToAutoAnns(final String key, final Annotation value) {
        if (!this.autoAnns.containsKey(key)) {
            final TreeMap<Span, ArrayList<Annotation>> values = new TreeMap<Span, ArrayList<Annotation>>();
            this.autoAnns.put(key, values);
        }
        final Span span = new Span(value.getSpanS(), value.getSpanE());
        if (!this.autoAnns.get(key).containsKey(span)) {
            this.autoAnns.get(key).put(span, new ArrayList<Annotation>());
        }
        this.autoAnns.get(key).get(span).add(value);
    }
    
    public void addToGatedefaultAnns(final String key, final Annotation value) {
        if (!this.gatedefaultAnns.containsKey(key)) {
            final TreeMap<Span, Annotation> values = new TreeMap<Span, Annotation>();
            this.gatedefaultAnns.put(key, values);
        }
        this.gatedefaultAnns.get(key).put(new Span(value.getSpanS(), value.getSpanE()), value);
    }
    
    @Override
    public String toString() {
        return this.senID;
    }
    
    @Override
    public boolean equals(final Object obj) {
        final Sentence s = (Sentence)obj;
        return this.senID.equals(s.getSenID());
    }
    
    @Override
    public int hashCode() {
        return this.senID.hashCode();
    }
    
    public int getSpanS() {
        return this.spanS;
    }
    
    public void setSpanS(final int spanS) {
        this.spanS = spanS;
    }
    
    public int getSpanE() {
        return this.spanE;
    }
    
    public void setSpanE(final int spanE) {
        this.spanE = spanE;
    }
    
    public String getHpC() {
        return this.hpC;
    }
    
    public void setHpC(final String hpC) {
        this.hpC = hpC;
    }
    
    public String getSenID() {
        return this.senID;
    }
    
    public String getSubjC() {
        return this.subjC;
    }
    
    public void setSubjC(final String subjC) {
        this.subjC = subjC;
    }
}
