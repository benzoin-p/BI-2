package Algorithm;

import Data.DataMap;
import org.springframework.stereotype.Component;

import java.util.*;

/*
* Apriori算法
* 通过递推剪枝直到无法剪枝，获取频繁项集
* */
@Component("apriori")
public class Apriori {

    //满足最小支持度的频繁集
    Map<List<String>,Double> supportMap = new HashMap<>();

    //满足最小置信度要求的频繁集
    Map<String, Double> confidenceMap = new HashMap<>();

    DataMap dataMap;

    //用户输入最小的支持度
    double minSupport;

    //用户输入最小的置信度
    double minConfidence;

    //判断是否完成频繁项集
    boolean finishFlag;

    //记录下的非频繁项集，键是项中元素的个数
    Set<List<String>> infrequentItemsSet = new HashSet<>();

    Apriori(DataMap dataMap,double minSupport,double minConfidence)
    {
        System.out.println("以下是Apriori算法的内容：");
        this.dataMap = dataMap;
        dataMap.printItemMap();
        this.minSupport = minSupport;
        this.minConfidence =minConfidence;
        firstScan(dataMap.getItemMap());
        firstCut();
        while(!finishFlag)
        {
            scanAndCut();
        }
        getConfidentMap();
    }

    Apriori(){}

    public DataMap getDataMap() {
        return dataMap;
    }

    public void printSupportMap()
    {
        if(supportMap.isEmpty())
        {
            System.out.println("所有项支持度均小于要求的最小支持度,故剪枝在上一次已经完成");
        }
        else
        {
            for(Map.Entry<List<String>,Double> entry:supportMap.entrySet())
            {
                System.out.println("ItemSet:"+entry.getKey()+"  sup:"+entry.getValue());
            }
        }
    }

    //第一次扫描
    public void firstScan(Map<Integer, List<String>> itemMap)
    {
        for(Map.Entry<Integer,List<String>> entry:itemMap.entrySet())
        {
            List<String> stringList = entry.getValue();
            for(String str:stringList)
            {
                List<String> strList = new ArrayList<>();
                strList.add(str);
                if(!supportMap.containsKey(strList))
                {
                    supportMap.put(strList,Support.getSupport(itemMap,strList));
                }
            }
        }
        System.out.println("经过首次剪枝后可得：");
        printSupportMap();
    }

    //第一次剪枝
    public void firstCut()
    {
        for(Map.Entry<List<String>,Double> entry:supportMap.entrySet())
        {
            if(entry.getValue()<minSupport)
            {
                //不能在迭代器迭代的时候进行移除
                //supportMap.remove(entry.getKey());
                infrequentItemsSet.add(entry.getKey());
            }
        }
        for(List<String> stringList:infrequentItemsSet)
        {
            supportMap.remove(stringList);
        }
    }

    //连接
    /*
    * 对上一轮的频繁集中的项进行两两比较，如果只相差1个不同的元则将其合并*/
    public Map<List<String>,Double> combine()
    {
        Map<List<String>,Double> map = new HashMap<>();
        for(Map.Entry<List<String>,Double> entry1:supportMap.entrySet())
        {
            List<String> stringList1 = entry1.getKey();
            for(Map.Entry<List<String>,Double> entry2:supportMap.entrySet())
            {
                List<String> stringList2 = entry2.getKey();
                //System.out.print("list1 = "+stringList1+"list2 = "+stringList2);
                List<String> subString = DataMap.getSubStringList1(stringList1,stringList2);
                if(!subString.isEmpty()&&(stringList1.size() == 1 ||subString.size() == stringList1.size()-1))
                {
                    //System.out.print("list1 = "+stringList1+"list2 = "+stringList2);
                    List<String> newStringList = new ArrayList<>(stringList1);
                    //System.out.print("1."+newStringList);
                    newStringList.removeAll(stringList2);
                    //System.out.print("2."+newStringList);
                    newStringList.addAll(stringList2);
                    //System.out.println("3."+newStringList);
                    List<String> newStringList2 = DataMap.orderList(newStringList);

                    if(map.isEmpty())
                    {
                        map.put(newStringList2,Support.getSupport(dataMap.getItemMap(),newStringList2));
                    }
                    else
                    {
                        for(Map.Entry<List<String>,Double> entry:map.entrySet())
                        {
                            if(DataMap.getSubStringList1(newStringList2,entry.getKey()).isEmpty())
                            {
                                break;
                            }
                        }
                        map.put(newStringList2,Support.getSupport(dataMap.getItemMap(),newStringList2));
                    }
                }
            }
        }
//        if(map.isEmpty())
//        {
//            System.out.println("所有项支持度均小于要求的最小支持度");
//        }
//        else
//        {
//            for(Map.Entry<List<String>,Double> entry:map.entrySet())
//            {
//                System.out.println("ItemSet:"+entry.getKey()+"  sup:"+entry.getValue());
//            }
//        }
        return map;
    }

    //剪枝
    public void scanAndCut()
    {
        //获取连接（对上一轮的频繁集中的项进行两两比较，如果只相差1个不同的元则将其合并）的结果
        Map<List<String>,Double> map = combine();
        Map<List<String>,Double> newMap = new HashMap<>();
        for(Map.Entry<List<String>,Double> entry:map.entrySet())
        {
            //进行剪枝，如果该组的支持度小于最小支持度，则将其置入非频繁项集
            if(entry.getValue() < minSupport)
            {
                infrequentItemsSet.add(entry.getKey());
            }
            //判断该项是否被移入非频繁项集，若是则将其从频繁项集移出，若否则加入
            else
            {

                for(List<String> stringList:infrequentItemsSet)
                {
                    List<String> newStringList = new ArrayList<>(entry.getKey());
                    newStringList.removeAll(stringList);
                    newStringList.addAll(stringList);
                    if(DataMap.getSubStringList1(newStringList,entry.getKey()).isEmpty())
                    {
                        break;
                    }
                }
                newMap.put(entry.getKey(), entry.getValue());
            }
        }
        //无法产生新的频繁项，说明剪枝结束
        if(newMap.isEmpty())
        {
            System.out.println("剪枝已经完成，以下为频繁项集");
            for(Map.Entry<List<String>,Double> entry:supportMap.entrySet())
            {
                System.out.println("ItemSet:"+entry.getKey()+"  sup:"+entry.getValue());
            }
            finishFlag = true;
        }
        //移除目前频繁项集中真子集为非频繁项的项，将map传给supportMap
        else
        {
            Map<List<String>,Double> newMap2 = new HashMap<>();
            for(Map.Entry<List<String>,Double> entry1:supportMap.entrySet())
            {
                newMap2.put(entry1.getKey(),entry1.getValue());
                for(Map.Entry<List<String>,Double> entry2:newMap.entrySet())
                {
                    List<String> stringList = new ArrayList<>(entry2.getKey());
                    stringList.removeAll(entry1.getKey());
                    stringList.addAll(entry1.getKey());
                    if(DataMap.getSubStringList1(stringList,entry2.getKey()).isEmpty())
                    {
                        newMap2.remove(entry1.getKey());
                        break;
                    }
                }
            }
            newMap.putAll(newMap2);
            supportMap = newMap;
            System.out.println("经过剪枝后可得：");
            printSupportMap();
        }
    }

    //获取子集
    public Set<List<String>> getSubSet(List<String> stringList)
    {
        Set<List<String>> set = new HashSet<>();
        List<String> result = new ArrayList<>();
        StringBuilder s = new StringBuilder();
        for(String str:stringList)
        {
            s.append(str);
        }
        calculate(set,result, s.toString(),0,s.length()-1);
        set.remove(stringList);
        return set;
    }

    //通过递归计算子集
    public void calculate(Set<List<String>> set,List<String> result, String a, int start, int end) {
        if (end == start) {
            String s = a.charAt(start) + "";
            result.add(s);
            List<String> list = new ArrayList<>();
            list.add(s);
            set.add(list);
            return;
        }
        calculate(set,result, a, start, end - 1);
        int size = result.size();
        for (int i = 0; i < size; i++) {
            String s = result.get(i) + a.charAt(end);
            result.add(s);
            List<String> list = new ArrayList<>();
            for(int j=0;j<s.length();j++)
            {
                String c = String.valueOf(s.charAt(j));
                list.add(c);
            }
            set.add(list);
        }
    }


    //判断是否大于最小置信度
    public void isConfident(List<String> stringList)
    {
        Set<List<String>> set = getSubSet(stringList);
        for(List<String> list:set)
        {
            double confidence = Support.getConfidence(stringList,list);
            if(confidence<minConfidence)
            {
                return;
            }
            else
            {
                //l-s
                List<String> newList = new ArrayList<>(stringList);
                newList.removeAll(list);
                String s = list.toString() + "->" + newList.toString();
                confidenceMap.put(s,confidence);
            }
        }
    }

    public void getConfidentMap()
    {
        for(Map.Entry<List<String>,Double> entry:supportMap.entrySet())
        {
            isConfident(entry.getKey());
        }

        System.out.println("以下是关联规则");
        for(Map.Entry<String,Double> entry:confidenceMap.entrySet())
        {
            System.out.println("ItemRule:"+entry.getKey()+"  cof:"+entry.getValue());
        }
    }



}
