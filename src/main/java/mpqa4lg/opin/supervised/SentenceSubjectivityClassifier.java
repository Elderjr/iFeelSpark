package mpqa4lg.opin.supervised;

import weka.classifiers.*;
import mpqa4lg.opin.config.*;
import mpqa4lg.opin.entity.*;
import mpqa4lg.opin.io.*;
import mpqa4lg.opin.logic.*;
import mpqa4lg.opin.supervised.entity.*;

import java.io.*;
import java.util.*;

import weka.core.*;

public class SentenceSubjectivityClassifier
{
    private static final HashSet<String> modalPOS;
    private static final HashSet<String> modalNOT;
    private static final HashSet<String> pronounPOS;
    private static final HashSet<String> adjectivePOS;
    private static final HashSet<String> adverbPOS;
    private static final HashSet<String> adverbNOT;
    private static final HashSet<String> cardinalPOS;
    private static final String SUBJ = "subj";
    private static final String OBJ = "obj";
    private static final String UNKNOWN = "unknown";
    public static final String PREDICTIONFILE = "sent_subj.txt";
    private Classifier cls;
    
    static {
        modalPOS = new HashSet<String>(Arrays.asList("MD"));
        modalNOT = new HashSet<String>(Arrays.asList("will"));
        pronounPOS = new HashSet<String>(Arrays.asList("PRP", "PRP$", "WP", "WP$", "PP", "PP$"));
        adjectivePOS = new HashSet<String>(Arrays.asList("JJ", "JJR", "JJS"));
        adverbPOS = new HashSet<String>(Arrays.asList("RB", "RBR", "RBS", "WRB"));
        adverbNOT = new HashSet<String>(Arrays.asList("n't", "not"));
        cardinalPOS = new HashSet<String>(Arrays.asList("CD"));
    }
    
    public SentenceSubjectivityClassifier(final Config c) {
        try {
            final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(c.getSubjModel()));
            this.cls = (Classifier)ois.readObject();
            ois.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("!!! Could not load subjectivity model");
            System.exit(-1);
        }
    }
    
    public void process(final Corpus corpus) {
        final ArrayList<Document> docs = corpus.getDocs();
        for (final Document doc : docs) {
            System.out.println("subjclassifier: processing " + doc.getTextFile().getParentFile().getName() + File.separator + doc.getTextFile().getName());
            this.classifyDocument(doc);
            this.writeClassification(corpus.getAnnotationFile("sent_subj.txt", doc), doc);
        }
    }
    
    private void writeClassification(final File outputFile, final Document doc) {
        final ArrayList<String> output = new ArrayList<String>();
        for (final Sentence s : doc.getSentences()) {
            output.add(String.valueOf(s.getSenID()) + "\t" + s.getSubjC());
        }
        WriterUtils.writeLines(outputFile, output);
    }
    
    private void classifyDocument(final Document doc) {
        final ArrayList<String> ids = new ArrayList<String>();
        final ArrayList<ArrayList<Feature>> docdata = new ArrayList<ArrayList<Feature>>();
        getSentenceFeaturesWithIds(doc, docdata, ids);
        if (docdata.size() != ids.size()) {
            System.out.println("!!! Inconsistent feature representation, exiting program");
            System.exit(-1);
        }
        final Instances sentenceDataForWeka = convert2Weka(docdata, ids);
        final HashMap<String, String> predictions = this.makePredictions(sentenceDataForWeka, ids);
        for (final Sentence s : doc.getSentences()) {
            if (predictions.containsKey(s.getSenID())) {
                s.setSubjC(predictions.get(s.getSenID()));
            }
            else {
                s.setSubjC("unknown");
            }
        }
    }
    
    private HashMap<String, String> makePredictions(final Instances instances, final ArrayList<String> ids) {
        final HashMap<String, String> output = new HashMap<String, String>();
        for (int i = 0; i < instances.numInstances(); ++i) {
            try {
                final double clsLabel = this.cls.classifyInstance(instances.instance(i));
                output.put(ids.get(i), instances.classAttribute().value((int)clsLabel));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return output;
    }
    
    private static void getSentenceFeaturesWithIds(final Document doc, final ArrayList<ArrayList<Feature>> docdata, final ArrayList<String> ids) {
        for (final Sentence s : doc.getSentences()) {
            final ArrayList<Feature> features = new ArrayList<Feature>();
            addInSentenceFeatures(s, features);
            docdata.add(features);
            ids.add(s.getSenID());
        }
        addInContextFeatures(docdata);
    }
    
    private static void addInSentenceFeatures(final Sentence sen, final ArrayList<Feature> features) {
        final HashMap<String, TreeMap<Span, ArrayList<Annotation>>> autoAnns = sen.getAutoAnns();
        final HashSet<Span> strongsubjclues = new HashSet<Span>();
        final HashSet<Span> weaksubjclues = new HashSet<Span>();
        for (final String key : autoAnns.keySet()) {
            if (AnnotationHandler.STRONGSUBJTYPES.contains(key)) {
            	//[4LG]
                strongsubjclues.addAll((Collection<? extends Span>)autoAnns.get(key).keySet());
            }
            else {
                if (!AnnotationHandler.WEAKSUBJTYPES.contains(key)) {
                    continue;
                }
            	//[4LG]
                weaksubjclues.addAll((Collection<? extends Span>)autoAnns.get(key).keySet());
            }
        }
        weaksubjclues.removeAll(strongsubjclues);
        features.add(new Feature(strongsubjclues.size(), "numeric"));
        features.add(new Feature(weaksubjclues.size(), "numeric"));
        features.add(new Feature(getSyntacticInfo(sen, SentenceSubjectivityClassifier.adjectivePOS), "numeric"));
        features.add(new Feature(getSyntacticInfo(sen, SentenceSubjectivityClassifier.adverbPOS, SentenceSubjectivityClassifier.adverbNOT), "numeric"));
        features.add(new Feature(getSyntacticInfo(sen, SentenceSubjectivityClassifier.modalPOS, SentenceSubjectivityClassifier.modalNOT), "numeric"));
        features.add(new Feature(getSyntacticInfo(sen, SentenceSubjectivityClassifier.pronounPOS), "numeric"));
        features.add(new Feature(getSyntacticInfo(sen, SentenceSubjectivityClassifier.cardinalPOS), "numeric"));
    }
    
    private static void addInContextFeatures(final ArrayList<ArrayList<Feature>> dataset) {
        final ArrayList<ArrayList<Feature>> incontext = new ArrayList<ArrayList<Feature>>();
        for (int i = 0; i < dataset.size(); ++i) {
            incontext.add(new ArrayList<Feature>());
        }
        if (dataset.size() > 1) {
            final int ftc = dataset.get(1).size();
            incontext.get(0).addAll(dataset.get(1).subList(0, ftc - 1));
            incontext.get(dataset.size() - 1).addAll(dataset.get(dataset.size() - 2).subList(0, ftc - 1));
        }
        for (int i = 1; i < dataset.size() - 1; ++i) {
            final ArrayList<Feature> incontextcur = incontext.get(i);
            final ArrayList<Feature> pre = dataset.get(i - 1);
            final ArrayList<Feature> fol = dataset.get(i + 1);
            for (int j = 0; j < pre.size() - 1; ++j) {
                incontextcur.add(new Feature((int)pre.get(j).getValue() + (int)fol.get(j).getValue(), "numeric"));
            }
        }
        for (int i = 0; i < dataset.size(); ++i) {
            dataset.get(i).addAll(incontext.get(i));
        }
    }
    
    private static int getSyntacticInfo(final Sentence sen, final HashSet<String> pos, final HashSet<String> not) {
        int count = 0;
        final HashMap<String, TreeMap<Span, Annotation>> gatedefaultAnns = sen.getGatedefaultAnns();
        if (gatedefaultAnns.containsKey("GATE_Token")) {
            final TreeMap<Span, Annotation> annotations = gatedefaultAnns.get("GATE_Token");
            for (final Span annspan : annotations.keySet()) {
                final Annotation ann = annotations.get(annspan);
                if (pos.contains(ann.getAttributes().get("category")) && !not.contains(ann.getAttributes().get("string").toLowerCase())) {
                    ++count;
                }
            }
        }
        return count;
    }
    
    private static int getSyntacticInfo(final Sentence sen, final HashSet<String> pos) {
        int count = 0;
        final HashMap<String, TreeMap<Span, Annotation>> gatedefaultAnns = sen.getGatedefaultAnns();
        if (gatedefaultAnns.containsKey("GATE_Token")) {
            final TreeMap<Span, Annotation> annotations = gatedefaultAnns.get("GATE_Token");
            for (final Span annspan : annotations.keySet()) {
                final Annotation ann = annotations.get(annspan);
                if (pos.contains(ann.getAttributes().get("category"))) {
                    ++count;
                }
            }
        }
        return count;
    }
    
    private static Instances convert2Weka(final ArrayList<ArrayList<Feature>> dataset, final ArrayList<String> ids) {
        final FastVector atts = new FastVector();
        final FastVector classVals = new FastVector();
        classVals.addElement((Object)"obj");
        classVals.addElement((Object)"subj");
        atts.addElement((Object)new Attribute("strongCount"));
        atts.addElement((Object)new Attribute("weakCount"));
        atts.addElement((Object)new Attribute("adjCount"));
        atts.addElement((Object)new Attribute("advCount"));
        atts.addElement((Object)new Attribute("modCount"));
        atts.addElement((Object)new Attribute("pronounCount"));
        atts.addElement((Object)new Attribute("cardinalCount"));
        atts.addElement((Object)new Attribute("c_strongCount"));
        atts.addElement((Object)new Attribute("c_weakCount"));
        atts.addElement((Object)new Attribute("c_adjCount"));
        atts.addElement((Object)new Attribute("c_advCount"));
        atts.addElement((Object)new Attribute("c_modCount"));
        atts.addElement((Object)new Attribute("c_pronounCount"));
        atts.addElement((Object)new Attribute("c_cardinalCount"));
        atts.addElement((Object)new Attribute("class", classVals));
        final Instances dataset_weka = new Instances("SentenceSubjectivityDataSet", atts, 0);
        for (final ArrayList<Feature> instance : dataset) {
            final double[] vals = new double[dataset_weka.numAttributes()];
            for (int i = 0; i < instance.size(); ++i) {
                final Feature feat = instance.get(i);
                final String featurevalue = (feat.getValue() == null) ? "?" : String.valueOf(feat.getValue());
                if (!featurevalue.equals("?")) {
                    vals[i] = Double.parseDouble(featurevalue);
                }
                else {
                    vals[i] = Instance.missingValue();
                }
            }
            vals[vals.length - 1] = Instance.missingValue();
            dataset_weka.add(new Instance(1.0, vals));
        }
        dataset_weka.setClassIndex(dataset_weka.numAttributes() - 1);
        return dataset_weka;
    }
}
