package Algorithm;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Support {

    static Map<String,Double> allSupportMap = new HashMap<>();
    public static double getSupport(Map<Integer, List<String>> itemMap,List<String> itemSet)
    {
        double a = itemMap.size();
        double b = 0;
        for(List<String> items: itemMap.values())
        {
            if(items.containsAll(itemSet))
            {
                b++;
            }
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        double returnDouble = Double.parseDouble(decimalFormat.format(b/a));
        allSupportMap.put(itemSet.toString(),returnDouble);
        return returnDouble;
    }

    public static int getSupportFrequency(Map<Integer, List<String>> itemMap,List<String> itemSet)
    {
        int b = 0;
        for(List<String> items: itemMap.values())
        {
            if(items.containsAll(itemSet))
            {
                b++;
            }
        }
        return b;
    }

    public static double getConfidence(List<String> stringList1,List<String> stringList2)
    {
        double a = allSupportMap.get(stringList1.toString());
        double b = 0.0000001;
        for(Map.Entry<String,Double> entry:allSupportMap.entrySet())
        {
            if(entry.getKey().equals(stringList2.toString()))
            {
                b = entry.getValue();
                break;
            }
        }
        //double b = allSupportMap.get(stringList2.toString());
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        double returnDouble = Double.parseDouble(decimalFormat.format(a/b));
        return returnDouble;
    }
}
