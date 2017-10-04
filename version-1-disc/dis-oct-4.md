##### Groovy DSL

#### Error Line

- def sql = Sql.newInstance("jdbc:mysql://192.168.11.170:3306",a,b,"com.mysql.jdbc.Driver")
```
Caught: com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure. The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server. com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure. The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
```
Discussions:
- https://stackoverflow.com/questions/2102912/cant-make-jdbc-connection-to-mysql-using-java-intellij-and-linux
- https://bbs.archlinux.org/viewtopic.php?pid=346836#p346836
