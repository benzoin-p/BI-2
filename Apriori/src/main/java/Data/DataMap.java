package Data;

import org.springframework.stereotype.Component;

import java.util.*;

@Component("dataMap")
public class DataMap {

    //键为数据id，值为项的内容（含一个或多个不同的元素）
    Map<Integer, List<String>> itemMap = new HashMap<>();

    //总元素的个数
    private int elementNum;

    //项的个数
    private int itemNum;

    //元素名的列表
    private List<String> elementName;

    DataMap(int elementNum,int itemNum)
    {
        setElementNum(elementNum);
        setItemNum(itemNum);
        InitialItemName();
        while(itemMap.size()<itemNum)
        {
            addRandomItem();
        }
    }

    DataMap() {}

    public Map<Integer, List<String>> getItemMap() {
        return itemMap;
    }

    public void printItemMap()
    {
        System.out.println("生成数据：");
        for(Map.Entry<Integer,List<String>> entry:itemMap.entrySet())
        {
            System.out.println("ID:"+entry.getKey()+"  List of Items'IDs:"+entry.getValue());
        }
    }

    //按字母顺序从A开始生成元素项名字
    private void InitialItemName()
    {
        this.elementName = new ArrayList<>();
        int i = 0;
        while(i<this.elementNum)
        {
            elementName.add(String.valueOf((char)(65+i)));
            i++;
        }
    }

    public void setElementNum(int elementNum)
    {
        this.elementNum = Math.max(elementNum, 0);
    }

    public void setItemNum(int itemNum)
    {
        if(elementNum < 0)
        {
            this.itemNum = 0;
        }
        else
        {
            this.itemNum = itemNum;
        }
    }

    public int getItemNum() {
        return itemNum;
    }

    //生成不重复的随机项
    private void addRandomItem()
    {
        if(itemMap.isEmpty())
        {
            itemMap.put(1,createItems());
        }
        else
        {
            int num = itemMap.size()+1;
            List<String> stringList1 = createItems();
            for(Map.Entry<Integer,List<String>> entry:itemMap.entrySet())
            {
                List<String> stringList2 = entry.getValue();
                List<String> subStringList;
                if(stringList1.size()>=stringList2.size())
                {
                    subStringList = getSubStringList1(stringList1,stringList2);
                }
                else
                {
                    subStringList = getSubStringList1(stringList2,stringList1);
                }
                if(subStringList.isEmpty())
                {
                    return;
                }
            }
            itemMap.put(num,stringList1);
        }
    }

    //生成长度在一定范围且元素随机的随机项
    private List<String> createItems()
    {
        Random random = new Random();
        int maxItemNum = elementNum-1;
        int itemNumInItems = 2+random.nextInt(maxItemNum-1);
        List<String> stringList = new ArrayList<>();
        while(stringList.size()<itemNumInItems)
        {
            String s = elementName.get(random.nextInt(elementNum));
            while(stringList.contains(s))
            {
                s = elementName.get(random.nextInt(elementNum));
            }
            if(stringList.isEmpty())
            {
                stringList.add(s);
            }
            else
            {
                stringList.add(s);
                for(String str:stringList)
                {
                    if(s.charAt(0)<str.charAt(0))
                    {
                        stringList.remove(stringList.size()-1);
                        stringList.add(stringList.indexOf(str),s);
                        break;
                    }
                }
            }
        }
        return stringList;
    }

    //用于比较两个项相差的元素
    public static List<String> getSubStringList1(List<String> list1,List<String> list2)
    {
        List<String> stringList = new ArrayList<>();
        if(list1.equals(list2))
        {
            return stringList;
        }
        Map<String,String> map = new HashMap<>();
        for(String str:list2)
        {
            map.put(str,str);
        }
        for(String str:list1)
        {
            if(!map.containsKey(str))
            {
                stringList.add(str);
            }
        }
        //System.out.println(list1+"-"+list2+"="+stringList);
        return stringList;
    }

    //按首字母对项中元素排序
    public static List<String> orderList(List<String> list)
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
                    if(str.charAt(0)<s.charAt(0))
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


}
