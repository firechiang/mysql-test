#### 一、[CentOS安装][1]
#### 二、[Explain执行计划][2]
#### 三、简单使用
```bash
truncate table 表名;                                                                    # 清空整张表数据

set global max_allowed_packet = 1024*1024;                                              # 加大mysq批量插入的数量
```

#### 四、批量修改
```bash
insert into table (aa,bb,cc) values(xx,xx,xx),(oo,oo,oo) on duplicate key update        # 遇见相同的key修改，没有插入

replace into table (aa,bb,cc) values(xxx,xxx,xxx),(ooo,ooo,ooo),(ccc,ccc,ccc)           # 遇见相同的key修改，没有不操作
```


#### 五、查询元数据信息
```bash
SELECT * FROM information_schema.columns WHERE column_name='job_name';                  # 查询所有表包含 job_name 列名


SELECT 
    CONCAT("ALTER TABLE ",TABLE_SCHEMA,".",TABLE_NAME," MODIFY COLUMN `job_name` VARCHAR(500);") 
FROM information_schema.columns 
WHERE column_name='job_name' 
AND TABLE_SCHEMA != 'zxyreportdb'
```

#### 六、数据查询导出
```bash
# mysqldump -u用户名 -p 库名 表名 --where="过滤条件"（不加 --where="过滤条件" 就是导出整张表） > 导出文件所在目录
$ mysqldump -uroot -p test  person --where="id=1" > /home/tools/4.txt

#mysql -h数据库所在ip -u用户名 -p -N -e"查询语句" 库名 > 导出文件所在目录
$ mysql -h127.0.0.1 -uroot -p -N -e"select * from person" test > /home/tools/1.txt
```
[1]: https://github.com/firechiang/mysql-test/blob/master/docs/1-setup-single.md
[2]: https://github.com/firechiang/mysql-test/blob/master/docs/explain-explain.md
