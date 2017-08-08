package mpqa4lg.opin.supervised;

import weka.classifiers.*;
import mpqa4lg.opin.config.*;
import mpqa4lg.opin.entity.*;
import mpqa4lg.opin.io.*;
import mpqa4lg.opin.supervised.entity.*;

import java.io.*;
import java.util.*;

import weka.core.*;

public class ExpressionPolarityClassifier
{
    private static final HashSet<String> modalPOS;
    private static final HashSet<String> modalNOT;
    private static final HashSet<String> pronounPOS;
    private static final HashSet<String> adjectivePOS;
    private static final HashSet<String> adverbPOS;
    private static final HashSet<String> adverbNOT;
    private static final HashSet<String> cardinalPOS;
    public static final String PREDICTIONFILE = "exp_polarity.txt";
    private Classifier cls;
    private Instances header;
    private HashMap<String, Classifier> swsdmodels;
    private HashMap<String, Instances> swsdheaders;
    private int nclassindex;
    private int posclassindex;
    private int negclassindex;
    private Config config;
    
    static {
        modalPOS = new HashSet<String>(Arrays.asList("MD"));
        modalNOT = new HashSet<String>(Arrays.asList("will"));
        pronounPOS = new HashSet<String>(Arrays.asList("PRP", "PRP$", "WP", "WP$", "PP", "PP$"));
        adjectivePOS = new HashSet<String>(Arrays.asList("JJ", "JJR", "JJS"));
        adverbPOS = new HashSet<String>(Arrays.asList("RB", "RBR", "RBS", "WRB"));
        adverbNOT = new HashSet<String>(Arrays.asList("n't", "not"));
        cardinalPOS = new HashSet<String>(Arrays.asList("CD"));
    }
    
    public ExpressionPolarityClassifier(final Config c) {
        this.config = c;
        try {
            final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(c.getPolarityModel()));
            this.cls = (Classifier)ois.readObject();
            ois.close();
            (this.header = new Instances((Reader)new BufferedReader(new FileReader(c.getPolarityModelHeader())))).setClassIndex(this.header.numAttributes() - 1);
            this.nclassindex = this.header.classAttribute().indexOfValue("neutral");
            this.posclassindex = this.header.classAttribute().indexOfValue("positive");
            this.negclassindex = this.header.classAttribute().indexOfValue("negative");
            if (c.isRunSWSD()) {
                this.swsdmodels = new HashMap<String, Classifier>();
                this.swsdheaders = new HashMap<String, Instances>();
                final File[] files = c.getSwsdModelDir().listFiles();
                File[] array;
                for (int length = (array = files).length, i = 0; i < length; ++i) {
                    final File f = array[i];
                    if (f.getAbsolutePath().endsWith(".model")) {
                        final String cluename = f.getName().replace(".model", "");
                        final ObjectInputStream oi = new ObjectInputStream(new FileInputStream(f));
                        final Classifier swsdcls = (Classifier)oi.readObject();
                        oi.close();
                        this.swsdmodels.put(cluename, swsdcls);
                    }
                    else if (f.getAbsolutePath().endsWith(".arff")) {
                        final String cluename = f.getName().replace(".arff", "");
                        final Instances swsdheader = new Instances((Reader)new BufferedReader(new FileReader(f)));
                        swsdheader.setClassIndex(swsdheader.numAttributes() - 1);
                        this.swsdheaders.put(cluename, swsdheader);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("!!! Could not load model, exiting program!");
            System.exit(-1);
        }
    }
    
    public void process(final Corpus corpus) {
        final ArrayList<Document> docs = corpus.getDocs();
        for (final Document doc : docs) {
            System.out.println("polarityclassifier: processing " + doc.getTextFile().getParentFile().getName() + File.separator + doc.getTextFile().getName());
            final HashMap<String, String> predictions = this.classifyExpressions(doc);
            this.writeClassification(doc, corpus.getAnnotationFile("exp_polarity.txt", doc), predictions);
        }
    }
    
    /**
     * [4LG]
     */
    public Map<String, String> process(List<Sentence> sentences) {

    	Map<String, String> predictions = this.classifyExpressions(sentences);
        return predictions;
    }
    
    private void writeClassification(final Document doc, final File outputFile, final HashMap<String, String> predictions) {
        final TreeMap<Integer, String> output = new TreeMap<Integer, String>();
        for (final String key : predictions.keySet()) {
            final String label = predictions.get(key);
            final String[] tokens = key.split("_");
            final Integer spanstart = Integer.parseInt(tokens[tokens.length - 2]);
            final Integer spanend = Integer.parseInt(tokens[tokens.length - 1]);
            output.put(spanstart, String.valueOf(key) + "\t" + label);
            if (!label.equals("neutral")) {
                doc.addPolarExpression(new Span(spanstart, spanend), label);
            }
        }
        WriterUtils.writeLines(outputFile, output.values());
    }
    
    private HashMap<String, String> classifyExpressions(final Document doc) {
        final Object[] temp = this.getExpressionFeaturesWithIds(doc);
        final ArrayList<ArrayList<Feature>> rawdataset = (ArrayList<ArrayList<Feature>>)temp[0];
        final ArrayList<String> ids = (ArrayList<String>)temp[1];
        final Instances data = this.convert2Weka(rawdataset, this.header);
        HashMap<String, String> predictions = null;
        if (this.config.isRunSWSD()) {
            final Object[] swsdfeatureinfo = this.getSWSDFeaturesWithInfo(doc);
            final ArrayList<ArrayList<Feature>> swsdfeatures = (ArrayList<ArrayList<Feature>>)swsdfeatureinfo[0];
            final ArrayList<SWSDClueInfo> swsdinfo = (ArrayList<SWSDClueInfo>)swsdfeatureinfo[1];
            if (rawdataset.size() != swsdfeatures.size()) {
                System.out.println("Warning");
                System.exit(-1);
            }
            predictions = this.makePredictions(data, ids, swsdfeatures, swsdinfo);
        }
        else {
            predictions = this.makePredictions(data, ids);
        }
        return predictions;
    }
    
    /**
     * [4LG] from classifyExpressions(Document)
     */
    private HashMap<String, String> classifyExpressions(List<Sentence> sentences) {
        final Object[] temp = this.getExpressionFeaturesWithIds(sentences);
        final ArrayList<ArrayList<Feature>> rawdataset = (ArrayList<ArrayList<Feature>>)temp[0];
        final ArrayList<String> ids = (ArrayList<String>)temp[1];
        final Instances data = this.convert2Weka(rawdataset, this.header);
        HashMap<String, String> predictions = null;
        if (this.config.isRunSWSD()) {
            final Object[] swsdfeatureinfo = this.getSWSDFeaturesWithInfo(sentences);
            final ArrayList<ArrayList<Feature>> swsdfeatures = (ArrayList<ArrayList<Feature>>)swsdfeatureinfo[0];
            final ArrayList<SWSDClueInfo> swsdinfo = (ArrayList<SWSDClueInfo>)swsdfeatureinfo[1];
            if (rawdataset.size() != swsdfeatures.size()) {
                System.out.println("Warning");
                System.exit(-1);
            }
            predictions = this.makePredictions(data, ids, swsdfeatures, swsdinfo);
        }
        else {
            predictions = this.makePredictions(data, ids);
        }
        return predictions;
    }

    private Object[] getExpressionFeaturesWithIds(final Document doc) {
        final Object[] output = new Object[2];
        final HashMap<String, ArrayList<Feature>> senfeatures = this.createSentenceFeatures(doc);
        final ArrayList<ArrayList<Feature>> dataset = new ArrayList<ArrayList<Feature>>();
        final ArrayList<String> ids = new ArrayList<String>();
        for (final Sentence s : doc.getSentences()) {
            final TreeMap<Span, ArrayList<Annotation>> clues = new TreeMap<Span, ArrayList<Annotation>>();
            if (s.getAutoAnns().containsKey("weaksubj")) {
                clues.putAll(s.getAutoAnns().get("weaksubj"));
            }
            if (s.getAutoAnns().containsKey("strongsubj")) {
                clues.putAll(s.getAutoAnns().get("strongsubj"));
            }
            for (final Span span : clues.keySet()) {
                final ArrayList<Feature> features = new ArrayList<Feature>();
                this.addWordFeatures(span, clues, s, features);
                this.addPolarityFeatures(span, clues, s, features);
                this.addModificationFeatures(span, s, features);
                this.addSentenceFeatures(s.getSenID(), features, senfeatures);
                dataset.add(features);
                final String spanid = String.valueOf(doc.getDocID()) + "_" + span.getStart() + "_" + span.getEnd();
                ids.add(spanid);
            }
        }
        output[0] = dataset;
        output[1] = ids;
        return output;
    }
    
    /**
     * [4LG]
     * from getExpressionFeaturesWithIds(Document)
     */
    private Object[] getExpressionFeaturesWithIds(List<Sentence> sentences) {
        final Object[] output = new Object[2];
        final HashMap<String, ArrayList<Feature>> senfeatures = this.createSentenceFeatures(sentences);
        final ArrayList<ArrayList<Feature>> dataset = new ArrayList<ArrayList<Feature>>();
        final ArrayList<String> ids = new ArrayList<String>();
        for (final Sentence s : sentences) {
            final TreeMap<Span, ArrayList<Annotation>> clues = new TreeMap<Span, ArrayList<Annotation>>();
            if (s.getAutoAnns().containsKey("weaksubj")) {
                clues.putAll(s.getAutoAnns().get("weaksubj"));
            }
            if (s.getAutoAnns().containsKey("strongsubj")) {
                clues.putAll(s.getAutoAnns().get("strongsubj"));
            }
            for (final Span span : clues.keySet()) {
                final ArrayList<Feature> features = new ArrayList<Feature>();
                this.addWordFeatures(span, clues, s, features);
                this.addPolarityFeatures(span, clues, s, features);
                this.addModificationFeatures(span, s, features);
                this.addSentenceFeatures(s.getSenID(), features, senfeatures);
                dataset.add(features);
                final String spanid = /*String.valueOf(doc.getDocID()) +*/ "_" + span.getStart() + "_" + span.getEnd();
                ids.add(spanid);
            }
        }
        output[0] = dataset;
        output[1] = ids;
        return output;
    }
    
    private Object[] getSWSDFeaturesWithInfo(final Document doc) {
        final Object[] output = new Object[2];
        final ArrayList<ArrayList<Feature>> swsddata = new ArrayList<ArrayList<Feature>>();
        final ArrayList<SWSDClueInfo> swsdinfo = new ArrayList<SWSDClueInfo>();
        for (final Sentence s : doc.getSentences()) {
            final TreeMap<Span, Annotation> gatetokens = s.getGatedefaultAnns().get("GATE_Token");
            final HashMap<Span, Integer> spanindexmap = new HashMap<Span, Integer>();
            final HashMap<Integer, Span> indexspanmap = new HashMap<Integer, Span>();
            int i = 0;
            for (final Span sp : gatetokens.keySet()) {
                if (gatetokens.get(sp).getAttributes().get("string").matches("\\W+")) {
                    continue;
                }
                spanindexmap.put(sp, i);
                indexspanmap.put(i, sp);
                ++i;
            }
            final TreeMap<Span, ArrayList<Annotation>> clues = new TreeMap<Span, ArrayList<Annotation>>();
            if (s.getAutoAnns().containsKey("weaksubj")) {
                clues.putAll(s.getAutoAnns().get("weaksubj"));
            }
            if (s.getAutoAnns().containsKey("strongsubj")) {
                clues.putAll(s.getAutoAnns().get("strongsubj"));
            }
            for (final Span span : clues.keySet()) {
                final int index = spanindexmap.get(span);
                final Span[] window = new Span[8];
                for (int wi = 1; index - wi >= 0 && wi <= 4; ++wi) {
                    window[4 - wi] = indexspanmap.get(index - wi);
                }
                for (int wi = 1; index + wi < gatetokens.size() && wi <= 4; ++wi) {
                    window[4 + wi - 1] = indexspanmap.get(index + wi);
                }
                final ArrayList<Feature> features = new ArrayList<Feature>();
                features.add(new Feature(gatetokens.get(span).getAttributes().get("string").toLowerCase(), "nominal", "token"));
                features.add(new Feature(gatetokens.get(span).getAttributes().get("category"), "nominal", "pos"));
                boolean anotherClue = false;
                for (int k = 0; k < window.length; ++k) {
                    final Span windowspan = window[k];
                    features.add(new Feature((windowspan == null) ? null : gatetokens.get(windowspan).getAttributes().get("string").toLowerCase(), "nominal", "token" + k));
                    features.add(new Feature((windowspan == null) ? null : gatetokens.get(windowspan).getAttributes().get("category"), "nominal", "pos" + k));
                    if (windowspan != null && clues.containsKey(windowspan)) {
                        anotherClue = true;
                    }
                }
                swsddata.add(features);
                final String lemma = gatetokens.get(span).getAttributes().get("lemma").toLowerCase();
                String pos = gatetokens.get(span).getAttributes().get("category");
                pos = pos.substring(0, 1).toLowerCase();
                swsdinfo.add(new SWSDClueInfo(String.valueOf(lemma) + "#" + pos, anotherClue));
            }
        }
        output[0] = swsddata;
        output[1] = swsdinfo;
        return output;
    }
    
    /**
     * [4LG] from getSWSDFeaturesWithInfo(Document)
     */
    private Object[] getSWSDFeaturesWithInfo(List<Sentence> sentences) {
        final Object[] output = new Object[2];
        final ArrayList<ArrayList<Feature>> swsddata = new ArrayList<ArrayList<Feature>>();
        final ArrayList<SWSDClueInfo> swsdinfo = new ArrayList<SWSDClueInfo>();
        for (final Sentence s : sentences) {
            final TreeMap<Span, Annotation> gatetokens = s.getGatedefaultAnns().get("GATE_Token");
            final HashMap<Span, Integer> spanindexmap = new HashMap<Span, Integer>();
            final HashMap<Integer, Span> indexspanmap = new HashMap<Integer, Span>();
            int i = 0;
            for (final Span sp : gatetokens.keySet()) {
                if (gatetokens.get(sp).getAttributes().get("string").matches("\\W+")) {
                    continue;
                }
                spanindexmap.put(sp, i);
                indexspanmap.put(i, sp);
                ++i;
            }
            final TreeMap<Span, ArrayList<Annotation>> clues = new TreeMap<Span, ArrayList<Annotation>>();
            if (s.getAutoAnns().containsKey("weaksubj")) {
                clues.putAll(s.getAutoAnns().get("weaksubj"));
            }
            if (s.getAutoAnns().containsKey("strongsubj")) {
                clues.putAll(s.getAutoAnns().get("strongsubj"));
            }
            for (final Span span : clues.keySet()) {
                final int index = spanindexmap.get(span);
                final Span[] window = new Span[8];
                for (int wi = 1; index - wi >= 0 && wi <= 4; ++wi) {
                    window[4 - wi] = indexspanmap.get(index - wi);
                }
                for (int wi = 1; index + wi < gatetokens.size() && wi <= 4; ++wi) {
                    window[4 + wi - 1] = indexspanmap.get(index + wi);
                }
                final ArrayList<Feature> features = new ArrayList<Feature>();
                features.add(new Feature(gatetokens.get(span).getAttributes().get("string").toLowerCase(), "nominal", "token"));
                features.add(new Feature(gatetokens.get(span).getAttributes().get("category"), "nominal", "pos"));
                boolean anotherClue = false;
                for (int k = 0; k < window.length; ++k) {
                    final Span windowspan = window[k];
                    features.add(new Feature((windowspan == null) ? null : gatetokens.get(windowspan).getAttributes().get("string").toLowerCase(), "nominal", "token" + k));
                    features.add(new Feature((windowspan == null) ? null : gatetokens.get(windowspan).getAttributes().get("category"), "nominal", "pos" + k));
                    if (windowspan != null && clues.containsKey(windowspan)) {
                        anotherClue = true;
                    }
                }
                swsddata.add(features);
                final String lemma = gatetokens.get(span).getAttributes().get("lemma").toLowerCase();
                String pos = gatetokens.get(span).getAttributes().get("category");
                pos = pos.substring(0, 1).toLowerCase();
                swsdinfo.add(new SWSDClueInfo(String.valueOf(lemma) + "#" + pos, anotherClue));
            }
        }
        output[0] = swsddata;
        output[1] = swsdinfo;
        return output;
    }
    
    private HashMap<String, ArrayList<Feature>> createSentenceFeatures(final Document doc) {
        final HashMap<String, ArrayList<Feature>> dataset = new HashMap<String, ArrayList<Feature>>();
        final ArrayList<ArrayList<Feature>> docdata = new ArrayList<ArrayList<Feature>>();
        final ArrayList<String> ids = new ArrayList<String>();
        for (final Sentence s : doc.getSentences()) {
            final ArrayList<Feature> features = new ArrayList<Feature>();
            this.addInSentenceFeatures(s, doc, features);
            docdata.add(features);
            ids.add(s.getSenID());
        }
        this.addInContextFeatures(docdata);
        for (int i = 0; i < docdata.size(); ++i) {
            dataset.put(ids.get(i), docdata.get(i));
        }
        return dataset;
    }
    
    /**
     * [4LG] from createSentenceFeatures(Document)
     */
    private HashMap<String, ArrayList<Feature>> createSentenceFeatures(List<Sentence> sentences) {
        final HashMap<String, ArrayList<Feature>> dataset = new HashMap<String, ArrayList<Feature>>();
        final ArrayList<ArrayList<Feature>> docdata = new ArrayList<>();
        final ArrayList<String> ids = new ArrayList<String>();
        for (final Sentence s : sentences) {
            final ArrayList<Feature> features = new ArrayList<Feature>();
            this.addInSentenceFeatures(s, features);
            docdata.add(features);
            ids.add(s.getSenID());
        }
        this.addInContextFeatures(docdata);
        for (int i = 0; i < docdata.size(); ++i) {
            dataset.put(ids.get(i), docdata.get(i));
        }
        return dataset;
    }
    
    private void addSentenceFeatures(final String sentenceid, final ArrayList<Feature> features, final HashMap<String, ArrayList<Feature>> senfeatures) {
        features.addAll(senfeatures.get(sentenceid));
    }
    
    private void addInSentenceFeatures(Sentence sen, Document doc, ArrayList<Feature> features) {
        HashMap<String, TreeMap<Span, ArrayList<Annotation>>> autoAnns = sen.getAutoAnns();
        HashSet<Span> strongsubjclues = new HashSet<Span>();
        HashSet<Span> weaksubjclues = new HashSet<Span>();
        if (autoAnns.containsKey("strongsubj")) {
            strongsubjclues.addAll(autoAnns.get("strongsubj").keySet());
        }
        if (autoAnns.containsKey("weaksubj")) {
            weaksubjclues.addAll(autoAnns.get("weaksubj").keySet());
        }
        weaksubjclues.removeAll(strongsubjclues);
        features.add(new Feature(String.valueOf(strongsubjclues.size()), "numeric", "strC"));
        features.add(new Feature(String.valueOf(weaksubjclues.size()), "numeric", "weakC"));
        features.add(new Feature(this.getSyntacticInfo(sen, adjectivePOS), "numeric", "adjC"));
        features.add(new Feature(this.getSyntacticInfo(sen, adverbPOS, adverbNOT), "numeric", "advC"));
        features.add(new Feature(this.getSyntacticInfo(sen, modalPOS, modalNOT), "numeric", "modalC"));
        features.add(new Feature(this.getSyntacticInfo(sen, pronounPOS), "numeric", "pronounC"));
        features.add(new Feature(this.getSyntacticInfo(sen, cardinalPOS), "numeric", "cardinalC"));
    }

    /**
     * [4LG] from addInSentenceFeatures(Sentence, Document, ArrayList) 
     */
    private void addInSentenceFeatures(final Sentence sen, final ArrayList<Feature> features) {
        final Map<String, TreeMap<Span, ArrayList<Annotation>>> autoAnns = sen.getAutoAnns();
        final Set<Span> strongsubjclues = new HashSet<Span>();
        final Set<Span> weaksubjclues = new HashSet<Span>();
        if (autoAnns.containsKey("strongsubj")) {
            strongsubjclues.addAll((Collection<? extends Span>)autoAnns.get("strongsubj").keySet());
        }
        if (autoAnns.containsKey("weaksubj")) {
            weaksubjclues.addAll((Collection<? extends Span>)autoAnns.get("weaksubj").keySet());
        }
        weaksubjclues.removeAll(strongsubjclues);
        features.add(new Feature(String.valueOf(strongsubjclues.size()), "numeric", "strC"));
        features.add(new Feature(String.valueOf(weaksubjclues.size()), "numeric", "weakC"));
        features.add(new Feature(this.getSyntacticInfo(sen, ExpressionPolarityClassifier.adjectivePOS), "numeric", "adjC"));
        features.add(new Feature(this.getSyntacticInfo(sen, ExpressionPolarityClassifier.adverbPOS, ExpressionPolarityClassifier.adverbNOT), "numeric", "advC"));
        features.add(new Feature(this.getSyntacticInfo(sen, ExpressionPolarityClassifier.modalPOS, ExpressionPolarityClassifier.modalNOT), "numeric", "modalC"));
        features.add(new Feature(this.getSyntacticInfo(sen, ExpressionPolarityClassifier.pronounPOS), "numeric", "pronounC"));
        features.add(new Feature(this.getSyntacticInfo(sen, ExpressionPolarityClassifier.cardinalPOS), "numeric", "cardinalC"));
    }
    
    private void addInContextFeatures(final ArrayList<ArrayList<Feature>> dataset) {
        final ArrayList<ArrayList<Feature>> incontext = new ArrayList<ArrayList<Feature>>();
        for (int i = 0; i < dataset.size(); ++i) {
            incontext.add(new ArrayList<Feature>());
        }
        for (int i = 0; i < dataset.size(); ++i) {
            final ArrayList<Feature> incontextcur = incontext.get(i);
            if (i != 0) {
                final ArrayList<Feature> pre = dataset.get(i - 1);
                incontextcur.add(new Feature(pre.get(0).getValue(), "numeric", String.valueOf(pre.get(0).getName()) + "P"));
                incontextcur.add(new Feature(pre.get(1).getValue(), "numeric", String.valueOf(pre.get(1).getName()) + "P"));
            }
            else {
                incontextcur.add(new Feature("0", "numeric", String.valueOf(dataset.get(i).get(0).getName()) + "P"));
                incontextcur.add(new Feature("0", "numeric", String.valueOf(dataset.get(i).get(1).getName()) + "P"));
            }
            if (i != dataset.size() - 1) {
                final ArrayList<Feature> fol = dataset.get(i + 1);
                incontextcur.add(new Feature(fol.get(0).getValue(), "numeric", String.valueOf(fol.get(0).getName()) + "F"));
                incontextcur.add(new Feature(fol.get(1).getValue(), "numeric", String.valueOf(fol.get(1).getName()) + "F"));
            }
            else {
                incontextcur.add(new Feature("0", "numeric", String.valueOf(dataset.get(i).get(0).getName()) + "F"));
                incontextcur.add(new Feature("0", "numeric", String.valueOf(dataset.get(i).get(1).getName()) + "F"));
            }
        }
        for (int i = 0; i < dataset.size(); ++i) {
            dataset.get(i).addAll(incontext.get(i));
        }
    }
    
    private void addModificationFeatures(final Span span, final Sentence s, final ArrayList<Feature> features) {
        final TreeMap<Span, Annotation> gatetokens = s.getGatedefaultAnns().get("GATE_Token");
        final HashMap<Span, Integer> spanindexmap = new HashMap<Span, Integer>();
        final HashMap<Integer, Span> indexspanmap = new HashMap<Integer, Span>();
        int i = 0;
        for (final Span sp : gatetokens.keySet()) {
            spanindexmap.put(sp, i);
            indexspanmap.put(i, sp);
            ++i;
        }
        final int index = spanindexmap.get(span);
        final Span previousspan = (index > 0) ? indexspanmap.get(index - 1) : null;
        final Span folspan = (index < gatetokens.size() - 1) ? indexspanmap.get(index + 1) : null;
        final HashSet<Span> leftwindow = new HashSet<Span>();
        for (i = index - 1; i > 0 & i >= index - 4; --i) {
            leftwindow.add(indexspanmap.get(i));
        }
        final HashSet<Span> rightwindow = new HashSet<Span>();
        for (i = index + 1; i < gatetokens.size() - 1 & i <= index + 4; ++i) {
            rightwindow.add(indexspanmap.get(i));
        }
        if (s.getAutoAnns().containsKey("intensifier")) {
            features.add(new Feature(s.getAutoAnns().get("intensifier").containsKey(span) ? "1" : "0", "numeric", "isIns"));
        }
        else {
            features.add(new Feature("0", "numeric", "isIns"));
        }
        if (previousspan != null) {
            features.add(new Feature(this.getSyntacticInfoPerAnn(gatetokens.get(previousspan), ExpressionPolarityClassifier.adjectivePOS), "numeric", "preAdj"));
            features.add(new Feature(this.getSyntacticInfoPerAnn(gatetokens.get(previousspan), ExpressionPolarityClassifier.adverbPOS, ExpressionPolarityClassifier.adverbNOT), "numeric", "preAdv"));
            if (s.getAutoAnns().containsKey("intensifier")) {
                final HashSet<Span> tmp = new HashSet<Span>(s.getAutoAnns().get("intensifier").keySet());
                tmp.retainAll(leftwindow);
                features.add(new Feature((tmp.size() > 0) ? "1" : "0", "numeric", "preIns"));
            }
            else {
                features.add(new Feature("0", "numeric", "preIns"));
            }
            if (s.getAutoAnns().containsKey("strongsubj")) {
                final HashSet<Span> tmp = new HashSet<Span>(s.getAutoAnns().get("strongsubj").keySet());
                tmp.retainAll(leftwindow);
                features.add(new Feature((tmp.size() > 0) ? "1" : "0", "numeric", "preStr"));
            }
            else {
                features.add(new Feature("0", "numeric", "preStr"));
            }
            if (s.getAutoAnns().containsKey("weaksubj")) {
                final HashSet<Span> tmp = new HashSet<Span>(s.getAutoAnns().get("weaksubj").keySet());
                tmp.retainAll(leftwindow);
                features.add(new Feature((tmp.size() > 0) ? "1" : "0", "numeric", "preWeak"));
            }
            else {
                features.add(new Feature("0", "numeric", "preWeak"));
            }
        }
        else {
            features.add(new Feature("0", "numeric", "preAdj"));
            features.add(new Feature("0", "numeric", "preAdv"));
            features.add(new Feature("0", "numeric", "preIns"));
            features.add(new Feature("0", "numeric", "preStr"));
            features.add(new Feature("0", "numeric", "preWeak"));
        }
        if (folspan != null) {
            features.add(new Feature(this.getSyntacticInfoPerAnn(gatetokens.get(folspan), ExpressionPolarityClassifier.adjectivePOS), "numeric", "folAdj"));
            features.add(new Feature(this.getSyntacticInfoPerAnn(gatetokens.get(folspan), ExpressionPolarityClassifier.adverbPOS, ExpressionPolarityClassifier.adverbNOT), "numeric", "folAdv"));
            if (s.getAutoAnns().containsKey("intensifier")) {
                final HashSet<Span> tmp = new HashSet<Span>(s.getAutoAnns().get("intensifier").keySet());
                tmp.retainAll(rightwindow);
                features.add(new Feature((tmp.size() > 0) ? "1" : "0", "numeric", "folIns"));
            }
            else {
                features.add(new Feature("0", "numeric", "folIns"));
            }
            if (s.getAutoAnns().containsKey("strongsubj")) {
                final HashSet<Span> tmp = new HashSet<Span>(s.getAutoAnns().get("strongsubj").keySet());
                tmp.retainAll(rightwindow);
                features.add(new Feature((tmp.size() > 0) ? "1" : "0", "numeric", "folStr"));
            }
            else {
                features.add(new Feature("0", "numeric", "folStr"));
            }
            if (s.getAutoAnns().containsKey("weaksubj")) {
                final HashSet<Span> tmp = new HashSet<Span>(s.getAutoAnns().get("weaksubj").keySet());
                tmp.retainAll(rightwindow);
                features.add(new Feature((tmp.size() > 0) ? "1" : "0", "numeric", "folWeak"));
            }
            else {
                features.add(new Feature("0", "numeric", "folWeak"));
            }
        }
        else {
            features.add(new Feature("0", "numeric", "folAdj"));
            features.add(new Feature("0", "numeric", "folAdv"));
            features.add(new Feature("0", "numeric", "folIns"));
            features.add(new Feature("0", "numeric", "folStr"));
            features.add(new Feature("0", "numeric", "folWeak"));
        }
    }
    
    private void addWordFeatures(final Span span, final TreeMap<Span, ArrayList<Annotation>> clues, final Sentence s, final ArrayList<Feature> features) {
        final TreeMap<Span, Annotation> gatetokens = s.getGatedefaultAnns().get("GATE_Token");
        final HashMap<Span, Integer> spanindexmap = new HashMap<Span, Integer>();
        final HashMap<Integer, Span> indexspanmap = new HashMap<Integer, Span>();
        int i = 0;
        for (final Span sp : gatetokens.keySet()) {
            spanindexmap.put(sp, i);
            indexspanmap.put(i, sp);
            ++i;
        }
        features.add(new Feature(gatetokens.get(span).getAttributes().get("string"), "nominal", "token"));
        features.add(new Feature(gatetokens.get(span).getAttributes().get("category"), "nominal", "pos"));
        final int index = spanindexmap.get(span);
        final String previousword = (index > 0) ? gatetokens.get(indexspanmap.get(index - 1)).getAttributes().get("string") : null;
        final String followingword = (index < gatetokens.size() - 1) ? gatetokens.get(indexspanmap.get(index + 1)).getAttributes().get("string") : null;
        features.add(new Feature(previousword, "nominal", "prevtoken"));
        features.add(new Feature(followingword, "nominal", "foltoken"));
        if (clues.get(span).get(0).getAttributes().containsKey("mpqapolarity")) {
            features.add(new Feature(reducePolarity(clues.get(span).get(0).getAttributes().get("mpqapolarity")), "nominal", "pripol"));
        }
        else {
            features.add(new Feature("neutral", "nominal", "pripol"));
        }
        features.add(new Feature(clues.get(span).get(0).getAttributes().get("type"), "nominal", "type"));
    }
    
    private void addPolarityFeatures(final Span span, final TreeMap<Span, ArrayList<Annotation>> clues, final Sentence s, final ArrayList<Feature> features) {
        final TreeMap<Span, Annotation> gatetokens = s.getGatedefaultAnns().get("GATE_Token");
        final HashMap<Span, Integer> spanindexmap = new HashMap<Span, Integer>();
        final HashMap<Integer, Span> indexspanmap = new HashMap<Integer, Span>();
        int i = 0;
        for (final Span sp : gatetokens.keySet()) {
            spanindexmap.put(sp, i);
            indexspanmap.put(i, sp);
            ++i;
        }
        final int index = spanindexmap.get(span);
        final HashSet<Span> preSpans = new HashSet<Span>();
        for (int j = (index - 4 >= 0) ? (index - 4) : 0; j < index; ++j) {
            preSpans.add(indexspanmap.get(j));
        }
        final Set<Span> negations = (s.getAutoAnns().get("negated") != null) ? s.getAutoAnns().get("negated").keySet() : new HashSet<Span>();
        final Set<Span> genshifter = (s.getAutoAnns().get("genshifter") != null) ? s.getAutoAnns().get("genshifter").keySet() : new HashSet<Span>();
        final Set<Span> shiftneg = (s.getAutoAnns().get("shiftneg") != null) ? s.getAutoAnns().get("shiftneg").keySet() : new HashSet<Span>();
        final Set<Span> shiftpos = (s.getAutoAnns().get("shiftpos") != null) ? s.getAutoAnns().get("shiftpos").keySet() : new HashSet<Span>();
        HashSet<Span> tmp = new HashSet<Span>(preSpans);
        tmp.retainAll(negations);
        if (negations.size() > 0 && tmp.size() > 0) {
            features.add(new Feature("1", "numeric", "negated"));
        }
        else {
            features.add(new Feature("0", "numeric", "negated"));
        }
        tmp = new HashSet<Span>(preSpans);
        tmp.retainAll(genshifter);
        if (genshifter.size() > 0 && tmp.size() > 0) {
            features.add(new Feature("1", "numeric", "genshifter"));
        }
        else {
            features.add(new Feature("0", "numeric", "genshifter"));
        }
        tmp = new HashSet<Span>(preSpans);
        tmp.retainAll(shiftneg);
        if (shiftneg.size() > 0 && tmp.size() > 0) {
            features.add(new Feature("1", "numeric", "shiftneg"));
        }
        else {
            features.add(new Feature("0", "numeric", "shiftneg"));
        }
        tmp = new HashSet<Span>(preSpans);
        tmp.retainAll(shiftpos);
        if (shiftpos.size() > 0 && tmp.size() > 0) {
            features.add(new Feature("1", "numeric", "shiftpos"));
        }
        else {
            features.add(new Feature("0", "numeric", "shiftpos"));
        }
        final Span previousspan = (index > 0) ? indexspanmap.get(index - 1) : null;
        final Span folspan = (index < gatetokens.size() - 1) ? indexspanmap.get(index - 1) : null;
        if (previousspan != null) {
            features.add(new Feature(clues.containsKey(previousspan) ? reducePolarity(clues.get(previousspan).get(0).getAttributes().get("mpqapolarity")) : "neutral", "nominal", "prePol"));
        }
        else {
            features.add(new Feature("notmod", "nominal", "prePol"));
        }
        if (folspan != null) {
            features.add(new Feature(clues.containsKey(folspan) ? reducePolarity(clues.get(folspan).get(0).getAttributes().get("mpqapolarity")) : "neutral", "nominal", "folPol"));
        }
        else {
            features.add(new Feature("notmod", "nominal", "folPol"));
        }
    }
    
    private static String reducePolarity(final String p) {
        if (p.contains("neutral")) {
            return "neutral";
        }
        if (p.contains("pos")) {
            return "positive";
        }
        return "negative";
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
    
    private HashMap<String, String> makePredictions(final Instances instances, final ArrayList<String> ids, final ArrayList<ArrayList<Feature>> swsdfeatures, final ArrayList<SWSDClueInfo> swsdinfo) {
        final HashMap<String, String> output = new HashMap<String, String>();
        for (int i = 0; i < instances.numInstances(); ++i) {
            try {
                final double[] dist = this.cls.distributionForInstance(instances.instance(i));
                final double polarconf = dist[this.negclassindex] + dist[this.posclassindex];
                final double neutralconf = dist[this.nclassindex];
                String predictedLabel = null;
                if (Math.max(polarconf, neutralconf) < 0.7 && this.swsdmodels.containsKey(swsdinfo.get(i).getClue())) {
                    final TreeMap<Double, String> scorelabelmap = new TreeMap<Double, String>();
                    scorelabelmap.put(dist[this.nclassindex], "neutral");
                    scorelabelmap.put(dist[this.posclassindex], "positive");
                    scorelabelmap.put(dist[this.negclassindex], "negative");
                    final TreeMap<Integer, String> ranklabelmap = new TreeMap<Integer, String>();
                    int rank = 1;
                    for (final Double d : scorelabelmap.descendingKeySet()) {
                        ranklabelmap.put(rank, scorelabelmap.get(d));
                        ++rank;
                    }
                    final String swsdpred = this.getSWSDPrediction(swsdfeatures.get(i), swsdinfo.get(i));
                    predictedLabel = ranklabelmap.get(1);
                    if ((predictedLabel.equals("negative") || predictedLabel.equals("positive")) && swsdpred.equals("obj")) {
                        if (!swsdinfo.get(i).isAnotherClue()) {
                            predictedLabel = "neutral";
                        }
                    }
                    else if (predictedLabel.equals("neutral") && swsdpred.equals("subj")) {
                        predictedLabel = ranklabelmap.get(2);
                    }
                }
                else {
                    final double clsLabel = this.cls.classifyInstance(instances.instance(i));
                    predictedLabel = instances.classAttribute().value((int)clsLabel);
                }
                output.put(ids.get(i), predictedLabel);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return output;
    }
    
    private String getSWSDPrediction(final ArrayList<Feature> features, final SWSDClueInfo clueinfo) {
        String output = null;
        final Classifier cls = this.swsdmodels.get(clueinfo.getClue());
        final Instances header = this.swsdheaders.get(clueinfo.getClue());
        final Instances swsdinstance = this.convertInstance2Weka(features, header);
        try {
            final double clsLabel = cls.classifyInstance(swsdinstance.instance(0));
            output = swsdinstance.classAttribute().value((int)clsLabel);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return output;
    }
    
    private int getSyntacticInfoPerAnn(final Annotation ann, final HashSet<String> pos, final HashSet<String> not) {
        if (pos.contains(ann.getAttributes().get("category")) && !not.contains(ann.getAttributes().get("string").toLowerCase())) {
            return 1;
        }
        return 0;
    }
    
    private int getSyntacticInfoPerAnn(final Annotation ann, final HashSet<String> pos) {
        if (pos.contains(ann.getAttributes().get("category"))) {
            return 1;
        }
        return 0;
    }
    
    private int getSyntacticInfo(final Sentence sen, final HashSet<String> pos, final HashSet<String> not) {
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
    
    private int getSyntacticInfo(final Sentence sen, final HashSet<String> pos) {
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
    
    private Instances convert2Weka(final ArrayList<ArrayList<Feature>> dataset, final Instances header) {
        final Instances dataset_weka = new Instances(header);
        for (final ArrayList<Feature> instance : dataset) {
            final double[] vals = new double[dataset_weka.numAttributes()];
            for (int i = 0; i < instance.size(); ++i) {
                final Feature feat = instance.get(i);
                final Object value = feat.getValue();
                if (value != null) {
                    final Attribute attribute = dataset_weka.attribute(i);
                    final String featurevalue = String.valueOf(value);
                    if (dataset_weka.attribute(i).isNominal()) {
                        final int indexValue = attribute.indexOfValue(Utils.quote(featurevalue));
                        if (indexValue == -1) {
                            vals[i] = Instance.missingValue();
                        }
                        else {
                            vals[i] = indexValue;
                        }
                    }
                    else {
                        vals[i] = Double.parseDouble(featurevalue);
                    }
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
    
    private Instances convertInstance2Weka(final ArrayList<Feature> instance, final Instances header) {
        final Instances dataset_weka = new Instances(header);
        final double[] vals = new double[dataset_weka.numAttributes()];
        for (int i = 0; i < instance.size(); ++i) {
            final Feature feat = instance.get(i);
            final Object value = feat.getValue();
            if (value != null) {
                final Attribute attribute = dataset_weka.attribute(i);
                final String featurevalue = String.valueOf(value);
                if (dataset_weka.attribute(i).isNominal()) {
                    final int indexValue = attribute.indexOfValue(Utils.quote(featurevalue));
                    if (indexValue == -1) {
                        vals[i] = Instance.missingValue();
                    }
                    else {
                        vals[i] = indexValue;
                    }
                }
                else {
                    vals[i] = Double.parseDouble(featurevalue);
                }
            }
            else {
                vals[i] = Instance.missingValue();
            }
        }
        vals[vals.length - 1] = Instance.missingValue();
        dataset_weka.add(new Instance(1.0, vals));
        dataset_weka.setClassIndex(dataset_weka.numAttributes() - 1);
        return dataset_weka;
    }
}
