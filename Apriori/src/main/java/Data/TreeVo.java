package Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeVo {
    private final String element;
    private Integer supportFrequency;

    private List<TreeVo> nodeList;

    public TreeVo(String element, Integer supportFrequency) {
        this.element = element;
        this.supportFrequency = supportFrequency;
    }

    public void setNodeList(List<TreeVo> nodeList) {
        this.nodeList = nodeList;
    }

    public List<TreeVo> getNodeList() {
        return nodeList;
    }

    public String getElement() {
        return element;
    }

    public Integer getSupportFrequency() {
        return supportFrequency;
    }

    public void setSupportFrequency(Integer supportFrequency) {
        this.supportFrequency = supportFrequency;
    }

    //检测是否已经含有这样的子节点
    public boolean isContain(String element)
    {
        if (this.nodeList != null) {
            for (TreeVo treeVo : nodeList) {
                if (treeVo.getElement().equals(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    //检测子树中是否含有这样的子节点，并将含有这样的子节点的子树存入
    public void isContainAll(String element, TreeVo p, List<String> stringList, Integer minSupport, Map<List<String>,Integer> map,TreeVo root)
    {
        //boolean flag = false;
        int support = p.getSupByElement(element,p.getElement(),root);
        if(support >= minSupport)
        {
            stringList.add(p.getElement());
            map.put(stringList,support);
            if (p.nodeList != null)
            {
                for (TreeVo treeVo : p.nodeList)
                {
                    List<String> stringList1 = new ArrayList<>(stringList);
                    isContainAll(element,treeVo,stringList1,minSupport,map,root);
                }
            }
        }
    }

    //获取所有以p.element开头，element结尾的子树
    public void getChildTree(TreeVo p,String element,Map<List<String>,Integer> map,List<String> stringList)
    {
        stringList.add(p.getElement());
        if(p.getElement().equals(element) && stringList.size()>1)
        {
            map.put(stringList,p.getSupportFrequency());
            return;
        }
        if(p.getNodeList() != null)
        {
            for(TreeVo treeVo:p.getNodeList())
            {
                List<String> childStringList = new ArrayList<>(stringList);
                getChildTree(treeVo,element,map,childStringList);
            }
        }
    }


    //找到该节点下element的sup值
    public int getSup(String element,TreeVo p)
    {
        int t = 0;
        if(p.nodeList != null)
        {
            for(TreeVo treeVo:p.nodeList)
            {
                t+=getSup(element,treeVo);
                if(treeVo.getElement().equals(element))
                {
                    t+=treeVo.getSupportFrequency();
                }
            }
        }
        return t;
    }

    //找到与root具有相同element的节点下element的sup值之和
    public int getSupByElement(String element,String root,TreeVo p)
    {
        int t=0;
        if(p.getElement()!=null && p.getElement().equals(root))
        {
            t = getSup(element, p);
            return t;
        }
        if(p.nodeList != null)
        {
            for(TreeVo treeVo:p.nodeList)
            {
                t += getSupByElement(element,root,treeVo);
            }
        }
        return t;
    }

    //检测两节点是否相同
    public boolean isEqual(String element)
    {
        return this.getElement().equals(element);
    }

    //增加支持度
    public void addSupport()
    {
        this.supportFrequency++;
    }

    //n为深度,对树进行深度遍历并打印
    public void printTree(int depth)
    {
        System.out.println();
        for(int i=0;i<3*depth;i++)
        {
            System.out.print(" ");
        }
        //System.out.println(n+": element:"+element+" support:"+supportFrequency);
        System.out.print(depth+":"+element+"("+supportFrequency+")");
        if(nodeList != null)
        {
            for(TreeVo treeVo:nodeList)
            {
                treeVo.printTree(depth+1);
            }
        }
    }
}
