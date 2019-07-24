#### 一、XtraBackup数据热备份还原的优势和原理说明
```bash
1，XtraBackup备份过程中加读锁，数据可读但不可写
2，XtraBackup备份过程中不会打断正在执行的事物
3，XtraBackup能够基于压缩等功能节约磁盘空间和流量
4，XtraBackup对InnoDB引擎可以做增量备份和全量备份
5，XtraBackup对MylSAM引擎只能做全量备份
6，XtraBackup实现原理是通过连接到MySQL服务端，然后读取并复制底层的文件以完成物理备份
7，XtraBackup增量备份原理，每一条数据都有一个全局递增的LSN号，每当数据被修改了以后就会加1。当我们执行增量备份时，XtraBackup会根据全量备份中最大的LSN号码去比较数据文件中的LSN号，大于全量备份中的LSN号就是新的数据（要做增量备份）
```

#### 二、XtraBackup下载安装包
```bash
# XtraBackup工具依赖包
$ wget -P /home/tools/xtrabackup https://www.percona.com/redir/downloads/percona-release/redhat/0.1-6/percona-release-0.1-6.noarch.rpm
# XtraBackup安装包（备用下载地址：https://github.com/firechiang/mysql-test/raw/master/data/percona-xtrabackup-24-2.4.15-1.el7.x86_64.rpm）
$ wget -P /home/tools/xtrabackup https://repo.percona.com/release/7/RPMS/x86_64/percona-xtrabackup-24-2.4.15-1.el7.x86_64.rpm
```

#### 三、安装XtraBackup（注意：要安装在带有MySQL的机器上）
```bash
$ cd /home/tools/xtrabackup
$ yum localinstall *.rpm                           # 安装所有安装包
$ xtrabackup --version                             # 查看XtraBackup版本号，以验证是否安装成功
```

#### 四、XtraBackup全量热备份简单使用
##### 4.1、XtraBackup全量热备份命令参数，简要说明
```bash
innobackupex               # 数据热备份
--xtrabackup               # 开启只备份InnoDB引擎数据表
--include                  # 指定要备份的数据表，多个用逗号隔开（注意：指定要备份的数据表可以写成正则表达式匹配）
--galera-info              # 开启备份Percona-XtraDB-Cluster节点的状态文件（注意：这个参数仅限于备份Percona-XtraDB-Cluster集群节点）
--defaults-file            # MySQL配置文件地址
--encrypt                  # 加密算法（AES128，AES192，AES256（建议生产使用））  
--encrypt-therads          # 执行加密的线程数
--encrypt-chunk-size       # 加密线程的缓存大小（默认64K，最大不能超过1M）
--encrypt-key              # 秘钥字符（一定要是24个字符）
--encryption-key-file      # 秘钥字符所在的文件地址（注意：和--encrypt-key配置二选一）
--no-timestamp             # 不创建时间戳目录
--stream=xbstream          # 使用使用流式压缩 （流式压缩，直接生成压缩文件，只有一次IO（传统是先备份，再压缩，有两次IO），所以效率特别高）
--compress                 # 开启压缩InnoDB数据文件（注意：这个参数不建议使用，可能会报错）
--compress-threads         # 执行压缩InnoDB数据文件执行的线程数（注意：这个参数不建议使用，可能会报错）
--compress-chunk-size      # 执行压缩InnoDB数据文件线程的缓存大小（默认64K，最大不能超过1M）（注意：这个参数不建议使用，可能会报错）
--decompress               # 开启解压缩InnoDB数据文件（注意：这个参数是在解压缩才使用，而且还要是压缩过的InnoDB数据文件，才能使用（注意：这个参数不建议使用，可能会报错））
```
##### 4.2、XtraBackup全量热备份命令innobackupex简单使用
```bash
# 全量热备份简单使用
# --xtrabackup      备份InnoDB引擎数据表
# --defaults-file   MySQL配置文件地址
# /home/backup      备份数据的输出目录
$ innobackupex --defaults-file=/etc/my.cnf \
               --host=localhost            \
               --port=3306                 \
               --user=root                 \
               --password=Jiang@123        \
               /home/backup
               
               
# 全量热备份并压缩备份文件简单使用（注意：文件输出格式一定要是.xbstream格式文件）           
# --no-timestamp    不创建时间戳目录
# --stream=xbstream 使用使用流式压缩 （流式压缩，直接生成压缩文件，只有一次IO（传统是先备份，再压缩，有两次IO），所以效率特别高）              
$ innobackupex --defaults-file=/etc/my.cnf \
               --host=localhost            \
               --port=3306                 \
               --user=root                 \
               --password=Jiang@123        \
               --no-timestamp              \
               --stream=xbstream           \
               -> /home/backup.xbstream
               
               
               
# 全量热备份并加密以及压缩备份文件简单使用（注意：文件输出格式一定要是.xbstream格式文件）
# --encrypt             加密算法（AES128，AES192，AES256（建议生产使用））  
# --encrypt-therads     执行加密的线程数
# --encrypt-chunk-size  加密线程的缓存大小，默认64K，最大不能超过1M
# --encrypt-key         秘钥字符（一定要是24个字符）
# --no-timestamp        不创建时间戳目录
# --stream=xbstream     使用使用流式压缩 （流式压缩，直接生成压缩文件，只有一次IO（传统是先备份，再压缩，有两次IO），所以效率特别高）              
$ innobackupex --defaults-file=/etc/my.cnf            \
               --host=localhost                       \
               --port=3306                            \
               --user=root                            \
               --password=Jiang@123                   \
               --encrypt=AES256                       \
               --encrypt-therads=10                   \
               --encrypt-chunk-size=512               \
               --encrypt-key=1124hdnvh746r8ushdfjnsdh \
               --no-timestamp                         \
               --stream=xbstream                      \
               -> /home/backup-encrypt.xbstream
               
               
# 全量热备份指定数据表并加密以及压缩备份文件简单使用（注意：文件输出格式一定要是.xbstream格式文件）
# --encrypt             加密算法（AES128，AES192，AES256（建议生产使用））  
# --encrypt-therads     执行加密的线程数
# --encrypt-chunk-size  加密线程的缓存大小，默认64K，最大不能超过1M
# --encrypt-key         秘钥字符（一定要是24个字符）
# --include             只备份mysql库的proc和user表
# --no-timestamp        不创建时间戳目录
# --stream=xbstream     使用使用流式压缩 （流式压缩，直接生成压缩文件，只有一次IO（传统是先备份，再压缩，有两次IO），所以效率特别高）              
$ innobackupex --defaults-file=/etc/my.cnf            \
               --host=localhost                       \
               --port=3306                            \
               --user=root                            \
               --password=Jiang@123                   \
               --encrypt=AES256                       \
               --encrypt-therads=10                   \
               --encrypt-chunk-size=512               \
               --encrypt-key=1124hdnvh746r8ushdfjnsdh \
               --include=mysql.proc,mysql.user        \
               --no-timestamp                         \
               --stream=xbstream                      \
               -> /home/backup-encrypt-table.xbstream
```

#### 五、创建[vi /home/xtrabackup-all.sh]一个全量热备份的脚本，[使用Linux Crontab定时任务去执行脚本][1]（注意：脚本要赋予权限，否则脚本将无法执行，赋权命令：chmod -R 777 /home/xtrabackup-all.sh）
```bash
#! /bin/bash
time=$(date "+%Y-%m-%d %H:%M:%S")
echo "执行全量备份时间  ${time}"
innobackupex --defaults-file=/etc/my.cnf              \
               --host=localhost                       \
               --port=3306                            \
               --user=root                            \
               --password=Jiang@123                   \
               --encrypt=AES256                       \
               --encrypt-therads=10                   \
               --encrypt-chunk-size=512               \
               --encrypt-key=1124hdnvh746r8ushdfjnsdh \
               --no-timestamp                         \
               --stream=xbstream                      \
               -> /home/xtrabackup-all-res.xbstream
```

#### 六、解压缩和解密XtraBackup备份的数据文件（注意：解压缩和解密只是针对压缩和加密过的备份数据文件）
```bash
# 解压缩 xtrabackup-all-res.xbstream 文件，到/home/xtrabackup-all-res目录（注意：解压目录需要手动创建）
$ xbstream -x < /home/xtrabackup-all-res.xbstream -C /home/xtrabackup-all-res

# 解密/home/xtrabackup-all-res目录里面的文件数据（--decrypt=加密时的算法，--encrypt-key=加密时的key）
$ innobackupex --decrypt=AES256 --encrypt-key=1124hdnvh746r8ushdfjnsdh /home/xtrabackup-all-res
```

#### 七、XtraBackup全量数据还原（注意：还原数据之前要关闭MySQL，清空数据目录包括表分区的目录）
```bash
# 清理备份数据（回滚没有提交的事物，同步已经提交的事物到数据文件）
# 说明：因为XtraBackup备份数据时不会锁表，所以在备份时可能会有数据正在写入，但还没有提交事物，就被备份出来了。像这样的数据就是要做清理的
# 清理 /home/backup/2019-07-19_07-45-35 目录里面的备份数据（注意：这个数据是没有经过压缩和加密的且目录一定要是实际备份数据的目录）
$ innobackupex --apply-log /home/backup/2019-07-19_07-45-35  

# 还原数据（说明：将 /home/backup/2019-07-19_07-45-35 目录里面的备份数据还原到MySQL（--defaults-file=MySQL配置文件））
$ innobackupex --defaults-file=/etc/my.cnf --copy-back /home/backup/2019-07-19_07-45-35

# 为还原过来的数据分配用户和用户组，如果有表分区目录也要分配用户和用户组（注意：/var/lib/mysql/* 是数据MysQL的数据目录，如果不分配用户和用户组MySQL将无法启动）
$ chown -R mysql:mysql /var/lib/mysql/*
```

[1]: https://github.com/firechiang/linux-test/blob/master/docs/linux-crontable-use.md