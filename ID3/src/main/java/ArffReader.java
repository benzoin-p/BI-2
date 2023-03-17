import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArffReader {
    public static final String stringPattern = "@attribute(.*)[{](.*?)[}]";

    //将arff文件读取到对应的数组中
    public static void readArff(File file, List<String> attribute, List<List<String>> attributeValue, List<List<String>> data)
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            //通过regex设置pattern匹配属性的名称以及取值
            Pattern pattern = Pattern.compile(stringPattern);
            while((line = br.readLine()) != null)
            {
                Matcher matcher = pattern.matcher(line);
                if(matcher.find())
                {
                    attribute.add(matcher.group(1).trim());
                    String[] values = matcher.group(2).split(",");
                    List<String> arrayList = new ArrayList<>(values.length);
                    for (String value:values)
                    {
                        arrayList.add(value.trim());
                    }
                    attributeValue.add(arrayList);
                }
                else if(line.startsWith("@data"))
                {
                    while((line = br.readLine()) != null)
                    {
                        if(line.equals(""))
                        {
                            continue;
                        }
                        String[] values = line.split(",");
                        List<String> arrayList = new ArrayList<>(values.length);
                        for(String value:values)
                        {
                            arrayList.add(value.trim());
                        }
                        data.add(arrayList);
                    }
                }
            }
            br.close();
        }
        catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
}
