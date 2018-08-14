//System.properties.each {println it}

def groovyHome = System.properties['groovy.home']

def cmd = []

cmd << "$groovyHome/bin/groovy"
//cmd << "groovy"
cmd << "-cp"

def classpath = ""

new File('../lib/ext').listFiles().each {

	if (it.name.endsWith('.jar'))
		
		classpath += File.pathSeparator + it.absolutePath
		//classpath +=it.absolutePath+' '
}

classpath = '../lib/ext/*'

cmd << classpath
cmd << 'main'
cmd << args[0]

//println "cmd : " + cmd

//println classpath

ProcessBuilder builder = new ProcessBuilder()

builder.command((String[]) cmd.toArray())
builder.inheritIO()

def process = builder.start()

def ret = process.waitFor()

println "Done ! Exit Status : $ret"

