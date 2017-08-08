package mpqa4lg.opin.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mpqa4lg.opin.config.Config;
import mpqa4lg.opin.entity.Annotation;
import mpqa4lg.opin.entity.Corpus;
import mpqa4lg.opin.entity.Document;
import mpqa4lg.opin.entity.Sentence;
import mpqa4lg.opin.featurefinder.entity.AutoAnnLine;
import mpqa4lg.opin.io.ReaderUtils;
import mpqa4lg.opin.preprocessor.entity.GateDefaultLine;

public class AnnotationHandler
{
    private static final Pattern LINETOKENIZER;
    private static final Pattern ATTRIBUTETOKENIZER;
    public static final HashSet<String> STRONGSUBJTYPES;
    public static final HashSet<String> WEAKSUBJTYPES;
    public static final String STRONGSUBJ = "strongsubj";
    public static final String WEAKSUBJ = "weaksubj";
    private Config conf;
    
    static {
        LINETOKENIZER = Pattern.compile("\\s+(?=([^\"]*?(=\"|$)))");
        ATTRIBUTETOKENIZER = Pattern.compile("(\\S+)=\"(.*)\"");
        STRONGSUBJTYPES = new HashSet<String>(Arrays.asList("basilisk-2000nouns-strongsubj", "basilisk-2000nouns-weaksubj", "metaboot-2000nouns-strongsubj", "bl_desire_verb", "fn_emotion_experiencer-subj_v", " bl_judge_verb", " fn_emotion_experiencer-obj_v", " fn_emotion_heat_v", " fn_emotion_emotion_active_v", " fixed4gram", " fn_emotion_directed_a", " ballmerLen1", " ballmerLen2", " ballmerLen3", " trainJWman", " fn_emotion_directed_a", " strongjandiss", "fn_perception_body_v", "jwman", "PolPmanstrong", "PolMmanstrong"));
        WEAKSUBJTYPES = new HashSet<String>(Arrays.asList("bl_psych_verb", "PolMmanweak", "metaboot-2000nouns-weaksubj", "weakjandiss", "PolPmanweak"));
    }
    
    public AnnotationHandler(final Config c) {
        this.conf = c;
    }
    
    public static ArrayList<Annotation> readAnns(final File annotationFile) {
        final ArrayList<String> annoLines = ReaderUtils.readFileToLinesIgnoreEmptyAndComments(annotationFile);
        final ArrayList<Annotation> output = new ArrayList<Annotation>();
        for (final String annoLine : annoLines) {
            final String[] tokens = AnnotationHandler.LINETOKENIZER.split(annoLine);
            final String[] parts = tokens[1].split(",");
            final Annotation currentAnn = new Annotation(tokens[2], Integer.parseInt(tokens[0]), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            for (int i = 3; i < tokens.length; ++i) {
                final Matcher matcher = AnnotationHandler.ATTRIBUTETOKENIZER.matcher(tokens[i]);
                if (matcher.matches()) {
                    currentAnn.addToAttributes(matcher.group(1), matcher.group(2));
                }
                else {
                    System.out.println("Attribute is not well formed! In file:" + annotationFile.getAbsolutePath() + " at " + tokens[i]);
                }
            }
            output.add(currentAnn);
        }
        return output;
    }

    /**
     * [4LG] from readAnss(File)
     */
    public static List<Annotation> convertGateDefaultLinesToAnnotations(List<GateDefaultLine> annoLines) {

        final List<Annotation> output = new ArrayList<Annotation>();
        int id = 0;
        for (GateDefaultLine gdl : annoLines) {
        	String annoLine = gdl.toString();
            final String[] tokens = AnnotationHandler.LINETOKENIZER.split(annoLine);
            final String[] parts = tokens[0].split(",");
            final Annotation currentAnn = new Annotation(tokens[1], ++id, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            for (int i = 2; i < tokens.length; ++i) {
                final Matcher matcher = AnnotationHandler.ATTRIBUTETOKENIZER.matcher(tokens[i]);
                if (matcher.matches()) {
                    currentAnn.addToAttributes(matcher.group(1), matcher.group(2));
                }
            }
            output.add(currentAnn);
        }
        return output;
    }

    public static void readAnns(final File annotationFile, final ArrayList<Annotation> annotations) {
        final ArrayList<String> annoLines = ReaderUtils.readFileToLinesIgnoreEmptyAndComments(annotationFile);
        for (final String annoLine : annoLines) {
            final String[] tokens = AnnotationHandler.LINETOKENIZER.split(annoLine);
            final String[] parts = tokens[1].split(",");
            final Annotation currentAnn = new Annotation(tokens[2], Integer.parseInt(tokens[0]), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            for (int i = 3; i < tokens.length; ++i) {
                final Matcher matcher = AnnotationHandler.ATTRIBUTETOKENIZER.matcher(tokens[i]);
                if (matcher.matches()) {
                    currentAnn.addToAttributes(matcher.group(1), matcher.group(2));
                }
                else {
                    System.out.println("Attribute is not well formed! In file:" + annotationFile.getAbsolutePath() + " at " + tokens[i]);
                }
            }
            annotations.add(currentAnn);
        }
    }
    
	/**
	 * [4LG] from readAnss(File, ArrayList)
	 */
	public static List<Annotation> convertAutoAnnLinesToAnnotations(List<AutoAnnLine> annoLines) {

		List<Annotation> annotations = new ArrayList<>();
		int id = 0;
		for (final AutoAnnLine annoLine : annoLines) {
			final String[] tokens = AnnotationHandler.LINETOKENIZER.split(annoLine.toString());
			final String[] parts = tokens[0].split(",");
			final Annotation currentAnn = new Annotation(tokens[1], ++id, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
			for (int i = 2; i < tokens.length; ++i) {
				final Matcher matcher = AnnotationHandler.ATTRIBUTETOKENIZER.matcher(tokens[i]);
				if (matcher.matches()) {
					currentAnn.addToAttributes(matcher.group(1), matcher.group(2));
				}
			}
			annotations.add(currentAnn);
		}
		return annotations;
	}

    
    /**
     * @param autoAnns changed from ArrayList to List [4LG]
     */
    private void fillSentencesWithAutoAnns(final List<Annotation> autoAnns, final ArrayList<Sentence> sentences) {
        int index = 0;
        if (sentences.size() > 0) {
            final Iterator<Annotation> itr = autoAnns.iterator();
            while (itr.hasNext()) {
                Sentence sen;
                Annotation aann;
                for (sen = sentences.get(index), aann = itr.next(); aann.getSpanS() >= sen.getSpanE() && index < sentences.size() - 1; ++index, sen = sentences.get(index)) {}
                if (aann.getSpanS() >= sen.getSpanS() && aann.getSpanE() <= sen.getSpanE() && aann.getAttributes().containsKey("type")) {
                    final String anntype = aann.getAttributes().get("type");
                    sen.addToAutoAnns(anntype, aann);
                }
            }
        }
    }
    
    /**
     * @param gatedefaultAnns changed from ArrayList to List [4LG]
     */
    private void fillSentencesWithGatedefaultAnns(final List<Annotation> gatedefaultAnns, final ArrayList<Sentence> sentences) {
        int index = 0;
        if (sentences.size() > 0) {
            final Iterator<Annotation> itr = gatedefaultAnns.iterator();
            while (itr.hasNext()) {
                Sentence sen;
                Annotation gatedefault;
                for (sen = sentences.get(index), gatedefault = itr.next(); gatedefault.getSpanS() >= sen.getSpanE() && index < sentences.size() - 1; ++index, sen = sentences.get(index)) {}
                sen.addToGatedefaultAnns(gatedefault.getName(), gatedefault);
            }
        }
    }
    
    public void buildSentencesFromGateDefault(final Corpus corpus) {
        final ArrayList<Document> docs = corpus.getDocs();
        for (final Document doc : docs) {
            final File gatedefaultFile = corpus.getAnnotationFile("gate_default", doc);
            if (!gatedefaultFile.exists()) {
                System.out.println("!! gatedefault file does not exist, skipping document: " + gatedefaultFile.getAbsolutePath());
            }
            else {
                final ArrayList<Annotation> gatedefaultAnns = readAnns(gatedefaultFile);
                for (final Annotation ann : gatedefaultAnns) {
                    if (ann.getName().equals("GATE_Sentence")) {
                        doc.getSentences().add(new Sentence(ann.getSpanS(), ann.getSpanE(), doc.getDocID()));
                    }
                }
                this.fillSentencesWithGatedefaultAnns(gatedefaultAnns, doc.getSentences());
            }
        }
    }
    
	/**
	 * [4LG]
	 */
	public List<Sentence> buildSentencesFromGateDefault(List<Annotation> gateDefaultAnns) {

		ArrayList<Sentence> sentences = new ArrayList<>(); 
		for (final Annotation ann : gateDefaultAnns) {
			if (ann.getName().equals("GATE_Sentence")) {
				sentences.add(new Sentence(ann.getSpanS(), ann.getSpanE(), null));
			}
		}
		this.fillSentencesWithGatedefaultAnns(gateDefaultAnns, sentences);
		
		return sentences;
	}
    
    public void readInRequiredAnnotationsForRuleBased(final Corpus corpus) {
        final HashSet<String> annosToRead = new HashSet<String>();
        final String annoName = getAnnotationName(this.conf.getSubjLexicon());
        if (!corpus.hasAnnotation(annoName)) {
            annosToRead.add(annoName);
        }
        if (annosToRead.size() > 0) {
            this.readInAnnotations(corpus, annosToRead);
        }
    }
    
    public void readInRequiredAnnotationsForSubjClassifier(final Corpus corpus) {
        final HashSet<String> annosToRead = new HashSet<String>();
        final String annoName = getAnnotationName(this.conf.getSubjLexicon());
        if (!corpus.hasAnnotation(annoName)) {
            annosToRead.add(annoName);
        }
        if (annosToRead.size() > 0) {
            this.readInAnnotations(corpus, annosToRead);
        }
    }
    
    public void readInRequiredAnnotationsForPolarityClassifier(final Corpus corpus) {
        final HashSet<String> annosToRead = new HashSet<String>();
        String annoName = getAnnotationName(this.conf.getPolarityLexicon());
        if (!corpus.hasAnnotation(annoName)) {
            annosToRead.add(annoName);
        }
        annoName = getAnnotationName(this.conf.getIntensifierLexicon());
        if (!corpus.hasAnnotation(annoName)) {
            annosToRead.add(annoName);
        }
        annoName = getAnnotationName(this.conf.getValenceshifterLexicon());
        if (!corpus.hasAnnotation(annoName)) {
            annosToRead.add(annoName);
        }
        if (annosToRead.size() > 0) {
            this.readInAnnotations(corpus, annosToRead);
        }
    }
    
    /**
     * [4LG] from readInAnnotations
     */
	public void readInRequiredAnnotationsForPolarityClassifier(List<Sentence> sentences, final Map<String, List<Annotation>> mapClueAnnotations) {

		for (Entry<String, List<Annotation>> pair : mapClueAnnotations.entrySet()) {

			final List<Annotation> autoanns = pair.getValue();
			this.fillSentencesWithAutoAnns(autoanns, (ArrayList<Sentence>)sentences);
		}
	}

    private void readInAnnotations(final Corpus corpus, final HashSet<String> annosToRead) {
        final ArrayList<Document> docs = corpus.getDocs();
        for (final Document doc : docs) {
            final ArrayList<Annotation> autoanns = new ArrayList<Annotation>();
            for (final String a : annosToRead) {
                final File autoAnnFile = corpus.getAnnotationFile(a, doc);
                if (!autoAnnFile.exists()) {
                    System.out.println("!! annotation file does not exist, skipping document: " + autoAnnFile.getAbsolutePath());
                }
                else {
                    readAnns(autoAnnFile, autoanns);
                }
            }
            this.fillSentencesWithAutoAnns(autoanns, doc.getSentences());
        }
    }
    
    private void sortAnnsBySpanS(final ArrayList<Annotation> anns) {
        Collections.sort(anns, new AnnComparator());
    }
    
    private static String getAnnotationName(final File annoFile) {
        return annoFile.getName().replaceAll("\\.tff", "");
    }
    
    static class AnnComparator implements Comparator<Annotation>
    {
        @Override
        public int compare(final Annotation a1, final Annotation a2) {
            return a1.getSpanS() - a2.getSpanS();
        }
    }
}
