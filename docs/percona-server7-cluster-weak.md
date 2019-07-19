#### 一、环境准备（注意：集群每个节点都要修改，如果不使用TokuDB引擎，可以不开启如下操作）
```bash
$ echo never > /sys/kernel/mm/transparent_hugepage/enabled  # 开启 Linux 大热内存管理，动态分配内存
$ echo never > /sys/kernel/mm/transparent_hugepage/defrag   # 开启 Linux 内存碎片整理
```

#### 二、修改[vi /etc/selinux/config]关闭SELinux安全验证（注意：集群每个节点都要修改，需要重启机器才能生效）
```bash
#SELINUX=enforcing
SELINUX=disabled
```

#### 三、下载依赖安装包
```bash
$ cd /home/tools
# 下载 Percona-Server 5.7.26 安装包（注意：如果不使用TokuDB引擎，可以不下载 jemalloc 内存碎片整理依赖）
$ wget -P /home/tools/percona-server https://www.percona.com/downloads/Percona-Server-5.7/Percona-Server-5.7.26-29/binary/redhat/7/x86_64/Percona-Server-5.7.26-29-r11ad961-el7-x86_64-bundle.tar
# 内存分配器依赖用于解决内存碎片问题
$ wget -P /home/tools/percona-server https://repo.percona.com/release/7/RPMS/x86_64/jemalloc-3.6.0-1.el7.x86_64.rpm
```

#### 四、分发安装包到其它节点
```bash
$ scp -r /home/tools/percona-server root@server002:/home/tools
$ scp -r /home/tools/percona-server root@server003:/home/tools
```

#### 五、安装 Percona-Server 节点（注意：集群每个节点都要安装，如果不使用TokuDB引擎，可以不安装jemalloc内存碎片整理）
```bash
$ cd /home/tools/percona-server
$ yum -y remove mari*                                               # 卸载 MariaDB 所有依赖包
$ tar -xvf Percona-Server-5.7.26-29-r11ad961-el7-x86_64-bundle.tar  # 解压 Percona-Server 安装包
$ yum localinstall *.rpm                                            # 安装所有安装包
```

#### 六、修改[vi /etc/my.cnf]配置（注意：先将原有的配置都删除掉，集群每个节点都要修改。还有server-id和wsrep_node_name以及wsrep_node_address每个节点需不一样）。以下配置信息其实是从 /etc/percona-server.conf.d/ 目录下的 mysqld.cnf（基础配置） 和 mysqld_safe.cnf（安全配置） 文件里面复制过来的
```bash
[mysqld]
# 集群节点唯一标识（注意：集群中不能重复，必须是数字）
server-id=1

# 数据目录
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock

# 字符集
character_set_server=utf8mb4

# 绑定主机
bind-address=0.0.0.0

# 跳过DNS解析
skip-name-resolve

# # 建议禁用符号链接以防止各种安全风险
symbolic-links=0

# 日志目录
log-error=/var/log/mysqld.log

# 进程ID所在目录
pid-file=/var/run/mysqld/mysqld.pid

# 开启bin_log以及bin_log日志名称是mysql_bin_log（名称可以随便写，也可以加目录）
log_bin=mysql_bin_log

# 开启接收bin_log，因为我们是双向同步（就是当前节点既是主节点又是从节点）
relay_log=mysql_relay_log

# binlog日志格式，基于mixed复制，安全可靠效率高
binlog_format=mixed

# 开启同步数据的更新记录到bin-log（开启同步更新记录）
log_slave_updates

# bin-log日志n天后自动删除，0表示不删除（生产建议配置成0）
expire_logs_days=0

# 不允许从节点同步哪些数据库，生产建议不要同步MySQL自带的库，可能会出现错误（注意：这个其实就是主节点不记录哪些数据库的bin-log日志，如果要配置多个，配置多个配置项即可，如下所示）
binlog_ignore_db=mysql
binlog_ignore_db=sys
binlog_ignore_db=performance_schema
binlog_ignore_db=information_schema

# 只允许从节点同步哪些数据库（注意：这个其实就是主节点只记录哪些数据库的bin-log日志，如果要配置多个，配置多个配置项即可，如下所示）
#binlog_do_db=test_user1
#binlog_do_db=test_user2

# 从节点只同步主节点哪些数据库（注意：如果要配置多个，配置多个配置项即可，如下所示）
#replicate-do-db=test_user1
#replicate-do-db=test_user2

# 从节点不同步主节点哪些数据库（注意：如果要配置多个，配置多个配置项即可，如下所示）
#replicate-ignore-db=test_user3
#replicate-ignore-db=test_user4

# 默认引擎
default_storage_engine=InnoDB

# 主键自增不锁表
innodb_autoinc_lock_mode=2

# MySQL缓存大小（专用MySQL服务器建议配置为机器总内存的70%，如果不是专用的建议配置为机器总内存的10%）
#innodb_buffer_pool_size = 128M

# Remove leading # to set options mainly useful for reporting servers.
# The server defaults are faster for transactions and fast SELECTs.
# Adjust sizes as needed, experiment to find the optimal values.
# join_buffer_size = 128M
# sort_buffer_size = 2M
# read_rnd_buffer_size = 2M

[mysqld_safe]
pid-file=/var/run/mysqld/mysqld.pid
socket=/var/run/mysqld/mysqld.sock
# 指定服务的运行级别
nice=0
# 指定内存锁片整理所使用的依赖库(注意：这个库是我们在上面已经安装的jemalloc内存碎片整理库)
malloc-lib=/usr/lib64/libjemalloc.so.1
```

#### 七、开放 Percona-Server 所使用的端口（注意：集群每个节点都要配置）
```bash
$ firewall-cmd --zone=public --add-port=3306/tcp --permanent   # 开放3306（MySQL 服务端口）
$ firewall-cmd --reload                                        # 刷新配置
```

#### 八、启动服务
```bash
$ service mysql start                                          # 启动服务
$ service mysql restart                                        # 重启服务
$ service mysql stop                                           # 停止服务
$ chkconfig mysqld on                                          # 开启开机启动
$ chkconfig mysqld off                                         # 禁止开机启动
```

#### 九、修改root账号密码和创建数据同步账号backup（注意：集群每个节点都要修改，因为我们是双向同步（既是主也是从））
```bash
$ grep 'temporary password' /var/log/mysqld.log                # 查看mysql默认root账号密码
$ mysql -uroot -p                                              # 进入MySQL服务（远程连接：mysql -h127.0.0.1 -P 3306 -uroot -p）
$ ALTER USER 'root'@'localhost' IDENTIFIED BY 'Jiang@123';     # 设置root用户密码为 Jiang@123，且只有本地能登录                 
$ use mysql;                                                   # 进入MySQL系统库
$ update user set host = '%' where user = 'root';              # 修改root用户允许所有IP访问（注意：修改看实际情况而定）

# 创建数据同步账号backup（注意：我们在配置文件里面配的就是这个账号）
$ CREATE USER 'backup'@'%' IDENTIFIED BY 'Jiang@123';          # 创建用户backup密码Jiang@123，%是指所有IP都可以连接
$ GRANT super,reload,replication slave ON *.* TO 'backup'@'%'; # 将数据读取权限都赋给backup账号
$ flush privileges;                                            # 刷新权限
```

#### 十、配置开启TokuDB引擎（注意：以下命令要退出MySQL客户端执行，而且集群每个节点都要配置）
```bash
$ ps-admin --enable -uroot -p                                  # 安装TokuDB引擎（注意：它会提示你输入密码）
$ service mysql restart                                        # 重启MySQL服务
$ ps-admin --enable -uroot -p                                  # 激活TokuDB引擎（注意：它会提示你输入密码）
$ mysql -uroot -p                                              # 进入MySQL服务（远程连接：mysql -h127.0.0.1 -P 3306 -uroot -p）
$ show engines;                                                # 查看数据所有引擎（注意：看看有没有TokuDB引擎）
```

#### 十一、配置主从同步（注意：每个节点都要配置，因为我们是双向同步（当前节点既是主节点也是从节点））
```bash
# 进入MySQL服务（远程连接：mysql -h127.0.0.1 -P 3306 -uroot -p）
$ mysql -uroot -p                                              

# 停止主从同步服务
$ stop slave;                                                  

# 配置要同步哪个节点的数据（注意：查看主节点的bin-log信息可使用命令：show master status）
# master_host     主节点的主机或IP
# master_port     主节点的端口
# master_user     主节点的用户
# master_password 主节点的用户密码
# master_log_file 从主节点的哪个bin-log文件开始同步（注意：这个参数可以不写默认同步全部）
# master_log_pos  从主节点的那个bin-log文件的哪个位置开始同步（注意：这个参数可以不写默认同步全部）
# for channel     指定同步通道，名字可以顺便起，建议使用主节点的 hostname命名（注意：如果要同步多个节点的数据，这个名字要唯一）
#$ change master to master_host='server007',master_port=3306,master_user='backup',master_password='Jiang@123',master_log_file='mysql-bin.000003',master_log_pos=123 for channel 'server007';
$ change master to master_host='server007',master_port=3306,master_user='backup',master_password='Jiang@123' for channel 'server007';

# 开启主从同步服务
$ start slave;

# 查看主从同步状态信息（看看我们上面配置的主从同步是否成功）
$ show slave status;
```