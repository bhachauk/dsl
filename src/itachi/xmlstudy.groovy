import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

Xml xmlstudy (Closure cl) {

    def xml = new Xml()
    cl.delegate = xml
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    return xml
}

class Xml {

    def root
    String rowpath
    List columnfields =[]
    boolean isattr
    def attr_name
    List rowdata=[]
    ArrayList<ArrayList<String>> data=new ArrayList<ArrayList<String>>()
    String delimiter=','
    List bind_node=['','']

    void xmltxt (String content)
    {
        root = new XmlSlurper().parseText(content)
    }
    void xmlfile (String filename)
    {
        root = new XmlSlurper().parse(new File(filename))
    }

    void add_root(String root_name='root')
    {
        def newXml = new groovy.xml.StreamingMarkupBuilder().bind {

            "$root_name" {
                mkp.yield root
            }
        }

        root = new XmlParser().parseText(groovy.xml.XmlUtil.serialize(newXml))

    }

    void set_delimiter(String del)
    {
        this.delimiter=del
    }

    void set_binders(String init,String last)
    {
        this.bind_node=[init,last]
    }

    def xpath(def n,def r){

        n.split('\\.').inject(r) { r1, n1 -> r1."$n1"}

    }

    List getAllchildren(String nodepath)
    {
        def childrenlist =[]
        xpath(nodepath,root).children().each{ !childrenlist.contains(nodepath+'.'+it.name()) ? childrenlist.add(nodepath+'.'+it.name()): {}}
        return childrenlist
    }
    List getinstantchildrenattribs (String nodepath)
    {
        def childrenlist =[]
        xpath(nodepath,root).children().each{node->
            node.attributes().each{k,v ->
                childrenlist << nodepath+'.'+node.name()+'@'+k
            }
        }
        return childrenlist.unique()
    }

    List getinstantAll (String nodepath)
    {
        return getinstantchildren(nodepath)+getinstantchildrenattribs(nodepath)
    }

    List getinstantchildren(String nodepath)
    {

        return removehavingchild(getAllchildren(nodepath))
    }

    Closure removehavingchild ={
        it.retainAll{ xpath(it,root).children()=='' }
        it
    }

    String row (String path)

    {
        this.rowpath=path

        return rowpath
    }

    List column (String... cpath)
    {
        for (String eachpath : cpath){

            this.columnfields.add (eachpath)
        }
        return columnfields

    }

    List column (List cpathlist)
    {

        this.columnfields += cpathlist

        return columnfields
    }

    def go_to_node(String node, def row)
    {
        int common_node=0
        node.split("\\.").eachWithIndex{it,id-> if(it!=rowpath.split("\\.")[id]){common_node=id; return}}
        common_node ==0 && rowpath.contains(node) ? common_node=node.split("\\.").size() : {}
        def fromroot=node.tokenize("\\.").drop(common_node)
        def parent=rowpath.tokenize("\\.").drop(common_node)
        def crow=row

        1.upto(parent.size()){
            crow=crow.parent()
        }

        return (fromroot==[] ? crow : xpath(fromroot.join('.'),crow))
    }

    List result(Closure cl)
    {
        xpath(rowpath,root).each{ row ->

            rowdata=[]

            columnfields.eachWithIndex{ node,j->

                if(node.contains('@')){

                    attr_name=node.split('@')[1]

                    node=node.split('@')[0]

                    isattr=true

                }
                else
                {
                    isattr=false
                }

                if (node.contains(rowpath))
                {
                    if(node==rowpath)
                    {
                        isattr ? collect_attribute(row,attr_name,j,bind_node):collect_value (row,j,bind_node)

                    }else{

                        node=node.minus(rowpath+'.')

                        isattr ? xpath(node,row).each{collect_attribute(it,attr_name,j,bind_node)}:xpath(node,row).each{collect_value(it,j,bind_node)}

                    }
                }
                else
                {
                    def current_node=go_to_node(node,row)

                    isattr ? collect_attribute(current_node,attr_name,j,bind_node) : current_node.each{ collect_value(it,j,bind_node)}
                }


            }
            (rowdata.size()!=columnfields.size()) ? dothis(columnfields.size(),rowdata): {}
            def acc_row = new Access_row()
            cl.delegate = acc_row
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call(rowdata)
        }
        return data
    }
    void collect_value(def nodename, int j,def bind_node)
    {
        (j>rowdata.size()) ? dothis(j,rowdata) : {}

        (rowdata[j] != null) ? rowdata[j]=rowdata[j]+delimiter+bind_node[0]+nodename.text()+bind_node[1]:rowdata.add(j,bind_node[0]+nodename.text()+bind_node[1])
    }

    void collect_attribute(def nodename,String attr, int j,def bind_node)
    {
        (j>rowdata.size()) ? dothis(j,rowdata) : {}

        (rowdata[j] != null) ? rowdata[j]=rowdata[j]+delimiter+bind_node[0]+nodename.attributes()[attr]+bind_node[1]:rowdata.add(j,bind_node[0]+nodename.attributes()[attr]+bind_node[1])

    }

    def dothis(int j,List rowdata)
    {
        1.upto(j-rowdata.size()){ rowdata.add('NULL')}
    }

    class Access_row
    {
        void printraw(String sep='|')
        {
            println rowdata.join(sep)
        }
        void collectdata()
        {
            data.add(new ArrayList<>(rowdata))
        }
        void trim()
        {
            rowdata=rowdata.collect{it.trim()}
        }
    }
}