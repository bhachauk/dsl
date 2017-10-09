/*
db {

	info ('jdbc:oracle:thin:@192.168.9.56:1521/DB11G','imhuawei19042017','imhuawei19042017')

	query {
		
		str "select * from ManagedObject where rownum < 10"

		eachRow {println it}

		tocsv "a.csv"

		exec()
	}
}

oracledbtocsv('192.168.9.56:1521/DB11G','imhuawei19042017','imhuawei19042017','select * from ManagedObject where rownum<10','output.txt'){
delimiter(',')
}
*/
mysqldbtocsv('localhost','bc','root','bhachauk','select * from bhanuptp','file_name.csv'){
delimiter(',')

transform('NAME'){
 val=val.toUpperCase()
 println val
}

}
