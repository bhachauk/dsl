package io.github.bhanuchander210.filestudy
import org.apache.commons.lang3.StringUtils
import groovy.lang.MissingPropertyException

/**
 * <h1>FileStudy</h1>
 * <h1>Base Example</h1>
 * <pre>
 *     fileparse.filestudy{
 *         	inputFile 'testfile.txt'
 *         	filterLine 'searchString'
 *         	makePattern 'patternable String'
 *         	result{
 *         	    println line
 *         	}
 *     }
 * </pre>
 */

class FileStudyDSL {
	static FileStudy filestudy(Closure closure) {
		FileStudy fileStudy = new FileStudy()
		closure.delegate = fileStudy
		closure()
		fileStudy
	}
}

class FileStudy
{
	List<String> input_files = []
	List<String> filter_strings= []

	String line = ''
	String patternStartString
	String patternEndString

	boolean isHeaderEnded = false

	List<List<String>> collect_fields (List<List<Object>> input_fields)
	{
		List<List<String>> data = []
		result {
			List<String> row =[]
			input_fields.each
					{val->
						try {
							if (val.size() <= 2)
								row.add (getBetweenString(*val))
							else
								row.add (getBetween(*val))
						} catch (StringIndexOutOfBoundsException e)
						{
							throw e
						}
						catch (MissingMethodException e)
						{
							throw (new Exception("Invalid input : ${val}"))
						}
					}
			data.add(new ArrayList<> (row) )
		}
		return data
	}
	void inputFile (String... filename)
	{
		for (String eachfile : filename)
		{
			input_files.add(eachfile)
		}
	}

	void inputFile(List<String> filename)
	{
		input_files += filename
	}

	void filterLine (List<String> filter_str)
	{
		filter_strings += filter_str
	}

	void filterLine (String... filter_str)
	{
		for (String each : filter_str)
		{
			filter_strings.add (each)
		}
	}

	void makePattern(String start_str, String end_str='')
	{
		this.patternStartString = start_str
		this.patternEndString = end_str
	}

	def doPattern (String in_line, Closure cl)
	{
		if (in_line == '->EOF<-')
		{
			if (line.contains(patternStartString))
				line_access(line,cl)
		}
		else if (in_line.contains (patternStartString))
		{
			if (isHeaderEnded && patternEndString=="")
			{
				line_access(line,cl)
			}
			line = ""
			line += in_line
			isHeaderEnded = true
		}
		else if (isHeaderEnded)
		{
			line = line + in_line
			if (in_line.contains(patternEndString)&& patternEndString !=""){
				line_access(line,cl)
				line=""
			}
		}
	}

	void result(Closure cl)
	{
		input_files.each
				{ file-> new File (file).eachLine
						{ init_line->
							if (patternStartString != null)
								return doPattern(init_line,cl)
							else
								return line_access(init_line,cl)
						}
					if (patternStartString != null)
						doPattern('->EOF<-',cl)
				}
	}
	String line_access (String in_line, Closure cl)
	{
		line = in_line
		def acc_line = new Line_Access()
		cl.delegate = acc_line
		cl.resolveStrategy = Closure.DELEGATE_FIRST
		if (!filter_strings.isEmpty())
		{
			if (filter_strings.any{line.contains(it)})
				cl.call(line)
		}
		else
			cl.call(line)
	}

	class Line_Access
	{
		void trim()
		{
			line = line.trim()
		}
		void printRaw()
		{
			println line
		}

		Closure isExists = {str -> return line.contains(str.toString())}

		int getIndex (String str, int n)
		{
			StringUtils.ordinalIndexOf (line, str, n)
		}

		String getBetweenString (String before, String after='')
		{
			if (after == '' && isExists (before))
				getBetweenIdx (line.indexOf(before) + before.length(), line.length())

			else if (isExists(after) && isExists(before))
				getBetweenIdx (line.indexOf(before) + before.length(), line.indexOf(after))

			else
				null
		}

		String getBetween (String before, int b_id, String after, int a_id)
		{
			if (isExists(before) && isExists(after))
				return getBetweenIdx (getIndex(before,b_id)+before.length(),getIndex(after,a_id))
			else
				return null
		}

		String getBetweenIdx (int start_idx, int end_idx)
		{
			line.substring (start_idx, end_idx)
		}

		/**
		 * Over loaded for specify index
		 * @param before
		 * @param idx
		 * @return
		 */
		String getBetweenString (String before, int idx)
		{
			line.substring (getIndex(before,idx) + before.length(), line.length())
		}

		/**
		 * Formatting the uneven spaced / delimited lines into uniform format.
		 * @param removeDelimiter
		 * @param insertDelimiter
		 * @return
		 */
		String makePrettyLine (String removeDelimiter, String insertDelimiter)
		{
			trim()
			line = line.replaceAll("($removeDelimiter)+", insertDelimiter)
		}
	}
}