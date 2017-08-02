package mpqa4lg.opin.preprocessor;

import mpqa4lg.opin.config.*;
import mpqa4lg.opin.entity.*;
import mpqa4lg.opin.entity.Document;
import mpqa4lg.opin.io.*;
import mpqa4lg.opin.logic.AnnotationHandler;
import mpqa4lg.opin.preprocessor.entity.*;
import edu.stanford.nlp.tagger.maxent.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.process.*;

public class PreProcess
{
    private Config conf;
    private MaxentTagger tagger;
    public static final String GATEDEFAULTFILE = "gate_default";
    
    public PreProcess(final Config c) {
        this.conf = c;
        try {
            this.tagger = new MaxentTagger(this.conf.getStanfordModel().getAbsolutePath());
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("!!! Could not load stanford model, exiting program");
            System.exit(-1);
        }
    }
    
    public void process(final Corpus corpus) {
        final ArrayList<Document> docs = corpus.getDocs();
        for (final Document d : docs) {
            final File doc = d.getTextFile();
            System.out.println("preprocessor: processing " + doc.getParentFile().getName() + File.separator + doc.getName());
            final ArrayList<GateDefaultLine> gls = new ArrayList<GateDefaultLine>();
            final String docContent = ReaderUtils.readFileToString(doc, this.conf.getCharset());
            final StringReader sReader = new StringReader(docContent);
            final List<List<HasWord>> sentences = (List<List<HasWord>>)MaxentTagger.tokenizeText((Reader)sReader);
            for (final List<HasWord> sentence : sentences) {
                final List<TaggedWord> tSentence = (List<TaggedWord>)this.tagger.tagSentence((List)sentence);
                gls.addAll(this.getGateDefaultLines(tSentence, docContent));
            }
            WriterUtils.writeLinesWithNum(corpus.getAnnotationFile("gate_default", doc), gls);
        }
    }
    
    /**
     * [4LG]
     */
    public List<Annotation> process(String text) {

    	final String docContent = text;
    	final StringReader sReader = new StringReader(docContent);
    	final List<List<HasWord>> sentences = (List<List<HasWord>>)MaxentTagger.tokenizeText((Reader)sReader);
    	final List<GateDefaultLine> gls = new ArrayList<GateDefaultLine>();

    	for (final List<HasWord> sentence : sentences) {
    		final List<TaggedWord> tSentence = (List<TaggedWord>)this.tagger.tagSentence((List<HasWord>)sentence);
    		gls.addAll(this.getGateDefaultLines(tSentence, docContent));
    	}

    	List<Annotation> annotations = AnnotationHandler.convertGateDefaultLinesToAnnotations(gls);

    	return annotations;
    }

    private ArrayList<GateDefaultLine> getGateDefaultLines(final List<TaggedWord> tSentence, final String docContent) {
        final ArrayList<GateDefaultLine> output = new ArrayList<GateDefaultLine>();
        output.add(new GateDefaultLine(tSentence.get(0).beginPosition(), tSentence.get(tSentence.size() - 1).endPosition()));
        for (final TaggedWord tWord : tSentence) {
            final String word = docContent.substring(tWord.beginPosition(), tWord.endPosition());
            final String pos = tWord.tag();
            output.add(new GateDefaultLine(word, Morphology.lemmatizeStatic(new WordTag(word, pos)).lemma(), pos, tWord.beginPosition(), tWord.endPosition()));
        }
        return output;
    }
    
    private String formatPTB(final List<TaggedWord> sentence) {
        final StringBuilder sb = new StringBuilder();
        for (final TaggedWord word : sentence) {
            sb.append(" " + PTBTokenizer.ptbToken2Text(word.word()));
        }
        return PTBTokenizer.ptb2Text(sb.toString().trim());
    }
}
