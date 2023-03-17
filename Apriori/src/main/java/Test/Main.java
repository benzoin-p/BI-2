package Test;

import Algorithm.Apriori;
import Algorithm.FPGrowth;
import Data.DataMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        //DataMap dataMap = (DataMap)applicationContext.getBean("dataMap");
        Apriori apriori = (Apriori)applicationContext.getBean("apriori");
        FPGrowth fpGrowth = (FPGrowth)applicationContext.getBean("fpGrowth");
        //apriori.getDataMap().printItemMap();
        //apriori.printSupportMap();
    }
}
