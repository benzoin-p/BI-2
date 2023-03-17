package Algorithm;

import Data.DataMap;
import Data.TreeVo;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("fpGrowth")
public class FPGrowth {

    DataMap dataMap;

    //用户输入最小的支持度
    double minSupport;

    //用户输入最小的置信度
    double minConfidence;

    //最小的支持数
    int minSupportFrequency;

    //满足最小支持度的频繁集
    Map<List<String>,Integer> supportMap = new HashMap<>();

    //用于接收频繁集项，进行排序
    List<Map.Entry<List<String>,Integer>> supportMapList = new ArrayList<>();

    //键为数据id，值为项的内容（含一个或多个不同的元素），项中元素顺序按照单个元素支持度倒序排
    Map<Integer, List<String>> orderItemMap = new HashMap<>();

    //fp树
    TreeVo FpTree = new TreeVo(null,-1);



//    //条件模式基
//    Map<List<String>,Integer> modMap = new HashMap<>();

    public FPGrowth(DataMap dataMap, double minSupport, double minConfidence) {
        System.out.println("以下是FP-Growth算法的内容：");
        this.dataMap = dataMap;
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.minSupportFrequency = (int) Math.round(minSupport*dataMap.getItemNum());
        firstScan(dataMap.getItemMap());
        orderItemMap();
        printItemMap();
        createTree();
        System.out.println("生成的频繁模式树如下：");
        FpTree.printTree(0);
        System.out.println();
        findMode();
        /*
        接下来要做的：
            对orderItemMap倒序遍历（不遍历最大的节点），
            找出其中每一个元素在树中大于最小支持数的节点，
            找出这些节点分别是什么树的子节点，
            获取频繁模式，该节点的支持数即为该模式的支持数，
            将支持数转换为支持度，调用获取关联规则的算法，
            对比Apriori算法得出的结果，观察是否正确，
            结束
         */
    }

    public FPGrowth() {
    }

    public void printSupportMap()
    {
        if(supportMap.isEmpty()||supportMapList.isEmpty())
        {
            System.out.println("所有项支持度均小于要求的最小支持度,故剪枝在上一次已经完成");
        }
        else
        {
            for(Map.Entry<List<String>,Integer> entry:supportMapList)
            {
                System.out.println("ItemSet:"+entry.getKey()+"  sup:"+entry.getValue());
            }
        }
    }

    public void printItemMap()
    {
        System.out.println("倒序整理数据：");
        for(Map.Entry<Integer,List<String>> entry:orderItemMap.entrySet())
        {
            System.out.println("ID:"+entry.getKey()+"  List of Items'IDs:"+entry.getValue());
        }
    }

    //第一次扫描并按倒序排序
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
                    supportMap.put(strList,Support.getSupportFrequency(itemMap,strList));
                }
            }
        }
        supportMapList = new ArrayList<>(supportMap.entrySet());
        supportMapList.sort((o1, o2) -> o2.getValue()- o1.getValue());
        //supportMapList.addAll(supportMap.entrySet());
        //supportMapList.sort();
        System.out.println("经过首次扫描后可得倒序频繁集：");
        printSupportMap();
    }

    //将排序后的结果添加到新的itemMap中
    public void orderItemMap()
    {
        for(Map.Entry<Integer,List<String>> entry:dataMap.getItemMap().entrySet())
        {
            orderItemMap.put(entry.getKey(),orderList(entry.getValue()));
        }
    }

    //按照单个元素支持度倒序对项中元素排序
    public List<String> orderList(List<String> list)
    {
        List<String> newList =  new ArrayList<>();
        for(String str:list)
        {
            if(newList.isEmpty())
            {
                newList.add(str);
            }
            else
            {
                newList.add(str);
                for(String s:newList)
                {
                    List<String> t1 = new ArrayList<>();
                    List<String> t2 = new ArrayList<>();
                    t1.add(str);
                    t2.add(s);
                    if(supportMap.get(t2)<supportMap.get(t1))
                    {
                        newList.remove(newList.size()-1);
                        newList.add(newList.indexOf(s),str);
                        break;
                    }
                }
            }
        }
        return newList;
    }

    //构造FP树
    public void createTree()
    {
        //对项中元素顺序按照单个元素支持度倒序排序的列表进行遍历
        for(Map.Entry<Integer,List<String>> entry:orderItemMap.entrySet())
        {
            //空的FP树，结点为(null,-1)
            TreeVo p = FpTree;
            //分别对每个项的元素进行遍历
            for(String string:entry.getValue())
            {
                List<TreeVo> nodeList = p.getNodeList();
                TreeVo pp = null;
                //该分支与下一分支均为树中不包含该元素的情况，功能为添加结点并使其支持数置为1，并使下次遍历以该结点作为根节点
                if(nodeList == null||nodeList.isEmpty())
                {
                    List<TreeVo> list = new ArrayList<>();
                    pp = new TreeVo(string,1);
                    list.add(pp);
                    p.setNodeList(list);
                    //System.out.println("add"+pp.getElement()+" to "+p.getElement()+"'s nodelist");
                }
                else if(!p.isContain(string))
                {
                    List<TreeVo> list = p.getNodeList();
                    pp = new TreeVo(string,1);
                    list.add(pp);
                    p.setNodeList(list);
                    //System.out.println("add"+pp.getElement()+" to "+p.getElement()+"'s nodelist");
                }
                //该分支为树中包含该元素的情况，功能为使该结点支持数自增1，并使下次遍历以该结点作为根节点
                else
                {
                    for(TreeVo treeVo:nodeList)
                    {
                        if(treeVo.isEqual(string))
                        {
                            treeVo.addSupport();
                            pp = treeVo;
                            //System.out.println("add"+pp.getElement()+"'s support which is in "+p.getElement()+"'s nodelist");
                            break;
                        }
                    }
                }
                //使下次遍历以该结点作为根节点
                if(pp != null)
                {
                    p = pp;
                }
            }
        }
    }

    //找到频繁模式
    public void findMode()
    {
        Map<List<String>,Integer> map = new HashMap<>();
        int max = supportMapList.get(0).getValue();
        for(Map.Entry<List<String>,Integer> entry:supportMapList)
        {
            //用于存储条件模式基
            Map<List<String>,Integer> modeMap= new HashMap<>();
            //用于存储条件fp树
            Map<List<String>,List<Integer>> treeMap= new HashMap<>();
            //用于存储剪枝后的条件fp树
            Map<List<String>,List<Integer>> tTreeMap;


            //不遍历最大的
            if(entry.getValue() == max)
            {
                continue;
            }
            String s = entry.getKey().get(0);
            //List<TreeVo> voList = new ArrayList<>();
            //List<List<String>> treePath = new ArrayList<>();

            //生成条件模式基
            createConditionMode(s,modeMap);

            //生成条件fp树
           createFPTree(s,modeMap,treeMap);
           tTreeMap = cutFPTree(treeMap,minSupportFrequency);

           //生成频繁模式
            createFreMode(tTreeMap,s,map);




            System.out.println("条件模式基");
            for(Map.Entry<List<String>,Integer> entry1:modeMap.entrySet())
            {
                System.out.println("ItemSet:"+entry1.getKey()+"  sup:"+entry1.getValue());
            }
            System.out.println();
            for(Map.Entry<List<String>,List<Integer>> entry2:treeMap.entrySet())
            {
                System.out.println("ItemSet:"+entry2.getKey()+"  sup:"+entry2.getValue());
            }
            System.out.println("条件fp树：");
            for(Map.Entry<List<String>,List<Integer>> entry2:tTreeMap.entrySet())
            {
                System.out.println("ItemSet:"+entry2.getKey()+"  sup:"+entry2.getValue());
            }
        }
        System.out.println("以下为频繁项集：");
        for(Map.Entry<List<String>,Integer> entry2:map.entrySet())
        {
            System.out.println("ItemSet:"+entry2.getKey()+"  sup:"+entry2.getValue());
        }
    }

    //生成条件模式基
    public void createConditionMode(String s,Map<List<String>,Integer> modeMap)
    {
        if(FpTree.getNodeList() != null)
        {
            for(TreeVo treeVo:FpTree.getNodeList())
            {
                List<String> stringList = new ArrayList<>();
                treeVo.getChildTree(treeVo,s,modeMap,stringList);
            }
        }
    }

    //生成条件fp树
    public void createFPTree(String s,Map<List<String>,Integer> modeMap,Map<List<String>,List<Integer>> treeMap)
    {
        List<List<String>> listList = new ArrayList<>();
        List<List<Integer>> listList1 = new ArrayList<>();
        Map<List<String>,List<Integer>> uncutTreeMap= new HashMap<>();
        for(Map.Entry<List<String>,Integer> modeEntry:modeMap.entrySet())
        {
            List<String> stringList = new ArrayList<>();
            List<String> modeList = modeEntry.getKey();
            List<Integer> supList = new ArrayList<>();
            for(int i=0;i<modeList.size();i++)
            {
                if(!modeList.get(i).equals(s))
                {
                    stringList.add(modeList.get(i));
                    supList.add(modeEntry.getValue());
                }
            }
            listList.add(stringList);
            listList1.add(supList);
            uncutTreeMap.put(stringList,supList);
        }
        //长度为1的情况
        if(listList.size() == 1)
        {
            treeMap.put(listList.get(0),listList1.get(0));
            return;
        }
        //长度不为1的情况
        for(int i=0;i<listList.size();i++)
        {
            for(Map.Entry<List<String>,List<Integer>> treeEntry:uncutTreeMap.entrySet())
            {
                List<String> stringList = new ArrayList<>(listList.get(i));
                List<Integer> integerList = new ArrayList<>(listList1.get(i));
                if(treeEntry.getKey().get(0).equals(listList.get(i).get(0)))
                {
                    //{A,C:3,1}替代{A:2}的情况
                    if(stringList.size()>treeEntry.getKey().size())
                    {
                        //System.out.println(treeEntry.getKey()+" 被 "+stringList+" 覆盖 ");
                        //System.out.println(" 使 "+integerList.get(0)+" + "+treeEntry.getValue().get(0));
                        treeMap.remove(treeEntry.getKey());
                        List<String> newStringList = new ArrayList<>(stringList);
                        integerList.set(0,integerList.get(0)+treeEntry.getValue().get(0));
                        treeMap.put(newStringList,integerList);
                    }
                    //{A,C:3,1}和{A,B:2,1}出现产生{A,C:5,1}和{A,B:5,1}的情况
                    else if (stringList.size() == treeEntry.getKey().size()
                            && stringList.size() != 1 && !stringList.get(1).equals(treeEntry.getKey().get(1)))
                    {
                        //System.out.println(stringList+"与"+treeEntry.getKey()+"合并");
                        //System.out.println("使 "+integerList.get(0)+" + "+treeEntry.getValue().get(0));
                        integerList.set(0,integerList.get(0)+treeEntry.getValue().get(0));
                        treeMap.put(stringList,integerList);
                    }
                    else {
                        if(!treeMap.containsKey(stringList))
                        {
                            treeMap.put(stringList,integerList);
                            for(Map.Entry<List<String>,List<Integer>> tTreeEntry:treeMap.entrySet())
                            {
                                if(stringList.get(0).equals(tTreeEntry.getKey().get(0))
                                        && tTreeEntry.getKey().size() != 1)
                                {
                                    treeMap.remove(stringList);
                                    break;
                                }
                                if(stringList.get(0).equals(tTreeEntry.getKey().get(tTreeEntry.getKey().size()-1)))
                                {
                                    integerList.set(0,integerList.get(0)
                                            +tTreeEntry.getValue().get(tTreeEntry.getKey().size()-1));
                                    treeMap.replace(stringList,integerList);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //删除条件fp树中不满足最小支持度的项
    public Map<List<String>,List<Integer>> cutFPTree(Map<List<String>,List<Integer>> treeMap,int minSupportFrequency)
    {
        Map<List<String>,List<Integer>> map = new HashMap<>();
        for(Map.Entry<List<String>,List<Integer>> tTreeEntry:treeMap.entrySet())
        {
            List<Integer> integerList = new ArrayList<>(tTreeEntry.getValue());
            int t=-1;
            for(int i=0;i<integerList.size();i++)
            {
                if(i == 0 && integerList.get(i)<minSupportFrequency)
                {
                    return null;
                }
                if(integerList.get(i)<minSupportFrequency)
                {
                    t=i;
                }
            }
            if(t>=0)
            {
                List<String> stringList = new ArrayList<>();
                List<Integer> newIntegerList = new ArrayList<>();
                for(int i=0;i<t;i++)
                {
                    stringList.add(tTreeEntry.getKey().get(i));
                    newIntegerList.add(tTreeEntry.getValue().get(i));
                }
                map.put(stringList,newIntegerList);
            }
            else
            {
                map.put(tTreeEntry.getKey(),tTreeEntry.getValue());
            }
        }
        return map;
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
        //set.remove(stringList);
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

    //生成频繁模式
    public void createFreMode(Map<List<String>,List<Integer>> tTreeMap,String s,Map<List<String>,Integer> map)
    {
        Map<String,Integer> oneItemMap = new HashMap<>();
        for(Map.Entry<List<String>,List<Integer>> entry2:tTreeMap.entrySet())
        {
            List<String> stringList = new ArrayList<>(entry2.getKey());
            Set<List<String>> subSet = getSubSet(stringList);
            for(List<String> stringList1:subSet)
            {
                stringList1.add(s);
                if(stringList1.size() != 2)
                {
                    List<Integer> integerList = entry2.getValue();
                    int size = integerList.size();
                    int t = integerList.get(size-1);
                    map.put(stringList1,t);
                }
                else
                {
                    String str = stringList1.get(0);
                    if(oneItemMap.isEmpty() || !oneItemMap.containsKey(str))
                    {
                        oneItemMap.put(str,entry2.getValue().get(0));
                    }
                    else
                    {
                        int t = oneItemMap.get(str);
                        oneItemMap.replace(str,t+entry2.getValue().get(0));
                    }
                }
            }
        }
        for(Map.Entry<List<String>,List<Integer>> entry2:tTreeMap.entrySet())
        {
            List<String> stringList = new ArrayList<>(entry2.getKey());
            Set<List<String>> subSet = getSubSet(stringList);
            for(List<String> stringList1:subSet)
            {
                stringList1.add(s);
                if(stringList1.size() == 2)
                {
                    int t = oneItemMap.get(stringList1.get(0));
                    map.put(stringList1,t);
                }
            }
        }
    }



//    //用于上个函数中进行递归调用
//    public void addMode(TreeVo treeVo,List<String> stringList,String s,List<TreeVo> voList)
//    {
//        stringList.add(treeVo.getElement());
//        int sup = treeVo.getSup(s,treeVo);
//        if(treeVo.getNodeList()!=null)
//        {
//            for(TreeVo vo:treeVo.getNodeList())
//            {
//                if(voList.contains(vo))
//                {
//                    addMode(vo,stringList,s,voList);
//                }
//                else if
//                {
//
//                }
//            }
//        }
//    }

}
