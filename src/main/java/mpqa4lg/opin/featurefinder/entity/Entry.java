package mpqa4lg.opin.featurefinder.entity;

import java.util.*;

public class Entry
{
    private HashMap<String, String> attributes;
    int id;
    
    public Entry(final int id, final boolean single) {
        this.id = id;
        this.attributes = new HashMap<String, String>();
    }
    
    public int getId() {
        return this.id;
    }
    
    public HashMap<String, String> getAttributes() {
        return this.attributes;
    }
    
    public void addAttributes(final String k, final String v) {
        this.attributes.put(k, v);
    }
    
    @Override
    public String toString() {
        return this.attributes.toString();
    }
}
