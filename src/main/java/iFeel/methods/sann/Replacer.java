package iFeel.methods.sann;
import java.util.Map;
import java.util.Set;

public class Replacer {
    private Map<String, Properties> wdict;
    private Set<String> wlist;
    
    public Replacer(Map<String, Properties> wdict, Set<String> wlist){
        this.wdict = wdict;
        this.wlist = wlist;
    }
    
    public String repeatReplacer(String word) {
        boolean intensify = false;
        char iTemp = ' ';
        if(word.charAt(word.length()-1)=='!' || word.charAt(word.length()-1)=='?'){
            intensify = true;
            iTemp = word.charAt(word.length()-1);
        }
        String check = word.replaceAll("[^a-zA-Z0-9']*", "");
        if(wdict.containsKey(word) || wlist.contains(word)){
            return word;
        }
        if(wdict.containsKey(check) || wlist.contains(check)){
            if(intensify){
                return check+""+iTemp;
            }
            else{
                return check;
            }
        }
        int sizeBefore = -1;
        int sizeAfter = +1;       
        String regexSann = "(.*)(.)\\2(.*)";
        String repl = "$1$2$3";
        
        while(sizeBefore != sizeAfter){
            sizeBefore = check.length();
            check = check.replaceAll(regexSann, repl);
            if(wdict.containsKey(check) || wlist.contains(check)){
                if(intensify){
                    return check+""+iTemp;
                }
                else{
                    return check;
                }
            }
            sizeAfter = check.length();
        }
        if(intensify){
            return check+""+iTemp;
        }
        else{
            return check;
        }
    }
}
