package mpqa4lg.opin.entity;

import java.util.*;

public class Annotation
{
    private HashMap<String, String> attributes;
    private String name;
    private String type;
    private int id;
    private int spanS;
    private int spanE;
    
    public Annotation(final String name, final int id, final int start, final int end) {
        this.attributes = new HashMap<String, String>(6);
        this.id = id;
        this.name = name;
        this.spanS = start;
        this.spanE = end;
    }
    
    public HashMap<String, String> getAttributes() {
        return this.attributes;
    }
    
    public String getSingleAttributes(final String key) {
        return this.attributes.get(key);
    }
    
    public boolean hasAttribute(final String key) {
        return this.attributes.containsKey(key);
    }
    
    public void addToAttributes(final String key, final String value) {
        this.attributes.put(key, value);
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setId(final int id) {
        this.id = id;
    }
    
    public int getSpanS() {
        return this.spanS;
    }
    
    public void setSpanS(final int start) {
        this.spanS = start;
    }
    
    public int getSpanE() {
        return this.spanE;
    }
    
    public void setSpanE(final int end) {
        this.spanE = end;
    }
    
    @Override
    public String toString() {
        return "id:" + String.valueOf(this.id) + " spanS:" + this.spanS + " name:" + this.name;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this.spanS == ((Annotation)obj).getSpanS() && this.spanE == ((Annotation)obj).getSpanE();
    }
}
