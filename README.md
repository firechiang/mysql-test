#### 一、[binlog 日志说明][10]
#### 二、[Centos Oracle-MySQL-8.0单节点搭建][1]（不推荐使用）
#### 三、[Centos Percona-Server-5.7.26单节点搭建][3]（推荐生产使用）
#### 四、[Centos Percona-Server-5.7.26镜像全量强一致性集群搭建][4]（推荐生产使用）
#### 五、[Centos Percona-Server-5.7.26集群强一致性数据同步流程以及普通MySQL集群弱一致性数据同步简要说明][11]
#### 六、[MyCat 主键取模分片搭建][5]
#### 七、[MyCat 根据某个字段的值分片搭建][6]（推荐生产使用）
#### 八、[MyCat 父子表（某一条数据在哪个分片，其关联数据就在哪个分片）简单使用][7]
#### 九、[MySQL 单表性能测试 Sysbench 简单使用][8]
#### 十、[MySQL 单表和多表关联压力测试 Tpcc-MySQL 简单使用][9]
#### 十一、[Explain执行计划说明][2]
#### 十二、常用操作简单使用
```bash
truncate table 表名;                                                                    # 清空整张表数据

set global max_allowed_packet = 1024*1024;                                              # 加大mysq批量插入的数量
```

#### 十三、批量修改
```bash
insert into table (aa,bb,cc) values(xx,xx,xx),(oo,oo,oo) on duplicate key update        # 遇见相同的key修改，没有插入

replace into table (aa,bb,cc) values(xxx,xxx,xxx),(ooo,ooo,ooo),(ccc,ccc,ccc)           # 遇见相同的key修改，没有不操作
```


#### 十四、查询元数据信息
```bash
SELECT * FROM information_schema.columns WHERE column_name='job_name';                  # 查询所有表包含 job_name 列名


SELECT 
    CONCAT("ALTER TABLE ",TABLE_SCHEMA,".",TABLE_NAME," MODIFY COLUMN `job_name` VARCHAR(500);") 
FROM information_schema.columns 
WHERE column_name='job_name' 
AND TABLE_SCHEMA != 'zxyreportdb'
```

#### 十五、数据查询导出
```bash
# mysqldump -u用户名 -p 库名 表名 --where="过滤条件"（不加 --where="过滤条件" 就是导出整张表） > 导出文件所在目录
$ mysqldump -uroot -p test  person --where="id=1" > /home/tools/4.txt

#mysql -h数据库所在ip -u用户名 -p -N -e"查询语句" 库名 > 导出文件所在目录
$ mysql -h127.0.0.1 -uroot -p -N -e"select * from person" test > /home/tools/1.txt
```

#### 十六、数据查询导入（注意：如果文件太大，在Linux系统下可以使用 split 命令切分文件）
```bash
$ mysql -h127.0.0.1 -P 3306 -uroot -p                          # 进入MySQL

# 导入 sql 文件数据（注意：这个方案比较慢，原因是sql是一条一条执行的）
$ source /home/tools/4.sql

# 导入数据文件（注意：这种方式导入速度快）
# fields terminated by（字段以什么分割）
# optionally enclosed by（以什么符号括住CHAR、VARCHAR和TEXT等字符型字段）
# lines terminated by（以什么换行）
# fields escaped by（设置转义字符，默认值为反斜线 \）
# ignore lines（可以忽略前n行）
$ load data infile "/data/mysql/user.sql" into table user fields terminated by ',' optionally enclosed by '"' lines terminated by '\n';
```

#### 十七、找回root账号密码
##### 17.1 修改[vi /etc/my.cnf]添加如下配置
```bash
skip-grant-tables                                              # 跳过用户名密码验证
```
##### 17.2 重启mysql服务
```bash
$ service mysqld restart                                       # 重启服务
```
##### 17.3 修改root密码
```bash
$ mysql                                                        # 进入MySQL服务
$ use mysql;                                                   # 进入MySQL系统库
# 修改root账号密码
$ update user set password = password('Jiang@123') where user = 'root';
$ flush privileges;                                            # 刷新权限
```
##### 17.4 删除[vi /etc/my.cnf]配置文件里面的 skip-grant-tables（跳过用户名密码验证）
##### 17.5 重启mysql服务
```bash
$ service mysqld restart                                       # 重启服务
```
#### 十八、SQL优化（注意：insert 语句后面加 IGNORE 关键字，如果插入数据违反了唯一约束（比如主键或唯一索引），不会报错，会返回受影响行数  0，建议生产使用，比如要查询用户手机是否存在，我直接插入数据就行了，返回0就说明用户手机已存在。插入语句示例：insert ignore into user(name)values(?)）
```bash
1，select * from user limit 1000000,10;可优化成：select a.* from user a join(select * from user limit 1000000,10) b on(a.id = b.id);
```

[1]: https://github.com/firechiang/mysql-test/blob/master/docs/setup-single-install.md
[2]: https://github.com/firechiang/mysql-test/blob/master/docs/explain-explain.md
[3]: https://github.com/firechiang/mysql-test/blob/master/docs/percona-server7-single-install.md
[4]: https://github.com/firechiang/mysql-test/blob/master/docs/percona-server7-cluster-install.md
[5]: https://github.com/firechiang/mysql-test/blob/master/docs/mycat-mod-use.md
[6]: https://github.com/firechiang/mysql-test/blob/master/docs/mycat-custom-use.md
[7]: https://github.com/firechiang/mysql-test/blob/master/docs/mycat-parent-use.md
[8]: https://github.com/firechiang/mysql-test/blob/master/docs/sysbench-use.md
[9]: https://github.com/firechiang/mysql-test/blob/master/docs/tpcc-mysql-use.md
[10]: https://github.com/firechiang/mysql-test/blob/master/docs/binlog-introduce.md
[11]: https://github.com/firechiang/mysql-test/blob/master/docs/pxc-sync.md
