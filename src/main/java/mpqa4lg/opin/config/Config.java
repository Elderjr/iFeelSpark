package mpqa4lg.opin.config;

import java.io.*;
import java.nio.charset.*;

import mpqa4lg.opin.io.*;

import java.util.*;

public class Config
{
    private File intensifierLexicon;
    private File valenceshifterLexicon;
    private File polarityLexicon;
    private File subjLexicon;
    private File stanfordModel;
    private File subjModel;
    private File polarityModel;
    private File polarityModelHeader;
    private File swsdModelDir;
    private Charset charset;
    private boolean runPreprocessor;
    private boolean runClueFinder;
    private boolean runSubjClassifier;
    private boolean runRulebasedClassifier;
    private boolean runPolarityClassifier;
    private boolean runSGMLOutput;
    private boolean runSWSD;
    private boolean useDatabaseStructure;
    private ArrayList<String> documents;
    private static String USAGE;
    
    static {
        Config.USAGE = "java -classpath ./lib/weka.jar:opinionfinder.jar opin.main.RunOpinionFinder FILE [OPTION]\n-d ... the input file holds a list of documents to be processed (default: the input file is a single document)\n-s ... use the database structure for processed documents (default: the annotations are created in the same folder as the original file)\n-r ... the modules to run, a comma seperated list (default: all modules --> preprocessor,cluefinder,rulebasedclass,subjclass,polarityclass)\n-m ... the folder holding of the opinionfinder classifier models (default: 'models' directory)\n-l ... the folder holding of the opinionfinder lexicons (default: 'lexicons' directory)\n-e ... character set of the processed documents (default: UTF-8)\n-w ... swsd support (default: false)\n";
    }
    
    public Config() {
        this.polarityLexicon = new File("lexicons" + File.separator + "subjclueslen1polar.tff");
        this.subjLexicon = new File("lexicons" + File.separator + "subjcluesSentenceClassifiersOpinionFinderJune06.tff");
        this.intensifierLexicon = new File("lexicons" + File.separator + "intensifiers.tff");
        this.valenceshifterLexicon = new File("lexicons" + File.separator + "valenceshifters.tff");
        this.stanfordModel = new File("models" + File.separator + "english-left3words-distsim.tagger");
        this.subjModel = new File("models" + File.separator + "sent_subj.model");
        this.polarityModel = new File("models" + File.separator + "exp_polarity.model");
        this.polarityModelHeader = new File("models" + File.separator + "exp_polarity_header.arff");
        this.swsdModelDir = new File("models" + File.separator + "swsd");
        this.charset = Charset.forName("UTF-8");
        this.runPreprocessor = true;
        this.runClueFinder = true;
        this.runSubjClassifier = true;
        this.runRulebasedClassifier = true;
        this.runPolarityClassifier = true;
        this.runSGMLOutput = true;
        this.runSWSD = false;
        this.useDatabaseStructure = false;
        this.documents = new ArrayList<String>();
    }
    
    public boolean parseCommandLineOptions(final String[] args) {
        if (args.length < 1) {
            System.out.println(Config.USAGE);
            return false;
        }
        final String f = args[0];
        boolean useAsDoclist = false;
        for (int i = 1; i < args.length; ++i) {
            if (args[i].equals("-d")) {
                useAsDoclist = true;
            }
            else if (args[i].equals("-m")) {
                if (i == args.length - 1) {
                    System.out.println(Config.USAGE);
                    return false;
                }
                final String v = args[++i];
                final File modelPath = new File(v);
                if (!modelPath.exists()) {
                    System.out.println("!! Model directory does not exist: " + modelPath.getAbsolutePath());
                    return false;
                }
                this.stanfordModel = new File(String.valueOf(modelPath.getAbsolutePath()) + File.separator + this.stanfordModel.getName());
                if (!this.stanfordModel.exists()) {
                    System.out.println("!! Stanford model does not exist: " + this.stanfordModel.getAbsolutePath());
                    return false;
                }
                this.subjModel = new File(String.valueOf(modelPath.getAbsolutePath()) + File.separator + this.subjModel.getName());
                if (!this.subjModel.exists()) {
                    System.out.println("!! Subjectivity classifier model does not exist: " + this.subjModel.getAbsolutePath());
                    return false;
                }
                this.polarityModel = new File(String.valueOf(modelPath.getAbsolutePath()) + File.separator + this.polarityModel.getName());
                if (!this.polarityModel.exists()) {
                    System.out.println("!! Polarity classifier model does not exist: " + this.polarityModel.getAbsolutePath());
                    return false;
                }
                this.polarityModelHeader = new File(String.valueOf(modelPath.getAbsolutePath()) + File.separator + this.polarityModelHeader.getName());
                if (!this.polarityModelHeader.exists()) {
                    System.out.println("!! Polarity model header does not exist: " + this.polarityModelHeader.getAbsolutePath());
                    return false;
                }
                this.swsdModelDir = new File(String.valueOf(modelPath.getAbsolutePath()) + File.separator + this.swsdModelDir.getName());
                if (!this.swsdModelDir.exists()) {
                    System.out.println("!! SWSD model directory does not exist: " + this.swsdModelDir.getAbsolutePath());
                }
            }
            else if (args[i].equals("-l")) {
                if (i == args.length - 1) {
                    System.out.println(Config.USAGE);
                    return false;
                }
                final String v = args[++i];
                final File lexiconPath = new File(v);
                if (!lexiconPath.exists()) {
                    System.out.println("!! Lexicon directory does not exist: " + lexiconPath.getAbsolutePath());
                    return false;
                }
                this.subjLexicon = new File(String.valueOf(lexiconPath.getAbsolutePath()) + File.separator + "subjcluesSentenceClassifiersOpinionFinderJune06.tff");
                if (!this.subjLexicon.exists()) {
                    System.out.println("!! Subjectivity lexicon does not exist: " + this.subjLexicon.getAbsolutePath());
                    return false;
                }
                this.polarityLexicon = new File(String.valueOf(lexiconPath.getAbsolutePath()) + File.separator + "subjclueslen1polar.tff");
                if (!this.polarityLexicon.exists()) {
                    System.out.println("!! Polarity lexicon does not exist: " + this.polarityLexicon.getAbsolutePath());
                    return false;
                }
                this.intensifierLexicon = new File(String.valueOf(lexiconPath.getAbsolutePath()) + File.separator + "intensifiers.tff");
                if (!this.intensifierLexicon.exists()) {
                    System.out.println("!! Intensifier lexicon does not exist: " + this.intensifierLexicon.getAbsolutePath());
                    return false;
                }
                this.valenceshifterLexicon = new File(String.valueOf(lexiconPath.getAbsolutePath()) + File.separator + "valenceshifters.tff");
                if (!this.valenceshifterLexicon.exists()) {
                    System.out.println("!! Valenceshifter lexicon does not exist: " + this.valenceshifterLexicon.getAbsolutePath());
                    return false;
                }
            }
            else if (args[i].equals("-r")) {
                if (i == args.length - 1) {
                    System.out.println(Config.USAGE);
                    return false;
                }
                final String[] vs = args[++i].split(",");
                this.runPreprocessor = false;
                this.runClueFinder = false;
                this.runSubjClassifier = false;
                this.runRulebasedClassifier = false;
                this.runPolarityClassifier = false;
                this.runSGMLOutput = false;
                for (int j = 0; j < vs.length; ++j) {
                    if (vs[j].equals("preprocessor")) {
                        this.runPreprocessor = true;
                    }
                    else if (vs[j].equals("cluefinder")) {
                        this.runClueFinder = true;
                    }
                    else if (vs[j].equals("subjclass")) {
                        this.runSubjClassifier = true;
                    }
                    else if (vs[j].equals("rulebasedclass")) {
                        this.runRulebasedClassifier = true;
                    }
                    else if (vs[j].equals("polarityclass")) {
                        this.runPolarityClassifier = true;
                    }
                    else {
                        if (!vs[j].equals("sgml")) {
                            System.out.println("!! Not recognized module name: " + vs[j]);
                            return false;
                        }
                        this.runSGMLOutput = true;
                    }
                }
            }
            else if (args[i].equals("-s")) {
                this.useDatabaseStructure = true;
            }
            else if (args[i].equals("-e")) {
                if (!Charset.isSupported(args[i + 1])) {
                    System.out.println("!! Not recognized character set: " + args[i + 1]);
                    return false;
                }
                this.charset = Charset.forName(args[++i]);
            }
            else if (args[i].equals("-w")) {
                if (this.swsdModelDir.exists()) {
                    this.runSWSD = true;
                }
            }
            else {
                System.out.println(Config.USAGE);
            }
        }        
/* [4LG] - text/sentence to be analysed is a parameter, not a file
        if (useAsDoclist) {
            final File doclistFile = new File(f);
            if (!doclistFile.exists()) {
                System.out.println("!! Doclist file does not exist: " + doclistFile.getAbsolutePath());
                return false;
            }
            this.documents.addAll(ReaderUtils.readFileToLinesIgnoreEmptyAndComments(doclistFile));
        }
        else {
            final File docFile = new File(f);
            if (!docFile.exists()) {
                System.out.println("!! Document does not exist: " + docFile.getAbsolutePath());
                return false;
            }
            this.documents.add(docFile.getAbsolutePath());
        }
*/
        return true;
    }
    
    public File getIntensifierLexicon() {
        return this.intensifierLexicon;
    }
    
    public File getValenceshifterLexicon() {
        return this.valenceshifterLexicon;
    }
    
    public File getPolarityLexicon() {
        return this.polarityLexicon;
    }
    
    public File getSubjLexicon() {
        return this.subjLexicon;
    }
    
    public File getStanfordModel() {
        return this.stanfordModel;
    }
    
    public File getSubjModel() {
        return this.subjModel;
    }
    
    public File getPolarityModel() {
        return this.polarityModel;
    }
    
    public Charset getCharset() {
        return this.charset;
    }
    
    public boolean isRunPreprocessor() {
        return this.runPreprocessor;
    }
    
    public boolean isRunClueFinder() {
        return this.runClueFinder;
    }
    
    public boolean isRunSubjClassifier() {
        return this.runSubjClassifier;
    }
    
    public boolean isRunRulebasedClassifier() {
        return this.runRulebasedClassifier;
    }
    
    public boolean isRunPolarityClassifier() {
        return this.runPolarityClassifier;
    }
    
    public boolean isUseDatabaseStructure() {
        return this.useDatabaseStructure;
    }
    
    public boolean isRunSGMLOutput() {
        return this.runSGMLOutput;
    }
    
    public ArrayList<String> getDocuments() {
        return this.documents;
    }
    
    public File getPolarityModelHeader() {
        return this.polarityModelHeader;
    }
    
    public boolean isRunSWSD() {
        return this.runSWSD;
    }
    
    public File getSwsdModelDir() {
        return this.swsdModelDir;
    }
}
