package mpqa4lg.opin.entity;

public class FeatureVector
{
    private String dir;
    private String document;
    private int spanS;
    private int spanE;
    private double confidence;
    private int strong;
    private int weak;
    private int patternsubj;
    private int patternobj;
    private int modal;
    private int cardinal;
    private int pronoun;
    private int adjective;
    private int adverb;
    private int strongA;
    private int weakA;
    private int patternsubjA;
    private int patternobjA;
    private int modalA;
    private int cardinalA;
    private int pronounA;
    private int adjectiveA;
    private int adverbA;
    private String c;
    
    public int getStrong() {
        return this.strong;
    }
    
    public void setStrong(final int strong) {
        this.strong = strong;
    }
    
    public int getWeak() {
        return this.weak;
    }
    
    public void setWeak(final int weak) {
        this.weak = weak;
    }
    
    public int getPatternsubj() {
        return this.patternsubj;
    }
    
    public void setPatternsubj(final int patternsubj) {
        this.patternsubj = patternsubj;
    }
    
    public int getPatternobj() {
        return this.patternobj;
    }
    
    public void setPatternobj(final int patternobj) {
        this.patternobj = patternobj;
    }
    
    public int getModal() {
        return this.modal;
    }
    
    public void setModal(final int modal) {
        this.modal = modal;
    }
    
    public int getCardinal() {
        return this.cardinal;
    }
    
    public void setCardinal(final int cardinal) {
        this.cardinal = cardinal;
    }
    
    public int getPronoun() {
        return this.pronoun;
    }
    
    public void setPronoun(final int pronoun) {
        this.pronoun = pronoun;
    }
    
    public int getAdjective() {
        return this.adjective;
    }
    
    public void setAdjective(final int adjective) {
        this.adjective = adjective;
    }
    
    public int getAdverb() {
        return this.adverb;
    }
    
    public void setAdverb(final int adverb) {
        this.adverb = adverb;
    }
    
    public int getStrongA() {
        return this.strongA;
    }
    
    public void setStrongA(final int strongA) {
        this.strongA = strongA;
    }
    
    public int getWeakA() {
        return this.weakA;
    }
    
    public void setWeakA(final int weakA) {
        this.weakA = weakA;
    }
    
    public int getPatternsubjA() {
        return this.patternsubjA;
    }
    
    public void setPatternsubjA(final int patternsubjA) {
        this.patternsubjA = patternsubjA;
    }
    
    public int getPatternobjA() {
        return this.patternobjA;
    }
    
    public void setPatternobjA(final int patternobjA) {
        this.patternobjA = patternobjA;
    }
    
    public int getModalA() {
        return this.modalA;
    }
    
    public void setModalA(final int modalA) {
        this.modalA = modalA;
    }
    
    public int getCardinalA() {
        return this.cardinalA;
    }
    
    public void setCardinalA(final int cardinalA) {
        this.cardinalA = cardinalA;
    }
    
    public int getPronounA() {
        return this.pronounA;
    }
    
    public void setPronounA(final int pronounA) {
        this.pronounA = pronounA;
    }
    
    public int getAdjectiveA() {
        return this.adjectiveA;
    }
    
    public void setAdjectiveA(final int adjectiveA) {
        this.adjectiveA = adjectiveA;
    }
    
    public int getAdverbA() {
        return this.adverbA;
    }
    
    public void setAdverbA(final int adverbA) {
        this.adverbA = adverbA;
    }
    
    public String toArffNominalString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.dir);
        builder.append("|");
        builder.append(this.document);
        builder.append("|");
        builder.append(this.spanS);
        builder.append("|");
        builder.append(this.spanE);
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.strong, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.weak, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.patternsubj, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.patternobj, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.modal, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.cardinal, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.pronoun, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.adjective, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.adverb, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.strongA, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.weakA, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.patternsubjA, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.patternobjA, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.modalA, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.cardinalA, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.pronounA, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.adjectiveA, 2));
        builder.append(",");
        builder.append(this.fromNumericToNominal(this.adverbA, 2));
        builder.append(",");
        builder.append(this.c);
        return builder.toString();
    }
    
    public String toArffNumericString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.dir);
        builder.append("|");
        builder.append(this.document);
        builder.append("|");
        builder.append(this.spanS);
        builder.append("|");
        builder.append(this.spanE);
        builder.append(",");
        builder.append(this.strong);
        builder.append(",");
        builder.append(this.weak);
        builder.append(",");
        builder.append(this.patternsubj);
        builder.append(",");
        builder.append(this.patternobj);
        builder.append(",");
        builder.append(this.modal);
        builder.append(",");
        builder.append(this.cardinal);
        builder.append(",");
        builder.append(this.pronoun);
        builder.append(",");
        builder.append(this.adjective);
        builder.append(",");
        builder.append(this.adverb);
        builder.append(",");
        builder.append(this.strongA);
        builder.append(",");
        builder.append(this.weakA);
        builder.append(",");
        builder.append(this.patternsubjA);
        builder.append(",");
        builder.append(this.patternobjA);
        builder.append(",");
        builder.append(this.modalA);
        builder.append(",");
        builder.append(this.cardinalA);
        builder.append(",");
        builder.append(this.pronounA);
        builder.append(",");
        builder.append(this.adjectiveA);
        builder.append(",");
        builder.append(this.adverbA);
        builder.append(",");
        builder.append(this.c);
        return builder.toString();
    }
    
    public String toSparseNumericString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.c.equals("subj") ? "1 " : "-1 ");
        builder.append((this.strong > 0) ? ("1:" + this.strong + " ") : "");
        builder.append((this.weak > 0) ? ("2:" + this.weak + " ") : "");
        builder.append((this.patternsubj > 0) ? ("3:" + this.patternsubj + " ") : "");
        builder.append((this.patternobj > 0) ? ("4:" + this.patternobj + " ") : "");
        builder.append((this.modal > 0) ? ("5:" + this.modal + " ") : "");
        builder.append((this.cardinal > 0) ? ("6:" + this.cardinal + " ") : "");
        builder.append((this.pronoun > 0) ? ("7:" + this.pronoun + " ") : "");
        builder.append((this.adjective > 0) ? ("8:" + this.adjective + " ") : "");
        builder.append((this.adverb > 0) ? ("9:" + this.adverb + " ") : "");
        builder.append((this.strong > 0) ? ("10:" + this.strongA + " ") : "");
        builder.append((this.weak > 0) ? ("11:" + this.weakA + " ") : "");
        builder.append((this.patternsubj > 0) ? ("12:" + this.patternsubjA + " ") : "");
        builder.append((this.patternobj > 0) ? ("13:" + this.patternobjA + " ") : "");
        builder.append((this.modal > 0) ? ("14:" + this.modalA + " ") : "");
        builder.append((this.cardinal > 0) ? ("15:" + this.cardinalA + " ") : "");
        builder.append((this.pronoun > 0) ? ("16:" + this.pronounA + " ") : "");
        builder.append((this.adjective > 0) ? ("17:" + this.adjectiveA + " ") : "");
        builder.append((this.adverb > 0) ? ("18:" + this.adverbA + " ") : "");
        return builder.toString();
    }
    
    public String getC() {
        return this.c;
    }
    
    public void setC(final String c) {
        this.c = c;
    }
    
    private String fromNumericToNominal(final int i, final int threshold) {
        if (i < threshold) {
            return "=" + i;
        }
        return ">=" + threshold;
    }
    
    public String getDocument() {
        return this.document;
    }
    
    public void setDocument(final String document) {
        this.document = document;
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
    
    public double getConfidence() {
        return this.confidence;
    }
    
    public void setConfidence(final double confidence) {
        this.confidence = confidence;
    }
    
    public String getDir() {
        return this.dir;
    }
    
    public void setDir(final String dir) {
        this.dir = dir;
    }
}
