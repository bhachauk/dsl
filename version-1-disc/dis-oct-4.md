##### Groovy DSL

#### Error Line 1

- def sql = Sql.newInstance("jdbc:mysql://192.168.11.170:3306",'user','pswd',"com.mysql.jdbc.Driver")
```
Caught: com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure. The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server. com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure. The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
Caused by: java.net.NoRouteToHostException: No route to host (Host unreachable)
```

#### Error Line 2
- url 'jdbc:mysql://ip:portnumber/servicename'

```
Caused by: java.io.EOFException: Can not read response from server. Expected to read 4 bytes, read 0 bytes before connection was unexpectedly lost.
Caught: com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure

The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure

The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
```
Discussions:
- https://stackoverflow.com/questions/2102912/cant-make-jdbc-connection-to-mysql-using-java-intellij-and-linux
- https://bbs.archlinux.org/viewtopic.php?pid=346836#p346836
- https://stackoverflow.com/questions/2118369/java-trouble-connecting-to-mysql
