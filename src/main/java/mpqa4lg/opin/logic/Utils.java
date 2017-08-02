package mpqa4lg.opin.logic;

import mpqa4lg.opin.entity.*;

import java.util.*;

public class Utils
{
    public static TreeMap<String, ArrayList<Span>> takeRandomSubset(final TreeMap<String, ArrayList<Span>> spans) {
        final TreeMap<String, ArrayList<Span>> randomSubSet = new TreeMap<String, ArrayList<Span>>();
        final Random r = new Random();
        for (final String key : spans.keySet()) {
            if (r.nextInt(2) == 1) {
                randomSubSet.put(key, spans.get(key));
            }
        }
        return randomSubSet;
    }
}
