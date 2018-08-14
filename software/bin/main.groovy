
if (args.length == 0)
{
	println "main script missing. :("
	System.exit(1)
}

def mainScript = args[0]

def dslDir = new File("../lib/int")

def files = dslDir.listFiles()

def script = ""

files.each {script += '\n' + it.text}

script += '\n' + new File(mainScript).text

def shell = new GroovyShell()

shell.evaluate(script)