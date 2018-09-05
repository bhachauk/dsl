import com.github.opendevl.*
import groovy.json.*

static Json jsonstudy (Closure cl)
{
    def jsonO = new Json()
    cl.delegate = jsonO
    cl()
    return jsonO
}

class Json {

    def json
    String rowpath
    List columnlist = []
    List<List<Object>> rowdata = [[]]
    List makeEachcol = []

    ArrayList<ArrayList<Object>> jsondata = new ArrayList<ArrayList<String>>()

    def jpath (def json, String rowpath)
    {
        rowpath.tokenize('.').inject(json) { j, str ->
            try {
                j."$str"
            }
            catch(NullPointerException e){
                null
            }
        }
    }

    def jpathAtnode (def json, String rowpath)
    {
        rowpath.tokenize('.').inject(json) { j, str ->
            try {
                if (str.matches(".*\\d.*") && str.contains('['))
                    j."${str.left(str.indexOf('['))}"["${str.substring(str.indexOf('[') + 1, str.indexOf(']'))}".toInteger()]
                else
                    j."$str"
            }catch (NullPointerException e)
            {
                null
            }
        }
    }
    void jsontxt (String content)
    {
        json =new JsonSlurper().parseText(content)
    }
    void jsonfile (String filename)
    {
        jsontxt (new File (filename).text)
    }

    void row(String row)
    {
        this.rowpath = row
    }
    void column (List<String> columns)
    {
        columnlist+=columns
    }
    void column (String... columns)
    {
        for (String eachpath : columns)
        {
            columnlist.add(eachpath)
        }
    }
    List <String> getinstantnodesof(String inputnode)
    {
        def instlist=getAllValNodes()
        instlist.retainAll{ele-> ele==inputnode||ele.contains(inputnode)&&!ele.minus(inputnode+'.').contains('.')}
        return instlist
    }
    List<String> getAllNodes()
    {
        List<String> list = []
        traverse(json).each { list.add(it.replaceAll("[^A-Za-z.]", "")) }
        return list.unique()
    }
    List<String> getAllValNodes()
    {
        List<String> filterlist=getAllNodes()
        filterlist.removeIf{
            getAllNodes().minus(it).any{val-> val.contains(it)}
        }
        return filterlist
    }
    Map<String,String> getJsonTreeMap()
    {
        def vallist=getAllValNodes()
        def map =[:]
        traverse(json).each{ele->
            if (vallist.any{ it==(ele.replaceAll('[^A-Za-z.]',''))})
            {
                map[ele]=jpathAtnode(json,ele)
            }
        }
        return map
    }
    void printTree()
    {
        new IterJson().printTree()
    }
    def traverse = { tree, keys = [], prefix = '' ->
        switch (tree) {
            case Map:
                tree.each { k, v ->
                    def name = prefix ? "${prefix}.${k}" : k
                    keys << name
                    traverse(v, keys, name)
                }
                return keys
            case Collection:
                tree.eachWithIndex { e, i -> traverse(e, keys, "${prefix}[$i]") }
                return keys
            default:
                return keys
        }
    }

    String makeAsCell(Object val)
    {
        return '"'+val+'"'
    }

    void makeEach (String columnPath)
    {
        columnlist.each {
            if (it.contains(columnPath))
                makeEachcol.add (it)
        }
    }

    void makeEach (List<String> columnPath)
    {
        if (columnlist.containsAll(columnPath))
            makeEachcol += columnPath
    }

    List result(Closure closure) {

        jpath(json,rowpath).each { row ->

            rowdata.clear()

            columnlist.eachWithIndex { node, id ->
                if (node == rowpath)
                {
                    row!= null ? row instanceof List ? rowdata << row : rowdata << makeAsCell(row) : rowdata.add(makeAsCell(null))
                }
                else if (node.contains(rowpath)) {

                    node = node.minus(rowpath + '.')

                    jpath(row, node) != null ? jpath(row, node) instanceof List ? rowdata << jpath(row, node) : rowdata << makeAsCell(jpath(row, node)) : rowdata.add(makeAsCell(null))


                } else {

                    jpath(json, node) != null ? jpath(json, node) instanceof List ? rowdata << makeAsCell(jpath(json, node).join('|')) : rowdata << makeAsCell(jpath(json, node)) : rowdata.add(makeAsCell(null))
                }

            }

            def acc_row = new Access_row()
            closure.delegate = acc_row
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call(rowdata)
        }
        return jsondata
    }

    class Access_row
    {
        Access_row()
        {
            if (makeEachcol)
            {
                List<Integer> idcs= columnlist.findIndexValues { makeEachcol.contains(it)}.collect{it.toInteger()}
                log.trace('Selected cols : {} Instance : {} Sizes : {}',columnlist[idcs],rowdata[idcs]*.class.name, rowdata[idcs]*.size())

                if (rowdata[idcs]*.size().unique().size() != 1 && rowdata[idcs].any{ !it instanceof List})
                    throw (new Exception('Invalid makeEach columns'))

                if (rowdata[idcs][0].size() == 0)
                    rowdata=[rowdata]
                else
                    rowdata = (0 ..< rowdata[idcs][0].size()).collect{ ix ->
                        rowdata.withIndex().collect{val,idx-> (idx in idcs) ? val[ix] : val }
                    }
            }
            rowdata = rowdata.collect { rows ->
                rows.collect {
                    it instanceof List ? makeAsCell(it) : it
                }
            }
        }
        void trim()
        {
            rowdata=rowdata.collect{it.trim()}
        }
        void printraw(String sep = ',')
        {
            rowdata.each{ println(it.join(sep))}
        }

        void collectdata()
        {
            !rowdata.isEmpty() ? rowdata.each {rows -> jsondata.add(new ArrayList<>(rows))} : {}
        }

    }
    class IterJson
    {
        Closure filterClosure

        IterJson() {
            if (columnlist.isEmpty())
                filterClosure = {nodeLine,name-> println nodeLine}
            else
                filterClosure = {nodeLine,name ->
                    if (columnlist.any{ it.contains(name)})
                        println(nodeLine)
                }
        }

        def printTree = { tree=json, height=0,keys = [], prefix = '' ->
            switch (tree) {
                case Map:
                    height++;
                    tree.each { k, v ->
                        def name = prefix ? "${prefix}.${k}" : k
                        keys << name
                        String nodeLine = (('\t'*(height-1))+"|__ ${height}. " +k)
                        switch (v){
                            case {it instanceof Map || it instanceof Collection || it instanceof List}:
                                filterClosure.call(nodeLine,name)
                                printTree(v,height,keys,name)
                                break
                            default:
                                nodeLine += ' = '+ v
                                filterClosure.call(nodeLine,name)
                        }
                    }
                    break
                case Collection:
                    tree.each { v ->
                        printTree(v,height,keys,prefix)
                    }
                    break
            }
        }
    }
}