#### 一、binlog日志，分为两种文件（日志文件和索引文件），日志文件格式分为3种（row，statement，mixed），具体如下
##### 1.1 row格式，优点：每条记录的变化都会写到日志当中，数据同步安全可靠（不会错误删除和修改），同步写入时出现行锁机率更低；缺点：日志体积大（一条删除10条数据的delete语句，会保存成10条删除日志），浪费存储空间，同步数据需要更大的网络带宽，频繁同步传输速度较慢），日志数据格式如下
```bash
# 字段说明：Log_name（文件名），Pos（日志数据开始位置），Event_type（时间类型），Server_id（服务器ID），End_log_pos（日志数据结束位置，Info（简要信息）
Log_name	        Pos	    Event_type	   Server_id	  End_log_pos	     Info
mysql_bin_log.000001	4	    Format_desc	      1	            107	             Server ver: 5.5.49-log, Binlog ver: 4
mysql_bin_log.000001	947	    Query	      1	            1015	     BEGIN
mysql_bin_log.000001	1015	    Table_map	      1	            1061	     table_id: 33 (test.dept)
mysql_bin_log.000001	1061	    Write_rows	      1	            1452	     table_id: 33 flags: STMT_END_F
mysql_bin_log.000001	1452	    Xid	              1	            1479	     COMMIT /* xid=35 */
mysql_bin_log.000001	1479	    Stop	      1	            1498	
```
##### 1.2 statement格式，优点：日志体积小（因为日志记录的是操作的SQL语句），频繁同步传输速度较快；缺点：不能保证数据同步的安全可靠性（自增主键可能出现不一致，还可能出现错误删除或修改），同步写入时出现行锁概率更高，数据格式如下
```bash
# 字段说明：Log_name（文件名），Pos（日志数据开始位置），Event_type（时间类型），Server_id（服务器ID），End_log_pos（日志数据结束位置，Info（实际操作的SQL语句）
Log_name	             Pos	         Event_type	         Server_id	      End_log_pos	       Info
mysql_bin_log.000005	     4	                 Format_desc	           1	              107	           Server ver: 5.5.49-log, Binlog ver: 4
mysql_bin_log.000005	     107	         Query	                   1	              282	           use `test`; CREATE TABLE `test`.`role`  (`id` int(0) NOT NULL,`name` varchar(255) NOT NULL, PRIMARY KEY (`id`))
mysql_bin_log.000005	     282	         Query	                   1	              429	           use `test`; ALTER TABLE `test`.`role` MODIFY COLUMN `id` int(11) NOT NULL AUTO_INCREMENT FIRST
mysql_bin_log.000005	     429	         Query	                   1	              497	           BEGIN
mysql_bin_log.000005	     497	         Intvar	                   1	              525	           INSERT_ID=1
mysql_bin_log.000005	     525	         Query	                   1	              674	           use `test`; insert into role(name) values('dasda'),('dadaasd'),('wdefrf'),('oloiojfs'),('dadaasd')
mysql_bin_log.000005	     674	         Xid	                   1	              701	           COMMIT /* xid=82 */
```
##### 1.3 mixed格式，它是row格式和statement格式的结合体，以达到性能最大化，推荐生产使用（注意：Percona Server不支持），mixed格式储存规则：如果是会出现数据不一致情况的操作（比如使用UUID的函数插入数据，因为UUID函数在每台机器上执行的结果是不一致的，就会采用row格式存储日志，普通操作采用statement格式存储日志（就是直接存储SQl语句））
```bash
# 字段说明：Log_name（文件名），Pos（日志数据开始位置），Event_type（时间类型），Server_id（服务器ID），End_log_pos（日志数据结束位置，Info（实际操作的SQL语句）
Log_name	             Pos	        Event_type	        Server_id	       End_log_pos	       Info
mysql_bin_log.000006	     4	                Format_desc	            1	              107	             Server ver: 5.5.49-log, Binlog ver: 4
mysql_bin_log.000006	     107	        Query	                    1	              297	             use `test`; CREATE TABLE `test`.`user`  (`id` int(0) NOT NULL AUTO_INCREMENT,`name` varchar(255) NOT NULL,PRIMARY KEY (`id`))
mysql_bin_log.000006	     297	        Query	                    1	              365	             BEGIN
mysql_bin_log.000006	     365	        Intvar	                    1	              393	             INSERT_ID=1
mysql_bin_log.000006	     393	        Query	                    1	              542	             use `test`; insert into user(name) values('dasda'),('dadaasd'),('wdefrf'),('oloiojfs'),('dadaasd')
mysql_bin_log.000006	     542	        Xid	                    1	              569	             COMMIT /* xid=27 */
```
#### 二、开启 binlog 日志（注意：Windows系统需写在 [mysqld] 配置项下面，Linux系统好像无所谓）
```bash
# 开启bin-log以及bin-log日志名称是mysql_bin_log（名称可以随便写，也可以加目录）
log-bin=mysql_bin_log
# binlog日志格式，基于ROW复制，安全可靠
binlog_format=ROW
```

#### 三、binlog 日志相关操作
```bash
$ mysql -h127.0.0.1 -P 3306 -uroot -p           # 进入MySQL服务
$ show global variables like '%log_bin%';       # 查看是否开启 log_bin
$ show global variables like '%binlog_format%'; # 查看 log_bin 日志格式
$ show master logs;                             # 查看 log_bin 日志列表（所有log_bin日志文件）
$ show binlog events in 'mysql_bin_log.000001'; # 查看log_bin文件内容，mysql_bin_log.000001是log_bin日志文件的名称（文件名称可以通过上一条命令得到）
```


