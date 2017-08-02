package mpqa4lg.opin.output;

import mpqa4lg.opin.entity.*;
import mpqa4lg.opin.io.*;

import java.io.*;
import java.util.*;

public class SGMLOutput
{
    private static String RuleBasedOutput;
    private static String SubjOutput;
    private static String PolarityOutput;
    public static String PREDICTIONFILE;
    private static boolean hasRuleBasedOutput;
    private static boolean hasSubjClassifierOutput;
    private static boolean hasPolarityClassifierOutput;
    
    static {
        SGMLOutput.PREDICTIONFILE = "markup.txt";
    }
    
    public SGMLOutput(final boolean isRunRulebasedClassifier, final boolean isRunSubjClassifier, final boolean isRunPolarityClassifier) {
        SGMLOutput.RuleBasedOutput = "sent_rule.txt";
        SGMLOutput.SubjOutput = "sent_subj.txt";
        SGMLOutput.PolarityOutput = "exp_polarity.txt";
        SGMLOutput.hasRuleBasedOutput = isRunRulebasedClassifier;
        SGMLOutput.hasSubjClassifierOutput = isRunSubjClassifier;
        SGMLOutput.hasPolarityClassifierOutput = isRunPolarityClassifier;
    }
    
    public void process(final Corpus corpus) {
        final ArrayList<Document> docs = corpus.getDocs();
        for (final Document doc : docs) {
            System.out.println("SGMLOutput: processing " + doc.getTextFile().getParentFile().getName() + File.separator + doc.getTextFile().getName());
            final String str = ReaderUtils.readFileToString(doc.getTextFile());
            String output = "";
            final SortedMap<Integer, String> map = new TreeMap<Integer, String>();
            if (SGMLOutput.hasRuleBasedOutput && SGMLOutput.hasSubjClassifierOutput) {
                final ArrayList<String> sents1 = ReaderUtils.readFileToLines(corpus.getAnnotationFile(SGMLOutput.RuleBasedOutput, doc));
                final ArrayList<String> sents2 = ReaderUtils.readFileToLines(corpus.getAnnotationFile(SGMLOutput.SubjOutput, doc));
                for (int i = 0; i < sents1.size(); ++i) {
                    if (sents1.get(i).length() >= 1) {
                        final String[] toks1 = sents1.get(i).split("\t");
                        final String[] toks2 = sents2.get(i).split("\t");
                        if (toks1[0].compareTo(toks2[0]) != 0) {
                            System.out.println("!! Error");
                            break;
                        }
                        final String[] idx = toks1[0].split("_");
                        if (idx.length >= 2) {
                            final int sIdx = Integer.parseInt(idx[idx.length - 2]);
                            final int eIdx = Integer.parseInt(idx[idx.length - 1]);
                            final String tag = "<MPQASENT autoclass1=\"".concat(toks1[1]).concat("\" autoclass2=\"").concat(toks2[1]).concat("\">");
                            map.put(sIdx, tag);
                            map.put(eIdx, "</MPQASENT>");
                        }
                    }
                }
            }
            else if (SGMLOutput.hasRuleBasedOutput) {
                final ArrayList<String> sents3 = ReaderUtils.readFileToLines(corpus.getAnnotationFile(SGMLOutput.RuleBasedOutput, doc));
                for (int j = 0; j < sents3.size(); ++j) {
                    if (sents3.get(j).length() >= 1) {
                        final String[] toks3 = sents3.get(j).split("\t");
                        final String[] idx2 = toks3[0].split("_");
                        if (idx2.length >= 2) {
                            final int sIdx2 = Integer.parseInt(idx2[idx2.length - 2]);
                            final int eIdx2 = Integer.parseInt(idx2[idx2.length - 1]);
                            final String tag2 = "<MPQASENT autoclass1=\"".concat(toks3[1]).concat("\">");
                            map.put(sIdx2, tag2);
                            map.put(eIdx2, "</MPQASENT>");
                        }
                    }
                }
            }
            else if (SGMLOutput.hasSubjClassifierOutput) {
                final ArrayList<String> sents3 = ReaderUtils.readFileToLines(corpus.getAnnotationFile(SGMLOutput.SubjOutput, doc));
                for (int j = 0; j < sents3.size(); ++j) {
                    if (sents3.get(j).length() >= 1) {
                        final String[] toks3 = sents3.get(j).split("\t");
                        final String[] idx2 = toks3[0].split("_");
                        if (idx2.length >= 2) {
                            final int sIdx2 = Integer.parseInt(idx2[idx2.length - 2]);
                            final int eIdx2 = Integer.parseInt(idx2[idx2.length - 1]);
                            final String tag2 = "<MPQASENT autoclass1=\"".concat(toks3[1]).concat("\">");
                            map.put(sIdx2, tag2);
                            map.put(eIdx2, "</MPQASENT>");
                        }
                    }
                }
            }
            if (SGMLOutput.hasPolarityClassifierOutput) {
                final ArrayList<String> pols = ReaderUtils.readFileToLines(corpus.getAnnotationFile(SGMLOutput.PolarityOutput, doc));
                for (final String pol : pols) {
                    if (pol.length() < 1) {
                        continue;
                    }
                    final String[] toks4 = pol.split("\t");
                    final String[] idx3 = toks4[0].split("_");
                    if (idx3.length < 2) {
                        continue;
                    }
                    final int sIdx3 = Integer.parseInt(idx3[idx3.length - 2]);
                    final int eIdx3 = Integer.parseInt(idx3[idx3.length - 1]);
                    final String tag3 = "<MPQAPOL autoclass=\"".concat(toks4[1]).concat("\">");
                    if (map.containsKey(sIdx3)) {
                        String temp = map.get(sIdx3);
                        temp = temp.concat(tag3);
                        map.remove(sIdx3);
                        map.put(sIdx3, temp);
                    }
                    else {
                        map.put(sIdx3, tag3);
                    }
                    if (map.containsKey(eIdx3)) {
                        String temp = map.get(eIdx3);
                        temp = tag3.concat(temp);
                        map.remove(eIdx3);
                        map.put(eIdx3, temp);
                    }
                    else {
                        map.put(eIdx3, "</MPQAPOL>");
                    }
                }
            }
            final Iterator<Integer> iter = map.keySet().iterator();
            int pidx = 0;
            while (iter.hasNext()) {
                final int idx4 = iter.next();
                output = output.concat(str.substring(pidx, idx4));
                output = output.concat(map.get(idx4));
                pidx = idx4;
            }
            output = output.concat(str.substring(pidx));
            WriterUtils.writeData(corpus.getAnnotationFile(SGMLOutput.PREDICTIONFILE, doc), output);
        }
    }
}
