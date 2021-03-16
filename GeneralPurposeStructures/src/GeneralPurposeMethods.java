import java.util.ArrayList;
import java.util.List;

/**
 * Clasa care contine metode statice generice, care rezolva anume sarcini elementare cu structuri de date de baza
 */
public class GeneralPurposeMethods {
    /**
     * Functie care calculeaza diferenta a doua liste generice.
     * @param <T> Tipul de date al membrilor listei.
     * @return list1 - list2
     */
    public static <T> List<T> ListDifferences(List<T> list1, List<T> list2){
        List<T> result = new ArrayList<>();
        boolean found;
        for(T list1_member : list1){
            found = false;
            for(T list2_member : list2){
                if(list1_member.equals(list2_member)){
                    found = true;
                    break;
                }
            }
            if(!found){
                result.add(list1_member);
            }
        }
        return result;
    }
}
