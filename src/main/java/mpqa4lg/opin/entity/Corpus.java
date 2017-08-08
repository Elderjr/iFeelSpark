package mpqa4lg.opin.entity;

import mpqa4lg.opin.config.*;

import java.io.*;
import java.util.*;

public class Corpus
{
    private HashSet<String> readAnnos;
    private ArrayList<Document> docs;
    private boolean useDB;
    
    public Corpus(final Config c) {
        this.useDB = c.isUseDatabaseStructure();
        this.docs = new ArrayList<Document>();
        this.readAnnos = new HashSet<String>();
        for (final String document : c.getDocuments()) {
            final File documentF = new File(document);
            if (!documentF.exists() || !documentF.isFile()) {
                System.out.println("!!! The document does not exist: " + documentF.getAbsolutePath());
            }
            else {
                final File parentD = documentF.getParentFile();
                if (this.useDB) {
                    if (parentD != null) {
                        final File subD = parentD.getParentFile();
                        if (subD != null && subD.getName().equals("docs")) {
                            final File autoannsBase = new File(documentF.getAbsolutePath().replace("docs", "auto_anns"));
                            if (!autoannsBase.exists()) {
                                autoannsBase.mkdirs();
                            }
                            this.docs.add(new Document(documentF));
                        }
                        else {
                            System.out.println("!!! The document does not follow the database directory structure, skipping document: " + documentF.getAbsolutePath());
                        }
                    }
                    else {
                        System.out.println("!!! The document does not follow the database directory structure, skipping document: " + documentF.getAbsolutePath());
                    }
                }
                else {
                    final File autoannsBase2 = new File(String.valueOf(documentF.getAbsolutePath()) + "_auto_anns");
                    if (!autoannsBase2.exists()) {
                        autoannsBase2.mkdirs();
                    }
                    this.docs.add(new Document(documentF));
                }
            }
        }
    }
    
    public File getAnnotationFile(final String name, final File docF) {
        String autoannsBase;
        if (this.useDB) {
            autoannsBase = docF.getAbsolutePath().replace("docs", "auto_anns");
        }
        else {
            autoannsBase = String.valueOf(docF.getAbsolutePath()) + "_auto_anns";
        }
        return new File(String.valueOf(autoannsBase) + File.separator + name);
    }
    
    public File getAnnotationFile(final String name, final Document doc) {
        final File docF = doc.getTextFile();
        return this.getAnnotationFile(name, docF);
    }
    
    public ArrayList<Document> getDocs() {
        return this.docs;
    }
    
    public boolean hasAnnotation(final String annoName) {
        return this.readAnnos.contains(annoName);
    }
    
    public void readAnnotation(final String annoName) {
        this.readAnnos.add(annoName);
    }
}
