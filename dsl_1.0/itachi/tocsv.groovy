import groovy.sql.GroovyResultSet
import groovy.sql.Sql

import java.sql.ResultSetMetaData
import java.sql.Statement

def oracledbtocsv(String hostPortDatabaseName, String username, String password, String sqlQuery, String outputFilename)
{
	dbtocsv(oracleUrl(hostPortDatabaseName), username, password, sqlQuery, outputFilename)
}

def mysqldbtocsv(String hostPort, String databaseName, String username, String password, String sqlQuery, String outputFilename)
{
	dbtocsv(mysqlUrl(hostPort, databaseName), username, password, sqlQuery, outputFilename)
}

def dbtocsv(String urlString, String username, String password, String sqlQuery, String outputFilename)
{
	db {

		info urlString, username, password

		query {

			str sqlQuery
			tocsv outputFilename
			exec()

		}
	}
}

private String mysqlUrl(String hostPort, String databaseName)
{
	return "jdbc:mysql://$hostPort/$databaseName"
}

private String oracleUrl(hostPortDatabaseName)
{
	return "jdbc:oracle:thin:@$hostPortDatabaseName"
}

DB db(Closure closure)
{
	DB db = new DB()
	closure.delegate = db
	closure()
	return db
}

class DB
{
	private String url;
	private String username;
	private String password;

	private Sql sql

	private Set<String> availableTables = []

	void info(String url, String username, String password)
	{
		this.url = url
		this.username = username
		this.password = password
	}

	private String driver()
	{
		if (url.contains("mysql"))
			return "com.mysql.jdbc.Driver"
		else if (url.contains("oracle"))
			return "oracle.jdbc.driver.OracleDriver"
		else
			return null
	}

	private void initSql()
	{
		if (sql == null)
			sql = Sql.newInstance(url, username, password, driver())

		if (url.contains('oracle'))
			sql.withStatement {Statement stmt ->

				try
				{
					stmt.fetchSize = 10000
				}
				catch (Exception ex)
				{
					log "Exception while initing stmt"
					ex.printStackTrace()
				}
			}
	}

	void query(Closure closure)
	{
		Query q = new Query()
		closure.delegate = q
		closure()
	}

	boolean tableExists(String table)
	{
		initSql()

		if (availableTables.contains(table))
			return true;

		def tables = sql.connection.getMetaData().getTables(null, null, table, null)
		boolean exists = tables.next()

		if (exists)
			availableTables.add(table)

		return exists;
	}

	boolean dropTable(String table)
	{
		if (tableExists(table))
			sql.execute("drop table $table")
	}

	String getInsertIntoStringWithPlaceHolders(String table, int sizeOfArguments)
	{
		def qs = []
		sizeOfArguments.times {qs << '?'}

		def v = qs.join(',')
		return "insert into $table values ($v)"
	}

	void insertBatch(String table, List<String> colNames, List<Collection<Object>> rowValues, int batchSize)
	{
		if (rowValues == null || rowValues.empty)
			return

		createTableIfNotExists(table, colNames)

		def phQuery = getInsertIntoStringWithPlaceHolders(table, rowValues.get(0).size())

		sql.withBatch(batchSize, phQuery) { ps ->

			rowValues.each { ps.addBatch(it.toArray()) }
		}
	}

	void createTableIfNotExists(String table, List<String> colNames)
	{
		if (tableExists(table))
			return;

		def cols = []

		colNames.each {cols << "$it varchar(255)"}

		def str = cols.join(", ")

		def insertQuery = "create table $table ($str)"

		println "insertQuery : $insertQuery"

		initSql()

		sql.execute(insertQuery);

		availableTables.add(table)
	}

	void insertRow(String table, Collection row)
	{
		String placeHolderQuery = getInsertIntoStringWithPlaceHolders(table, row.size())
		//println "query = $s"

		initSql()

		sql.executeInsert(placeHolderQuery, row.toArray(new Object[0]))
	}

	def getClose()
	{
		if (sql != null)
			sql.close()
	}

	class Query
	{
		String sopHeader
		String str
		Closure headerClosure
		List<Closure> rowClosures = []
		Closure resultsetMetadataClosure
		List<Closure> lastClosures = []

		Closure batchClosure
		int batchSize = -1

		int progressRowCount = -1

		void str(String str)
		{
			this.str = str
		}

		void prefixSopWith(String sopHeader)
		{
			this.sopHeader = sopHeader
		}

		void header(Closure headerClosure)
		{
			this.headerClosure = headerClosure
		}

		void last(Closure closure)
		{
			lastClosures << closure
		}

		void meta(Closure resultsetMetadataClosure)
		{
			this.resultsetMetadataClosure = resultsetMetadataClosure
		}

		void showProgressForEvery(int rowCount)
		{
			this.progressRowCount = rowCount
		}

		void eachRow(Closure rowClosure)
		{
			this.rowClosures << rowClosure
		}

		void eachBatch(int batchSize, Closure batchClosure)
		{
			this.batchSize = batchSize
			this.batchClosure = batchClosure
		}

		void exec()
		{
			log "Starting..."

			initSql()

			log "Inited sql..."

			def colNames = []

			long start = System.currentTimeMillis()

			log "Calling sql.eachRow()..."

			def batch = []

			def rowsCount = 0

			sql.eachRow(str,

					{ ResultSetMetaData m ->

						log "meta : " + new Date().toString()

						for (int i = 1; i <= m.columnCount; i++)
							colNames.add(m.getColumnName(i))

						if (resultsetMetadataClosure != null)
							resultsetMetadataClosure.call(m)

						if (headerClosure != null)
							headerClosure.call(colNames)
					},
	
					{GroovyResultSet row ->

						rowsCount++
						//log new Date().toString()
						rowClosures.each { it.call(row) }

						if (progressRowCount != -1)
						{
							if (rowsCount % progressRowCount == 0)
								log "rowsProcessed = " + row.getRow()
						}

						if (batchClosure != null)
						{
							batch << row.toRowResult().values()

							if (batch.size() % batchSize == 0)
							{
								batchClosure.call(colNames, batch)
								batch.clear()
							}
						}
					}
			)

			if (batchClosure != null && !batch.isEmpty())
			{
				batchClosure.call(colNames, batch)
				batch.clear()
			}

			lastClosures.each {it.call()}

			log "Total rows processed : $rowsCount"

			long end = System.currentTimeMillis()

			log "Time taken for query = " + (end - start) + " ms"
		}

		private void log(String msg)
		{
			println new Date().toString() + " : $sopHeader : $msg"
		}

		Collection array(GroovyResultSet row)
		{
			return row.toRowResult().values()
		}

		void tocsv(String filename)
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)))

			header {header -> bw.writeLine(header.join(','))}

			eachRow {GroovyResultSet row -> bw.writeLine(array(row).join(','))}

			last {bw.close()}
		}

		void todb(DB db, String table, int batchSize)
		{
			db.initSql()

			eachBatch(batchSize) {List<String> colNames, List<Collection<Object>> rows ->

				db.insertBatch(table, colNames, rows, batchSize)
			}
		}
	}
}

//------------------------------------------------------------------------------------------------------------------------------------------------------

def oracledbtocsv(String hostPortDatabaseName, String username, String password, String sqlQuery, String outputFilename, Closure closure)
{
	clsdbtocsv(oracleUrl(hostPortDatabaseName), username, password, sqlQuery, outputFilename, closure)
}

def mysqldbtocsv(String hostPort, String databaseName, String username, String password, String sqlQuery, String outputFilename, Closure closure)
{
	clsdbtocsv(mysqlUrl(hostPort, databaseName), username, password, sqlQuery, outputFilename, closure)
}


def clsdbtocsv(urlString, username, password, sqlQuery, outputFilename, closure)

{

	def driver = ""

	if (urlString.contains('mysql'))
	{
		 driver = "com.mysql.jdbc.Driver"
	}

	else
	{
		driver = "oracle.jdbc.driver.OracleDriver"
	}

def sql = Sql.newInstance(urlString, username, password, driver)
def del = ","
boolean idx = 1
File myfile = new File( outputFilename);
FileWriter f = new FileWriter(myfile, false);

sql.rows(sqlQuery).each{it.each{key,val-> 
    Format format = new Format()
    format.key = key
    format.val = val
    closure.delegate = format
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.call()
    key = format.key
    val = format.val
    del = format.delimiter
	if(idx){f.append (key+"$del")}}
f.append("\n")
it.each{key,val->
f.append (val+"$del")}
idx=0;
}
f.close()
}


class Format
{
    def str
    def key
    def val
    def delimiter
    void transform (String str, Closure cls) {
        this.str = str
        if (key==str){
       	Pass p = new Pass()
        p.val=val
        cls.delegate=p
        cls.resolveStrategy = Closure.DELEGATE_FIRST
        cls.call()
        this.val=p.val
        println p.val
    }
}
    void delimiter(String delimiter){
    	this.delimiter=delimiter
    }

    class Pass
    {
    	String key
    	String val
    } 
}
