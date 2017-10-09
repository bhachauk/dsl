
if (args.length == 0)
{
	println "main script missing. :("
	System.exit(1)
}

def mainScript = args[0]

def dsldir = new File("../itachi")

def files = dsldir.listFiles()

def script = ""

files.each {script += '\n' + it.text}

script += '\n' + new File(mainScript).text

def shell = new GroovyShell()

//println script

shell.evaluate(script)

//shell.evaluate(new File(mainScript).text)
