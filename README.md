#### 一、[Centos Oracle-MySQL-8.0单节点搭建][1]（不推荐使用）
#### 二、[Centos Percona-Server-5.7.26单节点搭建][3]（推荐生产使用）
#### 三、[Centos Percona-Server-5.7.26镜像集群搭建][4]（推荐生产使用）
#### 四、[Explain执行计划说明][2]
#### 五、常用操作简单使用
```bash
truncate table 表名;                                                                    # 清空整张表数据

set global max_allowed_packet = 1024*1024;                                              # 加大mysq批量插入的数量
```

#### 六、批量修改
```bash
insert into table (aa,bb,cc) values(xx,xx,xx),(oo,oo,oo) on duplicate key update        # 遇见相同的key修改，没有插入

replace into table (aa,bb,cc) values(xxx,xxx,xxx),(ooo,ooo,ooo),(ccc,ccc,ccc)           # 遇见相同的key修改，没有不操作
```


#### 七、查询元数据信息
```bash
SELECT * FROM information_schema.columns WHERE column_name='job_name';                  # 查询所有表包含 job_name 列名


SELECT 
    CONCAT("ALTER TABLE ",TABLE_SCHEMA,".",TABLE_NAME," MODIFY COLUMN `job_name` VARCHAR(500);") 
FROM information_schema.columns 
WHERE column_name='job_name' 
AND TABLE_SCHEMA != 'zxyreportdb'
```

#### 八、数据查询导出
```bash
# mysqldump -u用户名 -p 库名 表名 --where="过滤条件"（不加 --where="过滤条件" 就是导出整张表） > 导出文件所在目录
$ mysqldump -uroot -p test  person --where="id=1" > /home/tools/4.txt

#mysql -h数据库所在ip -u用户名 -p -N -e"查询语句" 库名 > 导出文件所在目录
$ mysql -h127.0.0.1 -uroot -p -N -e"select * from person" test > /home/tools/1.txt
```

#### 九、找回root账号密码
##### 9.1 修改[vi /etc/my.cnf]添加如下配置
```bash
skip-grant-tables                                              # 跳过用户名密码验证
```
##### 9.2 重启mysql服务
```bash
$ service mysqld restart                                       # 重启服务
```
##### 3.3 修改root密码
```bash
$ mysql                                                        # 进入MySQL服务
$ use mysql;                                                   # 进入MySQL系统库
# 修改root账号密码
$ update user set password = password('Jiang@123') where user = 'root';
$ flush privileges;                                            # 刷新权限
```
##### 9.4 删除[vi /etc/my.cnf]配置文件里面的 skip-grant-tables（跳过用户名密码验证）
##### 9.5 重启mysql服务
```bash
$ service mysqld restart                                       # 重启服务
```
[1]: https://github.com/firechiang/mysql-test/blob/master/docs/setup-single-install.md
[2]: https://github.com/firechiang/mysql-test/blob/master/docs/explain-explain.md
[3]: https://github.com/firechiang/mysql-test/blob/master/docs/percona-server7-single-install.md
[4]: https://github.com/firechiang/mysql-test/blob/master/docs/percona-server7-cluster-install.md
