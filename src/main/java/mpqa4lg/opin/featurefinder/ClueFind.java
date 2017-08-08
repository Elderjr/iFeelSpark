package mpqa4lg.opin.featurefinder;

import java.io.*;

import mpqa4lg.opin.config.*;
import mpqa4lg.opin.entity.*;
import mpqa4lg.opin.featurefinder.entity.*;
import mpqa4lg.opin.io.*;
import mpqa4lg.opin.logic.*;
import mpqa4lg.opin.preprocessor.entity.*;

import java.util.*;

public class ClueFind
{
    private static int entryIdCounter;
    private ArrayList<File> ngramFiles;
    private ArrayList<ArrayList<Entry>> ngramList;
    private ArrayList<ArrayList<GateDefaultLine>> gateDefaultContents;
    private ArrayList<Hashtable<String, ArrayList<Integer>>> wordLemmaTable;
    private ArrayList<String> featureFileNames;
    private Config conf;
    
    static {
        ClueFind.entryIdCounter = 0;
    }
    
    public ClueFind(final Config c) {
        this.conf = c;
        (this.ngramFiles = new ArrayList<File>()).add(this.conf.getSubjLexicon());
        this.ngramFiles.add(this.conf.getPolarityLexicon());
        this.ngramFiles.add(this.conf.getIntensifierLexicon());
        this.ngramFiles.add(this.conf.getValenceshifterLexicon());
        this.gateDefaultContents = new ArrayList<ArrayList<GateDefaultLine>>();
        this.ngramList = new ArrayList<ArrayList<Entry>>();
        this.wordLemmaTable = new ArrayList<Hashtable<String, ArrayList<Integer>>>();
        this.featureFileNames = new ArrayList<String>();
        for (final File ngramFile : this.ngramFiles) {
            int tempIdCounter = 0;
            this.ngramList.add(new ArrayList<Entry>());
            this.wordLemmaTable.add(new Hashtable<String, ArrayList<Integer>>());
            this.featureFileNames.add(ngramFile.getName());
            final ArrayList<String> ngramLines = ReaderUtils.readFileToLines(ngramFile);
            for (final String ngramLine : ngramLines) {
                if (ngramLine.length() != 0 && ngramLine.charAt(0) != '#') {
                    final Scanner ngramLineScanner = new Scanner(ngramLine);
                    final Entry thisEntry = new Entry(tempIdCounter, true);
                    String firstWord = "";
                    while (ngramLineScanner.hasNext()) {
                        final String attrAndValue = ngramLineScanner.next();
                        final Scanner attrValueSplitter = new Scanner(attrAndValue);
                        attrValueSplitter.useDelimiter("=");
                        final String attribute = attrValueSplitter.next();
                        final String value = attrValueSplitter.next();
                        thisEntry.addAttributes(attribute, value);
                        if (attribute.equals("word1")) {
                            firstWord = value;
                        }
                    }
                    this.ngramList.get(this.ngramList.size() - 1).add(thisEntry);
                    final Hashtable<String, ArrayList<Integer>> thisTable = this.wordLemmaTable.get(this.wordLemmaTable.size() - 1);
                    if (thisTable.containsKey(firstWord)) {
                        thisTable.get(firstWord).add(tempIdCounter);
                    }
                    else {
                        thisTable.put(firstWord, new ArrayList<Integer>());
                        thisTable.get(firstWord).add(tempIdCounter);
                    }
                    ++tempIdCounter;
                    ++ClueFind.entryIdCounter;
                }
            }
        }
    }
    
    public void process(final Corpus corpus) {
        final ArrayList<Document> docs = corpus.getDocs();
        for (final Document d : docs) {
            final File doc = d.getTextFile();
            System.out.println("cluefinder: processing " + doc.getParentFile().getName() + File.separator + doc.getName());
            final File docGateDef = corpus.getAnnotationFile("gate_default", doc);
            final ArrayList<Annotation> gateDefaultAnnotations = AnnotationHandler.readAnns(docGateDef);
            final ArrayList<ArrayList<Integer>> featureId = new ArrayList<ArrayList<Integer>>();
            final ArrayList<ArrayList<Integer>> featureStart = new ArrayList<ArrayList<Integer>>();
            final ArrayList<ArrayList<Integer>> featureEnd = new ArrayList<ArrayList<Integer>>();
            for (int curFeatSet = 0; curFeatSet < this.featureFileNames.size(); ++curFeatSet) {
                final ArrayList<Integer> ngramId = new ArrayList<Integer>();
                final ArrayList<String> ngramNextWord = new ArrayList<String>();
                final ArrayList<String> ngramNextPos = new ArrayList<String>();
                final ArrayList<Integer> ngramBytespanStart = new ArrayList<Integer>();
                final ArrayList<Integer> ngramTotal = new ArrayList<Integer>();
                final ArrayList<Integer> ngramNext = new ArrayList<Integer>();
                featureId.add(new ArrayList<Integer>());
                featureStart.add(new ArrayList<Integer>());
                featureEnd.add(new ArrayList<Integer>());
                for (final Annotation annotation : gateDefaultAnnotations) {
                    final int bytespanStart = annotation.getSpanS();
                    final int bytespanEnd = annotation.getSpanE();
                    final String isTokenToken = annotation.getName();
                    if (isTokenToken.equalsIgnoreCase("GATE_Token")) {
                        String word = "";
                        if (annotation.hasAttribute("string")) {
                            word = annotation.getSingleAttributes("string");
                        }
                        String lemma = "";
                        if (annotation.hasAttribute("lemma")) {
                            lemma = annotation.getSingleAttributes("lemma");
                        }
                        String pos = "";
                        String simplePos = "";
                        if (annotation.hasAttribute("category")) {
                            pos = annotation.getSingleAttributes("category");
                            simplePos = convertGateDefPos(pos);
                        }
                        for (int multiNgramIndex = 0; multiNgramIndex < ngramId.size(); ++multiNgramIndex) {
                            final String nextNgramItem = ngramNextWord.get(multiNgramIndex);
                            final String nextNgramPos = ngramNextPos.get(multiNgramIndex);
                            if ((nextNgramItem.equals(word) || nextNgramItem.equals(lemma)) && (nextNgramPos.equals(pos) || nextNgramPos.equals(simplePos) || nextNgramPos.equals("anypos"))) {
                                if (ngramTotal.get(multiNgramIndex) == ngramNext.get(multiNgramIndex)) {
                                    featureId.get(curFeatSet).add(ngramId.get(multiNgramIndex));
                                    featureStart.get(featureStart.size() - 1).add(ngramBytespanStart.get(multiNgramIndex));
                                    featureEnd.get(featureEnd.size() - 1).add(bytespanEnd);
                                    ngramId.remove(multiNgramIndex);
                                    ngramNextWord.remove(multiNgramIndex);
                                    ngramNextPos.remove(multiNgramIndex);
                                    ngramBytespanStart.remove(multiNgramIndex);
                                    ngramTotal.remove(multiNgramIndex);
                                    ngramNext.remove(multiNgramIndex);
                                    --multiNgramIndex;
                                }
                                else {
                                    final int thisWordNum = ngramNext.get(multiNgramIndex);
                                    final int nextWordNum = thisWordNum + 1;
                                    final String nextWordStr = this.ngramList.get(curFeatSet).get(ngramId.get(multiNgramIndex)).getAttributes().get("word" + nextWordNum);
                                    final String nextPosStr = this.ngramList.get(curFeatSet).get(ngramId.get(multiNgramIndex)).getAttributes().get("pos" + nextWordNum);
                                    ngramNextWord.set(multiNgramIndex, nextWordStr);
                                    ngramNextPos.set(multiNgramIndex, nextPosStr);
                                    ngramNext.set(multiNgramIndex, nextWordNum);
                                }
                            }
                            else {
                                ngramId.remove(multiNgramIndex);
                                ngramNextWord.remove(multiNgramIndex);
                                ngramNextPos.remove(multiNgramIndex);
                                ngramBytespanStart.remove(multiNgramIndex);
                                ngramTotal.remove(multiNgramIndex);
                                ngramNext.remove(multiNgramIndex);
                                --multiNgramIndex;
                            }
                        }
                        if (!this.wordLemmaTable.get(curFeatSet).containsKey(word) && !this.wordLemmaTable.get(curFeatSet).containsKey(lemma)) {
                            continue;
                        }
                        final ArrayList<Integer> matches = new ArrayList<Integer>();
                        if (this.wordLemmaTable.get(curFeatSet).get(word) != null) {
                            final ArrayList<Integer> potentialMatches = new ArrayList<Integer>();
                            potentialMatches.addAll(this.wordLemmaTable.get(curFeatSet).get(word));
                            for (int i = 0; i < potentialMatches.size(); ++i) {
                                final int potentialMatch = potentialMatches.get(i);
                                final String potentialFeatPos = this.ngramList.get(curFeatSet).get(potentialMatch).getAttributes().get("pos1");
                                if (!potentialFeatPos.equals(pos) && !potentialFeatPos.equals(simplePos) && !potentialFeatPos.equals("anypos")) {
                                    potentialMatches.remove(i);
                                    --i;
                                }
                            }
                            matches.addAll(potentialMatches);
                        }
                        if (this.wordLemmaTable.get(curFeatSet).get(lemma) != null) {
                            final ArrayList<Integer> potentialMatches = new ArrayList<Integer>();
                            potentialMatches.addAll(this.wordLemmaTable.get(curFeatSet).get(lemma));
                            for (int i = 0; i < potentialMatches.size(); ++i) {
                                final int potentialMatch = potentialMatches.get(i);
                                final String potentialFeatPos = this.ngramList.get(curFeatSet).get(potentialMatch).getAttributes().get("pos1");
                                if (!potentialFeatPos.equals(pos) && !potentialFeatPos.equals(simplePos) && !potentialFeatPos.equals("anypos")) {
                                    potentialMatches.remove(i);
                                    --i;
                                }
                            }
                            matches.addAll(potentialMatches);
                        }
                        Collections.sort(matches);
                        Collections.reverse(matches);
                        for (int j = 0; j < matches.size(); ++j) {
                            final int firstMatch = matches.get(j);
                            final HashMap<String, String> firstTable = this.ngramList.get(curFeatSet).get(firstMatch).getAttributes();
                            final int firstLen = Integer.parseInt(firstTable.get("len"));
                            for (int k = j + 1; k < matches.size(); ++k) {
                                final int secondMatch = matches.get(k);
                                final HashMap<String, String> secondTable = this.ngramList.get(curFeatSet).get(secondMatch).getAttributes();
                                final int secondLen = Integer.parseInt(secondTable.get("len"));
                                if (firstLen == secondLen) {
                                    boolean isSame = true;
                                    for (int l = 1; l <= firstLen; ++l) {
                                        if (!firstTable.get("word" + l).equals(secondTable.get("word" + l)) || !firstTable.get("pos" + l).equals(secondTable.get("pos" + l))) {
                                            isSame = false;
                                        }
                                    }
                                    if (isSame) {
                                        matches.remove(k);
                                        --k;
                                    }
                                }
                            }
                        }
                        boolean hasBeenAdded = false;
                        for (int matchNum = 0; matchNum < matches.size(); ++matchNum) {
                            final Entry thisMatch = this.ngramList.get(curFeatSet).get(matches.get(matchNum));
                            if (thisMatch.getAttributes().get("len").equals("1")) {
                                if (!hasBeenAdded) {
                                    hasBeenAdded = true;
                                    featureId.get(curFeatSet).add(matches.get(matchNum));
                                    featureStart.get(curFeatSet).add(bytespanStart);
                                    featureEnd.get(curFeatSet).add(bytespanEnd);
                                }
                            }
                            else {
                                ngramId.add(matches.get(matchNum));
                                ngramNextWord.add(thisMatch.getAttributes().get("word2"));
                                ngramNextPos.add(thisMatch.getAttributes().get("pos2"));
                                ngramBytespanStart.add(bytespanStart);
                                ngramTotal.add(Integer.parseInt(thisMatch.getAttributes().get("len")));
                                ngramNext.add(2);
                            }
                        }
                    }
                }
                final ArrayList<AutoAnnLine> als = new ArrayList<AutoAnnLine>();
                for (int m = 0; m < featureId.get(curFeatSet).size(); ++m) {
                    final Entry thisEntry = this.ngramList.get(curFeatSet).get(featureId.get(curFeatSet).get(m));
                    final HashMap<String, String> thisEntryHash = thisEntry.getAttributes();
                    final int bytespanStart2 = featureStart.get(curFeatSet).get(m);
                    final int bytespanEnd2 = featureEnd.get(curFeatSet).get(m);
                    String typeAls = "";
                    if (thisEntryHash.containsKey("type")) {
                        typeAls = thisEntryHash.get("type");
                    }
                    String mpqaPolarityAls = null;
                    if (thisEntryHash.containsKey("mpqapolarity")) {
                        mpqaPolarityAls = thisEntryHash.get("mpqapolarity");
                    }
                    als.add(new AutoAnnLine(typeAls, mpqaPolarityAls, bytespanStart2, bytespanEnd2));
                }
                WriterUtils.writeLinesWithNum(corpus.getAnnotationFile(this.ngramFiles.get(curFeatSet).getName().split("\\.")[0], doc), als);
            }
        }
    }

    	
    /**
     * [4LG]
     */
	public Map<String, List<Annotation>> process(final List<Annotation> gateDefaultAnnotations) {
    	
    	Map<String, List<Annotation>> contentClueAnnotationFiles = new HashMap<>();
    	
    	final List<List<Integer>> featureId = new ArrayList<>();
    	final List<List<Integer>> featureStart = new ArrayList<>();
    	final List<List<Integer>> featureEnd = new ArrayList<>();
    	for (int curFeatSet = 0; curFeatSet < this.featureFileNames.size(); ++curFeatSet) {
    		final List<Integer> ngramId = new ArrayList<Integer>();
    		final List<String> ngramNextWord = new ArrayList<String>();
    		final List<String> ngramNextPos = new ArrayList<String>();
    		final List<Integer> ngramBytespanStart = new ArrayList<Integer>();
    		final List<Integer> ngramTotal = new ArrayList<Integer>();
    		final List<Integer> ngramNext = new ArrayList<Integer>();
    		featureId.add(new ArrayList<Integer>());
    		featureStart.add(new ArrayList<Integer>());
    		featureEnd.add(new ArrayList<Integer>());
    		for (final Annotation annotation : gateDefaultAnnotations) {
    			final int bytespanStart = annotation.getSpanS();
    			final int bytespanEnd = annotation.getSpanE();
    			final String isTokenToken = annotation.getName();
    			if (isTokenToken.equalsIgnoreCase("GATE_Token")) {
    				String word = "";
    				if (annotation.hasAttribute("string")) {
    					word = annotation.getSingleAttributes("string");
    				}
    				String lemma = "";
    				if (annotation.hasAttribute("lemma")) {
    					lemma = annotation.getSingleAttributes("lemma");
    				}
    				String pos = "";
    				String simplePos = "";
    				if (annotation.hasAttribute("category")) {
    					pos = annotation.getSingleAttributes("category");
    					simplePos = convertGateDefPos(pos);
    				}
    				for (int multiNgramIndex = 0; multiNgramIndex < ngramId.size(); ++multiNgramIndex) {
    					final String nextNgramItem = ngramNextWord.get(multiNgramIndex);
    					final String nextNgramPos = ngramNextPos.get(multiNgramIndex);
    					if ((nextNgramItem.equals(word) || nextNgramItem.equals(lemma)) && (nextNgramPos.equals(pos) || nextNgramPos.equals(simplePos) || nextNgramPos.equals("anypos"))) {
    						if (ngramTotal.get(multiNgramIndex) == ngramNext.get(multiNgramIndex)) {
    							featureId.get(curFeatSet).add(ngramId.get(multiNgramIndex));
    							featureStart.get(featureStart.size() - 1).add(ngramBytespanStart.get(multiNgramIndex));
    							featureEnd.get(featureEnd.size() - 1).add(bytespanEnd);
    							ngramId.remove(multiNgramIndex);
    							ngramNextWord.remove(multiNgramIndex);
    							ngramNextPos.remove(multiNgramIndex);
    							ngramBytespanStart.remove(multiNgramIndex);
    							ngramTotal.remove(multiNgramIndex);
    							ngramNext.remove(multiNgramIndex);
    							--multiNgramIndex;
    						}
    						else {
    							final int thisWordNum = ngramNext.get(multiNgramIndex);
    							final int nextWordNum = thisWordNum + 1;
    							final String nextWordStr = this.ngramList.get(curFeatSet).get(ngramId.get(multiNgramIndex)).getAttributes().get("word" + nextWordNum);
    							final String nextPosStr = this.ngramList.get(curFeatSet).get(ngramId.get(multiNgramIndex)).getAttributes().get("pos" + nextWordNum);
    							ngramNextWord.set(multiNgramIndex, nextWordStr);
    							ngramNextPos.set(multiNgramIndex, nextPosStr);
    							ngramNext.set(multiNgramIndex, nextWordNum);
    						}
    					}
    					else {
    						ngramId.remove(multiNgramIndex);
    						ngramNextWord.remove(multiNgramIndex);
    						ngramNextPos.remove(multiNgramIndex);
    						ngramBytespanStart.remove(multiNgramIndex);
    						ngramTotal.remove(multiNgramIndex);
    						ngramNext.remove(multiNgramIndex);
    						--multiNgramIndex;
    					}
    				}
    				if (!this.wordLemmaTable.get(curFeatSet).containsKey(word) && !this.wordLemmaTable.get(curFeatSet).containsKey(lemma)) {
    					continue;
    				}
    				final List<Integer> matches = new ArrayList<Integer>();
    				if (this.wordLemmaTable.get(curFeatSet).get(word) != null) {
    					final List<Integer> potentialMatches = new ArrayList<Integer>();
    					potentialMatches.addAll(this.wordLemmaTable.get(curFeatSet).get(word));
    					for (int i = 0; i < potentialMatches.size(); ++i) {
    						final int potentialMatch = potentialMatches.get(i);
    						final String potentialFeatPos = this.ngramList.get(curFeatSet).get(potentialMatch).getAttributes().get("pos1");
    						if (!potentialFeatPos.equals(pos) && !potentialFeatPos.equals(simplePos) && !potentialFeatPos.equals("anypos")) {
    							potentialMatches.remove(i);
    							--i;
    						}
    					}
    					matches.addAll(potentialMatches);
    				}
    				if (this.wordLemmaTable.get(curFeatSet).get(lemma) != null) {
    					final List<Integer> potentialMatches = new ArrayList<Integer>();
    					potentialMatches.addAll(this.wordLemmaTable.get(curFeatSet).get(lemma));
    					for (int i = 0; i < potentialMatches.size(); ++i) {
    						final int potentialMatch = potentialMatches.get(i);
    						final String potentialFeatPos = this.ngramList.get(curFeatSet).get(potentialMatch).getAttributes().get("pos1");
    						if (!potentialFeatPos.equals(pos) && !potentialFeatPos.equals(simplePos) && !potentialFeatPos.equals("anypos")) {
    							potentialMatches.remove(i);
    							--i;
    						}
    					}
    					matches.addAll(potentialMatches);
    				}
    				Collections.sort(matches);
    				Collections.reverse(matches);
    				for (int j = 0; j < matches.size(); ++j) {
    					final int firstMatch = matches.get(j);
    					final HashMap<String, String> firstTable = this.ngramList.get(curFeatSet).get(firstMatch).getAttributes();
    					final int firstLen = Integer.parseInt(firstTable.get("len"));
    					for (int k = j + 1; k < matches.size(); ++k) {
    						final int secondMatch = matches.get(k);
    						final HashMap<String, String> secondTable = this.ngramList.get(curFeatSet).get(secondMatch).getAttributes();
    						final int secondLen = Integer.parseInt(secondTable.get("len"));
    						if (firstLen == secondLen) {
    							boolean isSame = true;
    							for (int l = 1; l <= firstLen; ++l) {
    								if (!firstTable.get("word" + l).equals(secondTable.get("word" + l)) || !firstTable.get("pos" + l).equals(secondTable.get("pos" + l))) {
    									isSame = false;
    								}
    							}
    							if (isSame) {
    								matches.remove(k);
    								--k;
    							}
    						}
    					}
    				}
    				boolean hasBeenAdded = false;
    				for (int matchNum = 0; matchNum < matches.size(); ++matchNum) {
    					final Entry thisMatch = this.ngramList.get(curFeatSet).get(matches.get(matchNum));
    					if (thisMatch.getAttributes().get("len").equals("1")) {
    						if (!hasBeenAdded) {
    							hasBeenAdded = true;
    							featureId.get(curFeatSet).add(matches.get(matchNum));
    							featureStart.get(curFeatSet).add(bytespanStart);
    							featureEnd.get(curFeatSet).add(bytespanEnd);
    						}
    					}
    					else {
    						ngramId.add(matches.get(matchNum));
    						ngramNextWord.add(thisMatch.getAttributes().get("word2"));
    						ngramNextPos.add(thisMatch.getAttributes().get("pos2"));
    						ngramBytespanStart.add(bytespanStart);
    						ngramTotal.add(Integer.parseInt(thisMatch.getAttributes().get("len")));
    						ngramNext.add(2);
    					}
    				}
    			}
    		}
    		final List<AutoAnnLine> als = new ArrayList<AutoAnnLine>();
    		for (int m = 0; m < featureId.get(curFeatSet).size(); ++m) {
    			final Entry thisEntry = this.ngramList.get(curFeatSet).get(featureId.get(curFeatSet).get(m));
    			final HashMap<String, String> thisEntryHash = thisEntry.getAttributes();
    			final int bytespanStart2 = featureStart.get(curFeatSet).get(m);
    			final int bytespanEnd2 = featureEnd.get(curFeatSet).get(m);
    			String typeAls = "";
    			if (thisEntryHash.containsKey("type")) {
    				typeAls = thisEntryHash.get("type");
    			}
    			String mpqaPolarityAls = null;
    			if (thisEntryHash.containsKey("mpqapolarity")) {
    				mpqaPolarityAls = thisEntryHash.get("mpqapolarity");
    			}
    			als.add(new AutoAnnLine(typeAls, mpqaPolarityAls, bytespanStart2, bytespanEnd2));
    		}

    		List<Annotation> temp = AnnotationHandler.convertAutoAnnLinesToAnnotations(als);
    		contentClueAnnotationFiles.put(this.ngramFiles.get(curFeatSet).getName().split("\\.")[0], temp);                
    	}
        
        return contentClueAnnotationFiles;
    }

    public static String convertGateDefPos(final String gateDefPos) {
        if (gateDefPos.equals("RB") || gateDefPos.equals("RBR") || gateDefPos.equals("RBS") || gateDefPos.equals("WRB")) {
            return "adverb";
        }
        if (gateDefPos.equals("VB") || gateDefPos.equals("VBD") || gateDefPos.equals("VBG") || gateDefPos.equals("VBN") || gateDefPos.equals("VBP") || gateDefPos.equals("VBZ")) {
            return "verb";
        }
        if (gateDefPos.equals("NNS") || gateDefPos.equals("NN") || gateDefPos.equals("NNP") || gateDefPos.equals("NNPS") || gateDefPos.equals("NP") || gateDefPos.equals("NPS")) {
            return "noun";
        }
        if (gateDefPos.equals("WP") || gateDefPos.equals("WP$") || gateDefPos.equals("PRP") || gateDefPos.equals("PRP$") || gateDefPos.equals("PP") || gateDefPos.equals("PP$")) {
            return "pronoun";
        }
        if (gateDefPos.equals("JJ") || gateDefPos.equals("JJR") || gateDefPos.equals("JJS")) {
            return "adj";
        }
        if (gateDefPos.equals("MD")) {
            return "modal";
        }
        if (gateDefPos.equals("EX")) {
            return "exthere";
        }
        if (gateDefPos.equals("CD")) {
            return "number";
        }
        return "";
    }
}
