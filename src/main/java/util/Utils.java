package util;


import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author johnnatan, matheus, elias, jpaulo
 */
public class Utils {

	/**
	 * common for some Methods
	 * regex w/o double backslashes: [!\"#$%&\'()*+,-./:;<=>?@[\\]^_`{|}~]
	 */
	private static final String PUNCTUATION_REGEX = "[!\"#$%&'()\\*\\+,-\\./:;<=>?@\\[\\]\\^_`{|}~]";

	/**
	 * @param puncRegex regex containing all punctuation symbols
	 * @param text
	 * @return text with puctuation removed
	 */
	public static String removePunctuation(String puncRegex, String text) {

		return text.replaceAll(puncRegex, " ");
	}

	/**
	 * @param text
	 * @return text with puctuation symbols in PUNCTUATION_REGEX removed
	 */
	public static String removePunctuation(String text) {

		return removePunctuation(PUNCTUATION_REGEX, text);
	}

	/**
	 * @return whether str doesn't contain lowercase letter
	 */
	public static boolean isUpperString(String str) {

		return !str.matches("^.*[a-z].*$");

/*		for (int i=0; i<str.length(); ++i)
			if (Character.isLowerCase(str.charAt(i))) return false;
		return true; */		
	}

	/**
	 * @param list
	 * @param value
	 * @return how many elements <code>value</code> the <code>list</code> contains.
	 */
	public static int countOccurrences(List<String> list, String value) {

		int count = 0;
		for (String element : list) {

			if (element.equals(value)) {
				++count;
			}
		}

		return count;
	}

	/**
	 * @param text
	 * @param c
	 * @return how many times <code>c</code> occurs in <code>text</code>.
	 */
	public static int countChars(String text, char c) {

		int count = 0;
		for (int i=0; i < text.length(); ++i) {
			if (c == text.charAt(i)) {
				++count;
			}
		}
		
		return count;
	}

	/**
	 * @param d number to apply precision
	 * @param precision max size of decimal
	 * @return <code>d</code> with <code>precision</code> especified
	 */
	public static double setPrecision(double d, int precision) {
		BigDecimal bd = new BigDecimal(d).setScale(precision, RoundingMode.HALF_EVEN);
	    return bd.doubleValue();
	}

	/**
	 * 
	 * @param fileName
	 * @return Set containing all lexicon word read
	 */
	public static Set<String> readFileWordsToSet(final String fileName) {
		Set<String> set = new HashSet<String>();
		try {
			//InputStream in = Utils.class.getResourceAsStream(fileName);
			File f = new File(fileName);			
			FileReader fr = new FileReader(f);
			BufferedReader input = new BufferedReader(fr);
			String line;
			while((line = input.readLine()) != null) {
				line = removePunctuation(line.trim().toLowerCase());
				for(String word: line.split("\\s+")){
					set.add(word);
				}
			}
			input.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
			//System.err.println("Error opening file: " + fileName);
			//System.exit(1989);
		}
		
		
		return set;
	}
	
	public static LinkedList<String> readFileLines(final String fileName) {
		LinkedList<String> list = new LinkedList<>();
		try {
			//InputStream in = Utils.class.getResourceAsStream(fileName);
			File f = new File(fileName);			
			FileReader fr = new FileReader(f);
			BufferedReader input = new BufferedReader(fr);
			String line;
			while((line = input.readLine()) != null) {
				line = line.trim();
				list.add(line);
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			//System.err.println("Error opening file: " + fileName);
			//System.exit(1989);
		}
		
		
		return list;
	}
	
	public static LinkedList<String> readFileLinesProcessed(final String fileName) {
		LinkedList<String> list = new LinkedList<>();
		try {
			//InputStream in = Utils.class.getResourceAsStream(fileName);
			File f = new File(fileName);			
			FileReader fr = new FileReader(f);
			BufferedReader input = new BufferedReader(fr);
			String line;
			while((line = input.readLine()) != null) {
				line = removePunctuation(line.trim().toLowerCase());
				list.add(line);
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			//System.err.println("Error opening file: " + fileName);
			//System.exit(1989);
		}
		
		
		return list;
	}
	
	public static Set<String> readFileLinesToSet(final String fileName) {
		Set<String> set = new HashSet<>();
		try {
			//InputStream in = Utils.class.getResourceAsStream(fileName);
			File f = new File(fileName);			
			FileReader fr = new FileReader(f);
			BufferedReader input = new BufferedReader(fr);
			String line;
			while((line = input.readLine()) != null) {
				line = line.trim();
				set.add(line);
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}
	
	public static List<Integer> readClassifications(final String fileName){
		List<Integer> classifications = new LinkedList<>();
		try {
			File f = new File(fileName);			
			FileReader fr = new FileReader(f);
			BufferedReader input = new BufferedReader(fr);
			String line;
			while((line = input.readLine()) != null) {
				classifications.add(Integer.parseInt(line));
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classifications;
	}
	
	/**
	 * @param x
	 * @return log of x at base 2
	 */
	public static double log2(double x) {
		return Math.log(x) / Math.log(2);
	}
}
