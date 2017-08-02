package mpqa4lg.opin.supervised.entity;

public class Feature
{
    String type;
    String name;
    Object value;
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public Feature(final Object value, final String type) {
        this.value = value;
        this.type = type;
    }
    
    public Feature(final Object value, final String type, final String name) {
        this.value = value;
        this.type = type;
        this.name = name;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    public Object getValue() {
        return this.value;
    }
    
    public void setValue(final Object value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return String.valueOf(this.name) + " : " + this.type + "_" + this.value;
    }
}
