package mpqa4lg.opin.io;

import java.util.*;
import java.nio.charset.*;
import java.io.*;

public class ReaderUtils
{
    private static final int BUFFERSIZE = 16384;
    
    public static ArrayList<String> readFileToLines(final File f) {
        return readFileToLines(f, Charset.forName("UTF-8"));
    }
    
    public static ArrayList<String> readFileToLinesIgnoreEmpty(final File f) {
        return readFileToLinesIgnoreEmpty(f, Charset.forName("UTF-8"));
    }
    
    public static ArrayList<String> readFileToLinesIgnoreEmptyAndComments(final File f) {
        return readFileToLinesIgnoreEmptyAndComments(f, Charset.forName("UTF-8"));
    }
    
    public static String readFileToString(final File f) {
        return readFileToString(f, Charset.forName("UTF-8"));
    }
    
    public static String readFileToString(final File f, final Charset cs) {
        final StringBuilder sb = new StringBuilder();
        FileInputStream rawFile = null;
        InputStreamReader sReader = null;
        BufferedReader bReader = null;
        try {
            final char[] buffer = new char[4096];
            rawFile = new FileInputStream(f);
            sReader = new InputStreamReader(rawFile, cs);
            bReader = new BufferedReader(sReader, 16384);
            int read = 0;
            while ((read = bReader.read(buffer, 0, buffer.length)) != -1) {
                sb.append(buffer, 0, read);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        catch (IOException e3) {
            e3.printStackTrace();
        }
        finally {
            try {
                if (rawFile != null) {
                    rawFile.close();
                }
                if (sReader != null) {
                    sReader.close();
                }
                if (bReader != null) {
                    bReader.close();
                }
            }
            catch (IOException e4) {
                e4.printStackTrace();
            }
        }
        try {
            if (rawFile != null) {
                rawFile.close();
            }
            if (sReader != null) {
                sReader.close();
            }
            if (bReader != null) {
                bReader.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
        return sb.toString();
    }
    
    public static ArrayList<String> readFileToLines(final File f, final Charset cs) {
        FileInputStream rawFile = null;
        InputStreamReader sReader = null;
        BufferedReader bReader = null;
        final ArrayList<String> output = new ArrayList<String>();
        try {
            rawFile = new FileInputStream(f);
            sReader = new InputStreamReader(rawFile, cs);
            bReader = new BufferedReader(sReader, 16384);
            String line;
            while ((line = bReader.readLine()) != null) {
                output.add(line);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        catch (IOException e3) {
            e3.printStackTrace();
        }
        finally {
            try {
                if (rawFile != null) {
                    rawFile.close();
                }
                if (sReader != null) {
                    sReader.close();
                }
                if (bReader != null) {
                    bReader.close();
                }
            }
            catch (IOException e4) {
                e4.printStackTrace();
            }
        }
        try {
            if (rawFile != null) {
                rawFile.close();
            }
            if (sReader != null) {
                sReader.close();
            }
            if (bReader != null) {
                bReader.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
        return output;
    }
    
    public static ArrayList<String> readFileToLinesIgnoreEmpty(final File f, final Charset cs) {
        FileInputStream rawFile = null;
        InputStreamReader sReader = null;
        BufferedReader bReader = null;
        final ArrayList<String> output = new ArrayList<String>();
        try {
            rawFile = new FileInputStream(f);
            sReader = new InputStreamReader(rawFile, cs);
            bReader = new BufferedReader(sReader, 16384);
            String line;
            while ((line = bReader.readLine()) != null) {
                line = line.trim();
                if (!line.equals("")) {
                    output.add(line);
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        catch (IOException e3) {
            e3.printStackTrace();
        }
        finally {
            try {
                if (rawFile != null) {
                    rawFile.close();
                }
                if (sReader != null) {
                    sReader.close();
                }
                if (bReader != null) {
                    bReader.close();
                }
            }
            catch (IOException e4) {
                e4.printStackTrace();
            }
        }
        try {
            if (rawFile != null) {
                rawFile.close();
            }
            if (sReader != null) {
                sReader.close();
            }
            if (bReader != null) {
                bReader.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
        return output;
    }
    
    public static ArrayList<String> readFileToLinesIgnoreEmptyAndComments(final File f, final Charset cs) {
        FileInputStream rawFile = null;
        InputStreamReader sReader = null;
        BufferedReader bReader = null;
        final ArrayList<String> output = new ArrayList<String>();
        try {
            rawFile = new FileInputStream(f);
            sReader = new InputStreamReader(rawFile, cs);
            bReader = new BufferedReader(sReader, 16384);
            String line;
            while ((line = bReader.readLine()) != null) {
                line = line.trim();
                if (!line.equals("") && !line.startsWith("#")) {
                    output.add(line);
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        catch (IOException e3) {
            e3.printStackTrace();
        }
        finally {
            try {
                if (rawFile != null) {
                    rawFile.close();
                }
                if (sReader != null) {
                    sReader.close();
                }
                if (bReader != null) {
                    bReader.close();
                }
            }
            catch (IOException e4) {
                e4.printStackTrace();
            }
        }
        try {
            if (rawFile != null) {
                rawFile.close();
            }
            if (sReader != null) {
                sReader.close();
            }
            if (bReader != null) {
                bReader.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
        return output;
    }
    
    public static void emptyBuffer(final Process p, final boolean show) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if (show) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }
            else {
                String line;
                while ((line = br.readLine()) != null) {}
            }
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            if (show) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }
            else {
                String line;
                while ((line = br.readLine()) != null) {}
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
