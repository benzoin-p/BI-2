import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ID3 {

    Document xmldoc;
    Element root;

    //存储属性的名称
    //e.g.[outlook,temperature,play]
    private List<String> attribute = new ArrayList<>();

    //存储每个属性的取值
    //e.g.[[sunny,overcast,rainy],[hot,mild,cool],[yes,no]]
    private List<List<String>> attributeValue = new ArrayList<>();

    //原始数据
    //e.g.[[sunny,hot,no],[sunny,mild,yes],[overcast,hot,yes],[sunny,cool,no]]
    private List<List<String>> data = new ArrayList<>();

    //决策变量在属性集中的索引,即返回决策变量在attribute中的下标
    int decisionIndex;

    ID3(File file,String decision,Element root,Document xmldoc)
    {
        ArffReader.readArff(file,attribute,attributeValue,data);
        this.decisionIndex = getDec(decision,attribute);
        this.root = root;
        this.xmldoc =xmldoc;
    }

    //初始化列表，存储了不包括分类属性的其他属性的索引
    public List<Integer> initAttIndexList()
    {
        List<Integer> attIndexList = new ArrayList<>();
        for(int i=0;i<attribute.size();i++)
        {
            if(i != decisionIndex)
            {
                attIndexList.add(i);
            }
        }
        return attIndexList;
    }

    public List<Integer> initDataSubset()
    {
        List<Integer> dataSubset = new ArrayList<>();
        for(int i=0;i<data.size();i++)
        {
            dataSubset.add(i);
        }
        return dataSubset;
    }


    //构建决策树
    public void buildDT(String name, String value, List<Integer> dataSubset, List<Integer> attIndexList)
    {
        Element ele = null;
        List<Node> list = root.selectNodes("//"+name);
        Iterator<Node> iterator = list.iterator();
        //通过遍历找到树中与传入value相等的节点
        while(iterator.hasNext())
        {
            ele = (Element) iterator.next();
            if(ele.attributeValue("value").equals(value))
            {
                break;
            }
        }
        //递推结束的出口,即分类为y/n,当dataSubSet中所有项的分类取值都相同时取真
        if(isPure(dataSubset))
        {
            ele.setText(data.get(dataSubset.get(0)).get(decisionIndex));
            return;
        }
        if(ele == null)
        {
            return;
        }
        //获取在选择哪个作为节点的情况下熵最小（数值增益最大）
        int minIndex = -1;
        double minEntropy = Double.MAX_VALUE;
        for (Integer i : attIndexList)
        {
            double entropy = calNodeEntropy(dataSubset, i);
            //System.out.println(entropy);
            if (entropy < minEntropy)
            {
                minEntropy = entropy;
                minIndex = i;
            }
        }
        //从不含分类项的属性索引数组中移除该属性,并将该属性的不同取值作为分支添加到该节点
        String nodeName = attribute.get(minIndex);
        attIndexList.remove(new Integer(minIndex));
        List<String> attValueList = attributeValue.get(minIndex);
        for(String s:attValueList)
        {
            ele.addElement(nodeName).addAttribute("value",s);
            //将已经加入决策树的属性移除，递推调用该函数
            List<Integer> newDataSubset = new ArrayList<>();
            for(Integer i:dataSubset)
            {
                //如果该数据项的该属性为s，则加入进行递推
                if(data.get(i).get(minIndex).equals(s))
                {
                    newDataSubset.add(i);
                }
            }
            buildDT(nodeName,s,newDataSubset,attIndexList);
        }
    }

    //计算在某种划分下样本关于某个分类（为y/n）的熵
    public double getEntropy(List<Double> pList)
    {
        double entropy = 0.0;
        for(Double t:pList)
        {
            if(t == 0)
            {
                entropy -= 0;
            }
            else
            {
                entropy -= t*getLog2t(t);
            }
        }
        return entropy;
    }

    //求以2为底的一个数的对数
    public double getLog2t(double t)
    {
        return Math.log(t)/Math.log(2);
    }

    //计算在某个划分作为选择指标时的信息增益（实质计算的为熵）
    public double calNodeEntropy(List<Integer> dataSubset,int attValueIndex)
    {
        /*
        * 思路：通过传入的dataSubset遍历data中index属于dataSubset的项
        * 对每个项的第index项的值（如v1，v2，v3）和第decisionIndex项的值（y，n）进行统计
        * 获取p（y|vi）和p(n|vi),传入getEntropy
        * 不能这么算，也许会发生负上溢,当x->0,xlog2x->0
        * 节点总熵=sum(count（vi）/sum（count(vi)）*getEntropy(vi))*/
        double entropy = 0.0;
        /*构造m*n的矩阵存储各取值取y/n的count值，
        其中m为attIndex所对应的属性的可取值数，n为decisionIndex所对应的属性的可取值数
        * 构造长度为m的数组存储各取值的count值*/
        int[][] countYN = new int[attributeValue.get(attValueIndex).size()][];
        int[] count = new int[attributeValue.get(attValueIndex).size()];
        for(int i=0;i<countYN.length;i++)
        {
            countYN[i] = new int[attributeValue.get(decisionIndex).size()];
        }
        //对countYN矩阵赋值
        for(Integer i:dataSubset)
        {
            List<String> dataItem = data.get(i);
            String attValue = dataItem.get(attValueIndex);
            int m = attributeValue.get(attValueIndex).indexOf(attValue);
            String decValue = dataItem.get(decisionIndex);
            int n = attributeValue.get(decisionIndex).indexOf(decValue);
            count[m]++;
            countYN[m][n]++;
        }
        //获取概率，并通过概率获取熵
        for(int i=0;i<countYN.length;i++)
        {
            List<Double> pList = new ArrayList<>();
            for(int j=0;j<countYN[i].length;j++)
            {
                Double p = (1.0*countYN[i][j])/(1.0*count[i]);
                pList.add(p);
            }
            entropy += getEntropy(pList)*count[i]/dataSubset.size();
        }
        return entropy;
    }

    //判断是否已经在该条路径上分类结束
    public boolean isPure(List<Integer> dataSubset)
    {
        String s = data.get(dataSubset.get(0)).get(decisionIndex);
        for(int i=0;i<dataSubset.size();i++)
        {
            String ss = data.get(dataSubset.get(i)).get(decisionIndex);
            if(!s.equals(ss))
            {
                return false;
            }
        }
        return true;
    }

    //获取决策变量
    public int getDec(int n,List<String> attribute) {
        if (n<0 || n>=attribute.size()) {
            System.err.println("决策变量指定错误。");
            System.exit(2);
        }
        return n;
    }

    public int getDec(String name,List<String> attribute) {
        int n = attribute.indexOf(name);
        return getDec(n,attribute);
    }

    // 把xml写入文件
    public void writeXML(String filename)
    {
        try
        {
            File file = new File(filename);
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file);
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter output = new XMLWriter(fw, format);
            output.write(xmldoc);
            output.close();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
