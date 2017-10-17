FileStudy filestudy(Closure closure)
{
	FileStudy fileStudy = new FileStudy()
	closure.delegate = fileStudy
	closure()
	return fileStudy
}

class FileStudy
{
	List<String> filename= []
	String processname='fieldprocess'
	String fieldname
	String line=''
	String startdel
	String enddel
	String delimiter
	def fieldvalues=[]
	String alamstartstring
	String alarmendstring
	List<String> matchfieldvalues=[]
	List<String> andmatchfieldvalues=[]
	boolean headerended=false
	String andfiltervalues=""

	void file(String... filename)
	{
		this.filename=filename
	}

	def valuebetween(String startdel, String enddel) {

        this.startdel = startdel
        this.enddel = enddel
    	//return { String s, String s2 -> /* do something here */}
    }

    Map search(String fieldname, String delimiter = '=') {

        this.fieldname = fieldname
        this.delimiter = delimiter
        ['in': { it(startdel, enddel) }]

    }

    def also(String... andfiltervalues) {

        for (String eachvalue : andfiltervalues) {

            andmatchfieldvalues.add(eachvalue)

        }
    }

    Map where(String... filtervalues) {

        for (String eachvalue : filtervalues) {

            matchfieldvalues.add(eachvalue)
        }

        ['and': { also(it) }]
    }

	def fieldprocess={String line->

		if(line.contains(fieldname)){

		startdel == null && enddel == null ? process(line) : process(line.substring(line.indexOf(startdel),line.indexOf(startdel)+line.substring(line.indexOf(startdel)).indexOf(enddel))) //
	}
	}

	def process={

	if(it.contains(delimiter)){

	it.tokenize(delimiter).with{if(it[0]==fieldname){

		 		fieldvalues.add(it[1])
		 	}
		 	}
		}
		 	
	}

	def filterprocess={it->

			if (matchfieldvalues==null || matchfieldvalues.empty)
			{ 
				fieldprocess(line)

				
			}else{

				if(matchfieldvalues.any{line.contains(it)}){

					if (andmatchfieldvalues==null || andmatchfieldvalues.empty)
					{
						fieldprocess(line)
					} 
					else
					{
						if(andmatchfieldvalues.any{line.contains(it)})
						{
							fieldprocess(line)							
						}

		
					} 
					
				}
			}

	}

	def alarmprocess={it->

		if (it.contains(alamstartstring))
		{ 

			if(headerended==true && alarmendstring==""){

				filterprocess(line)
			}

			line=""
			line+=it
			headerended=true

		}else if (headerended){ 

			line=line+it

			if(it.contains(alarmendstring)&& alarmendstring !=""){

				filterprocess(line)
				
				}
				
		}
	}

	void launchfileto(String processname){

		for (String eachfile : this.filename){

		new File (eachfile).eachLine {it-> "$processname"(it)}

		}

	}

	def patternflag (String alamstartstring, String alarmendstring="")
	{
		this.alamstartstring=alamstartstring
		this.fieldvalues=fieldvalues
		this.alarmendstring=alarmendstring
		this.processname='alarmprocess'
	}

	def showdistinct(){

		launchfileto(processname)
		fieldvalues.unique().each{println it}
	}

	def showall(){

		launchfileto(processname)
		this.fieldvalues.each{println it}

	}

	def showgroupby(){

		launchfileto(processname)
		def groupbymap = fieldvalues.inject([:]) { groupbymap, x -> if (!groupbymap[x]) groupbymap[x] = 0; groupbymap[x] += 1; groupbymap }
		groupbymap.each{println it}
	}
}

