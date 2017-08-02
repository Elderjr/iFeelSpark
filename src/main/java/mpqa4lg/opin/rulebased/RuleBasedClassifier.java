package mpqa4lg.opin.rulebased;

import java.io.*;

import mpqa4lg.opin.entity.*;
import mpqa4lg.opin.io.*;
import mpqa4lg.opin.logic.*;

import java.util.*;

public class RuleBasedClassifier
{
    private static final String SUBJ = "subj";
    private static final String OBJ = "obj";
    private static final String UNKNOWN = "unknown";
    private static final int SIMPLEOBJTESTSTRONG = 2;
    private static final int SIMPLEOBJTESTWEAK = 3;
    public static final String PREDICTIONFILE = "sent_rule.txt";
    
    public void process(final Corpus corpus) {
        final ArrayList<Document> docs = corpus.getDocs();
        for (final Document doc : docs) {
            System.out.println("rulebased: processing " + doc.getTextFile().getParentFile().getName() + File.separator + doc.getTextFile().getName());
            this.classifyDocument(doc);
            this.writeClassification(corpus.getAnnotationFile("sent_rule.txt", doc), doc);
        }
    }
    
    private void writeClassification(final File outputFile, final Document doc) {
        final ArrayList<String> output = new ArrayList<String>();
        for (final Sentence s : doc.getSentences()) {
            output.add(String.valueOf(s.getSenID()) + "\t" + s.getHpC());
        }
        WriterUtils.writeLines(outputFile, output);
    }
    
    private void classifyDocument(final Document doc) {
        final ArrayList<Sentence> sentences = doc.getSentences();
        for (int i = 0; i < sentences.size(); ++i) {
            final Sentence current = sentences.get(i);
            final HashMap<String, TreeMap<Span, ArrayList<Annotation>>> currentAutoAnns = current.getAutoAnns();
            final HashSet<Span> strongsubjclues = new HashSet<Span>();
            final HashSet<Span> weaksubjclues = new HashSet<Span>();
            for (final String type : currentAutoAnns.keySet()) {
                if (AnnotationHandler.STRONGSUBJTYPES.contains(type)) {
                	//[4LG]
                    strongsubjclues.addAll((Collection<? extends Span>)currentAutoAnns.get(type).keySet());
                }
                if (AnnotationHandler.WEAKSUBJTYPES.contains(type)) {
                	//[4LG]
                    weaksubjclues.addAll((Collection<? extends Span>)currentAutoAnns.get(type).keySet());
                }
            }
            weaksubjclues.removeAll(strongsubjclues);
            if (strongsubjclues.size() > 1) {
                current.setHpC("subj");
            }
            else {
                int totalStrongCount = 0;
                int totalWeakCount = 0;
                int currentStrongCount = 0;
                currentStrongCount = strongsubjclues.size();
                totalWeakCount += weaksubjclues.size();
                if (i > 0) {
                    final Sentence prev = sentences.get(i - 1);
                    final HashMap<String, TreeMap<Span, ArrayList<Annotation>>> prevAutoAnns = prev.getAutoAnns();
                    final HashSet<Span> prevstrongsubjclues = new HashSet<Span>();
                    final HashSet<Span> prevweaksubjclues = new HashSet<Span>();
                    for (final String type2 : prevAutoAnns.keySet()) {
                        if (AnnotationHandler.STRONGSUBJTYPES.contains(type2)) {
                        	//[4LG]
                            prevstrongsubjclues.addAll((Collection<? extends Span>)prevAutoAnns.get(type2).keySet());
                        }
                        if (AnnotationHandler.WEAKSUBJTYPES.contains(type2)) {
                        	//[4LG]
                            prevweaksubjclues.addAll((Collection<? extends Span>)prevAutoAnns.get(type2).keySet());
                        }
                    }
                    prevweaksubjclues.removeAll(prevstrongsubjclues);
                    totalStrongCount += prevstrongsubjclues.size();
                    totalWeakCount += prevweaksubjclues.size();
                }
                if (i < sentences.size() - 1) {
                    final Sentence following = sentences.get(i + 1);
                    final HashMap<String, TreeMap<Span, ArrayList<Annotation>>> followingAutoAnns = following.getAutoAnns();
                    final HashSet<Span> followingstrongsubjclues = new HashSet<Span>();
                    final HashSet<Span> followingweaksubjclues = new HashSet<Span>();
                    for (final String type2 : followingAutoAnns.keySet()) {
                        if (AnnotationHandler.STRONGSUBJTYPES.contains(type2)) {
                        	//[4LG]
                            followingstrongsubjclues.addAll((Collection<? extends Span>)followingAutoAnns.get(type2).keySet());
                        }
                        if (AnnotationHandler.WEAKSUBJTYPES.contains(type2)) {
                        	//[4LG]
                            followingweaksubjclues.addAll((Collection<? extends Span>)followingAutoAnns.get(type2).keySet());
                        }
                    }
                    followingweaksubjclues.removeAll(followingstrongsubjclues);
                    totalStrongCount += followingstrongsubjclues.size();
                    totalWeakCount += followingweaksubjclues.size();
                }
                if (currentStrongCount == 0 && totalStrongCount < 2 && totalWeakCount < 3) {
                    current.setHpC("obj");
                }
                else {
                    current.setHpC("unknown");
                }
            }
        }
    }
}
