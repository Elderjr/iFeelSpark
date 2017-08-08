package mpqa4lg.opin.entity;

import java.io.*;
import java.util.*;

public class Document
{
    private File textFile;
    private ArrayList<Sentence> sentences;
    private HashMap<Span, String> expressions;
    private String docID;
    
    public Document(final File f) {
        this.textFile = f;
        this.sentences = new ArrayList<Sentence>();
        this.expressions = new HashMap<Span, String>();
        this.docID = String.valueOf(f.getParentFile().getName()) + "_" + f.getName();
    }
    
    public ArrayList<Sentence> getSentences() {
        return this.sentences;
    }
    
    public void setSentences(final ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }
    
    public String getDocID() {
        return this.docID;
    }
    
    public File getTextFile() {
        return this.textFile;
    }
    
    public void addPolarExpression(final Span s, final String p) {
        this.expressions.put(s, p);
    }
}
