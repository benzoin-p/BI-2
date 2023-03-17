import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("开始");
        Document xmlDoc;
        Element root;
        xmlDoc = DocumentHelper.createDocument();
        root = xmlDoc.addElement("root");
        root.addElement("DecisionTree").addAttribute("value", "null");

        File file = new File("D:/whu/商务智能/dataSet/weather.arff");
        ID3 id3 = new ID3(file,"play",root,xmlDoc);
        id3.buildDT("DecisionTree","null",id3.initDataSubset(),id3.initAttIndexList());
        id3.writeXML("D:/whu/商务智能/dataSet/data.xml");
        System.out.println("结束");
    }
}
