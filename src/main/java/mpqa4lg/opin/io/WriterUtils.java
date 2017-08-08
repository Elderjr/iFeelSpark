package mpqa4lg.opin.io;

import java.util.*;
import java.io.*;

public class WriterUtils
{
    private static final int BUFFERSIZE = 16384;
    
    public static <V> void writeLines(final File f, final Collection<V> data) {
        FileOutputStream rawFile = null;
        OutputStreamWriter sWriter = null;
        BufferedWriter bWriter = null;
        try {
            rawFile = new FileOutputStream(f);
            sWriter = new OutputStreamWriter(rawFile, "UTF-8");
            bWriter = new BufferedWriter(sWriter, 16384);
            for (final V v : data) {
                bWriter.append((CharSequence)v.toString());
                bWriter.newLine();
            }
            bWriter.flush();
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
                if (sWriter != null) {
                    sWriter.close();
                }
                if (bWriter != null) {
                    bWriter.close();
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
            if (sWriter != null) {
                sWriter.close();
            }
            if (bWriter != null) {
                bWriter.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
    }
    
    public static <V> void writeLinesWithId(final File f, final Collection<V> data) {
        FileOutputStream rawFile = null;
        OutputStreamWriter sWriter = null;
        BufferedWriter bWriter = null;
        try {
            rawFile = new FileOutputStream(f);
            sWriter = new OutputStreamWriter(rawFile, "UTF-8");
            bWriter = new BufferedWriter(sWriter, 16384);
            int i = 1;
            for (final V v : data) {
                bWriter.append((CharSequence)("ID" + i + "\t"));
                bWriter.append((CharSequence)String.valueOf(i));
                bWriter.append((CharSequence)"\t");
                bWriter.append((CharSequence)v.toString());
                bWriter.newLine();
                ++i;
            }
            bWriter.flush();
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
                if (sWriter != null) {
                    sWriter.close();
                }
                if (bWriter != null) {
                    bWriter.close();
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
            if (sWriter != null) {
                sWriter.close();
            }
            if (bWriter != null) {
                bWriter.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
    }
    
    public static <V> void writeLinesWithNum(final File f, final Collection<V> data) {
        FileOutputStream rawFile = null;
        OutputStreamWriter sWriter = null;
        BufferedWriter bWriter = null;
        try {
            rawFile = new FileOutputStream(f);
            sWriter = new OutputStreamWriter(rawFile, "UTF-8");
            bWriter = new BufferedWriter(sWriter, 16384);
            int i = 1;
            for (final V v : data) {
                bWriter.append((CharSequence)String.valueOf(i));
                bWriter.append((CharSequence)"\t");
                bWriter.append((CharSequence)v.toString());
                bWriter.newLine();
                ++i;
            }
            bWriter.flush();
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
                if (sWriter != null) {
                    sWriter.close();
                }
                if (bWriter != null) {
                    bWriter.close();
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
            if (sWriter != null) {
                sWriter.close();
            }
            if (bWriter != null) {
                bWriter.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
    }
    
    public static <V> void writeData(final File f, final String data) {
        FileOutputStream rawFile = null;
        OutputStreamWriter sWriter = null;
        BufferedWriter bWriter = null;
        try {
            rawFile = new FileOutputStream(f);
            sWriter = new OutputStreamWriter(rawFile, "UTF-8");
            bWriter = new BufferedWriter(sWriter, 16384);
            bWriter.append((CharSequence)data);
            bWriter.flush();
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
                if (sWriter != null) {
                    sWriter.close();
                }
                if (bWriter != null) {
                    bWriter.close();
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
            if (sWriter != null) {
                sWriter.close();
            }
            if (bWriter != null) {
                bWriter.close();
            }
        }
        catch (IOException e4) {
            e4.printStackTrace();
        }
    }
    
    public static void executeCommand(final String command) {
        final Runtime rt = Runtime.getRuntime();
        System.out.println("Running Command : " + command);
        try {
            final Process p = rt.exec(command);
            emptyBuffer(p, true);
            p.waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
