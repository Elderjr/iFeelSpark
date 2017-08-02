/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package umigon4lg.LanguageDetection;

import umigon4lg.LanguageDetection.Cyzoku.util.Detector;
import umigon4lg.LanguageDetection.Cyzoku.util.DetectorFactory;
import umigon4lg.LanguageDetection.Cyzoku.util.LangDetectException;

/**
 *
 * @author C. Levallois
 */
public class LanguageDetector {

    Detector detector;
    String lang;

    public LanguageDetector() throws LangDetectException {
        
    }

    public boolean detectEnglish(String status) throws LangDetectException {
        detector = DetectorFactory.create();
        detector.append(status);
        System.out.println("status: " + status);
        try {
            lang = detector.detect();
            if (!lang.equals("en")) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
