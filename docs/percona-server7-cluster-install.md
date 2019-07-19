#### 一、Percona-XtraDB-Cluster 注意事项
```bash
1，集群建议 2-3 个节点，过多影响性能，原因是数据强一直性的同步。建议每个节点硬件配置相同，因为是并行同步，配置低的哪个会拖慢同步速度。
2，数据同步强一致性（镜像全量），任意节点都可写入和读取，事务在集群中要么同时提交，要么不提交。
3，只有 InnoDB 引擎的数据才会被同步（注意：用户信息也会自动同步）
4，如果集群出项脑裂，超过半数的那一边对外提供服务，没有超过半数的默认暂态对外提供服务
```
#### 二、下载依赖安装包
```bash
# 下载 Percona-XtraDB-Cluster 5.7.26 安装包
$ wget -P /home/tools/Percona-XtraDB-Cluster https://www.percona.com/downloads/Percona-XtraDB-Cluster-LATEST/Percona-XtraDB-Cluster-5.7.26-31.37/binary/redhat/7/x86_64/Percona-XtraDB-Cluster-5.7.26-31.37-r505-el7-x86_64-bundle.tar
# 下载 qpree解压缩工具
$ wget -P /home/tools/Percona-XtraDB-Cluster https://repo.percona.com/release/7/RPMS/x86_64/qpress-11-1.el7.x86_64.rpm
# 下载数据同步插件
$ wget -P /home/tools/Percona-XtraDB-Cluster https://repo.percona.com/release/7/RPMS/x86_64/percona-xtrabackup-24-2.4.15-1.el7.x86_64.rpm
$ wget -P /home/tools/Percona-XtraDB-Cluster https://repo.percona.com/release/7/RPMS/x86_64/percona-xtrabackup-24-debuginfo-2.4.15-1.el7.x86_64.rpm
$ wget -P /home/tools/Percona-XtraDB-Cluster https://repo.percona.com/release/7/RPMS/x86_64/percona-xtrabackup-test-24-2.4.15-1.el7.x86_64.rpm
```

#### 三、分发安装包到其它节点
```bash
$ scp -r /home/tools/Percona-XtraDB-Cluster root@server002:/home/tools
$ scp -r /home/tools/Percona-XtraDB-Cluster root@server003:/home/tools
```

#### 四、安装 Percona-XtraDB-Cluster 节点（注意：集群每个节点都要安装）
```bash
$ cd /home/tools/Percona-XtraDB-Cluster
$ yum -y remove mari*                                                     # 卸载 MariaDB 所有依赖包
$ tar -xvf Percona-XtraDB-Cluster-5.7.26-31.37-r505-el7-x86_64-bundle.tar # 解压 Percona-Server 安装包
$ yum localinstall *.rpm                                                  # 安装所有安装包
```

#### 五、修改[vi /etc/my.cnf]配置（注意：先将原有的配置都删除掉，集群每个节点都要修改。还有server-id和wsrep_node_name以及wsrep_node_address每个节点需不一样）。以下配置信息其实是从 /etc/percona-xtradb-cluster.conf.d 目录下的 mysqld.cnf（基础配置） 和 wsrep.cnf（集群配置） 文件里面复制过来的
```bash
[client]
socket=/var/lib/mysql/mysql.sock

[mysqld]
# 数据目录（注意：InnoDB的日志就是该目录下的 innobackup.backup.log 文件，如果想看InnoDB的错误信息，查看该文件即可）
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
# 日志目录
log-error=/var/log/mysqld.log
# 进程ID所在目录
pid-file=/var/run/mysqld/mysqld.pid
# 开启同步数据的更新记录到bin-log（开启同步更新记录）
log_slave_updates
# # bin-log日志n天后自动删除，0表示不删除（生产建议配置成0）
expire_logs_days=0
# 配置字符集
character_set_server=utf8mb4
# 绑定
bind-address=0.0.0.0
# 跳过DNS解析
skip-name-resolve
# 0（log buffer将每秒一次地写入log file中，并且log file的flush(刷到磁盘)操作同时进行.该模式下，在事务提交的时候，不会主动触发写入磁盘的操作）
# 1（每次事务提交时MySQL都会把log buffer的数据写入log file，并且flush(刷到磁盘)中去）
# 2（每次事务提交时MySQL都会把log buffer的数据写入log file.但是flush(刷到磁盘)操作并不会同时进行。该模式下,MySQL会每秒执行一次 flush(刷到磁盘)操作）
# 建议设置成0或2
#innodb_flush_log_at_trx_commit=2
# MySQL缓存大小（专用MySQL服务器建议配置为机器总内存的70%，如果不是专用的建议配置为机器总内存的10%）
#innodb_buffer_pool_size=200M

# 建议禁用符号链接以防止各种安全风险
symbolic-links=0

# 集权相关配置
# 集群节点唯一标识（注意：集群中不能重复，必须是数字）
server-id=1
# 当前节点的名称（注意：集群中不能重复）
wsrep_node_name=node001
# 当前节点的IP或主机（注意：集群中不能重复）
wsrep_node_address=server001
# 集群名称
wsrep_cluster_name=myCluster
# 集群所有的节点
wsrep_cluster_address=gcomm://server001,server002,server003
wsrep_provider=/usr/lib64/galera3/libgalera_smm.so
# 数据同步方法（mysqldump，rsync，xtrabackup-v2）
wsrep_sst_method=xtrabackup-v2
# 数据同步最大线程数（可加快数据同步速度）
#wsrep_slave_threads=8
# 同步数据时所使用的账号
wsrep_sst_auth=admin:Jiang@123
# 同步数据使用严格模式，不允许不一致问题
pxc_strict_mode=ENFORCING
# 开启bin-log以及bin-log日志名称是mysql_bin_log（名称可以随便写，也可以加目录）
log-bin=mysql_bin_log
# binlog日志格式，基于ROW复制，安全可靠
binlog_format=ROW
# 默认引擎，其它引擎无效
default_storage_engine=InnoDB
# 主键自增不锁表
innodb_autoinc_lock_mode=2

# 集群同步超时相关配置，说明如下
# evs.keepalive_period      控制多久发送一次keepalive请求信号
# evs.inactive_check_period 控制多久检测一次节点活动/静止状态
# evs.suspect_timeout       控制某个节点是否被标识为suspected状态的时间间隔
# evs.inactive_timeout      控制节点不活动时检测周期
# evs.consensus_timeout     控制多久检测一次节点一致性 通过上面的设置，可以使节点超时时间为30秒
# evs.inactive_timeout参数必须不小于evs.suspect_timeout， evs.consensus_timeout必须不小于evs.inactive_timeout
# 示例配置如下
#wsrep_provider_options = "evs.keepalive_period = PT3S; evs.inactive_check_period = PT10S; evs.suspect_timeout = PT30S; evs.inactive_timeout = PT1M; evs.consensus_timeout = PT1M;gmcast.peer_timeout=PT30S"
```
#### 六、开放 Percona XtraDB Cluster 所使用的端口（注意：集群每个节点都要配置）
```bash
$ firewall-cmd --zone=public --add-port=3306/tcp --permanent   # 开放3306（MySQL 服务端口）
$ firewall-cmd --zone=public --add-port=4444/tcp --permanent   # 开放4444（请求全量同步数据（SST）端口（注意：全量同步会限制其它节点的写入操作，一般在数据库启动时会执行全量同步））
$ firewall-cmd --zone=public --add-port=4567/tcp --permanent   # 开放4567（数据库节点之间的通信端口）
$ firewall-cmd --zone=public --add-port=4568/tcp --permanent   # 开放4568（请求增量同步（IST）端口，正常同步数据使用）
$ firewall-cmd --reload                                        # 刷新配置
```

#### 七、修改[vi /etc/selinux/config]关闭SELinux安全验证（注意：集群每个节点都要修改，需要重启机器才能生效）
```bash
#SELINUX=enforcing
SELINUX=disabled
```

#### 八、启动集群当中引导节点（注意：如果是旧集群，一定要选择最后停止的那机器执行（可通过查看mysql数据目录下grastate.dat文件里面safe_to_bootstrap属性的值（1是，0否），来判定节点是否最后停止，如果集群中没有是1的，找一台修改成1即可）。如果是新建集群，任选一台机器执行）
```bash
$ systemctl start mysql@bootstrap.service                      # 启动集群引导节点
$ systemctl restart mysql@bootstrap.service                    # 重启集群引导节点
$ systemctl stop mysql@bootstrap.service                       # 停止集群引导节点
$ chkconfig mysqld on                                          # 开启开机启动
$ chkconfig mysqld off                                         # 禁止开机启动（集群模式，建议禁止开机启动）
```

#### 九、修改root账号密码和创建数据同步账号admin（注意：在集群引导节点上执行，因为如果集群引导节点上没有admin账号，其它节点将无法加入集群）
```bash
$ grep 'temporary password' /var/log/mysqld.log                # 查看mysql默认root账号密码
$ mysql -uroot -p                                              # 进入MySQL服务（远程连接：mysql -h127.0.0.1 -P 3306 -uroot -p）
$ ALTER USER 'root'@'localhost' IDENTIFIED BY 'Jiang@123';     # 设置root用户密码为 Jiang@123，且只有本地能登录                 
$ use mysql;                                                   # 进入MySQL系统库
$ update user set host = '%' where user = 'root';              # 修改root用户允许所有IP访问（注意：修改看实际情况而定）

# 创建数据同步账号admin（注意：我们在配置文件里面配的就是这个账号）
$ CREATE USER 'admin'@'%' IDENTIFIED BY 'Jiang@123';           # 创建用户admin密码Jiang@123，%是指所有IP都可以连接
$ GRANT all privileges ON *.* TO 'admin'@'%';                  # 将所有权限都赋给admin账号
$ flush privileges;                                            # 刷新权限
```

#### 十、启动集群当中其它节点（注意：如果集群当中没有正在运行的节点，必须先启动引导节点，再启动其它节点。（引导节点启动方法，请看第八项））
```bash
$ service mysql start                                          # 启动服务
$ service mysql restart                                        # 重启服务
$ service mysql stop                                           # 停止服务
$ chkconfig mysqld on                                          # 开启开机启动
$ chkconfig mysqld off                                         # 禁止开机启动（集群模式，建议禁止开机启动）
```

#### 十一、集群相关操作和说明
```bash
$ mysql -uroot -p                                              # 进入MySQL服务（远程连接：mysql -h127.0.0.1 -P 3306 -uroot -p）
$ show status like 'wsrep_cluster%';                           # 查看集群和当前节点的状态信息
+--------------------------+---------------------------------+
| Variable_name            | Value                           |
+--------------------------+---------------------------------+
| wsrep_cluster_weight     | 3                               |
| wsrep_cluster_conf_id    | 3                               |
| wsrep_cluster_size       | 3                               | # 集群节点总数量（注意：如果右节点宕机，是不会减少这个数量的）
| wsrep_cluster_state_uuid | 0f6b0-a5d-11e9-a12c-9ffe44269d9 |
| wsrep_cluster_status     | Primary                         | # 集群状态（Primary（正常），Non_Primary（出现裂脑），Disconnected（集群不可用（原因可能是脑裂所引起）））
+--------------------------+---------------------------------+

$ show status like '%queue%';                                  # 查看集群同步数据的队列相关信息
$ show status like 'wsrep_flow%';                              # 查看集群数据同步是否限速以及相关信息
$ show status like 'wsrep_commit%';                            # 查看发送数据的队列当中，含有事务的数据，相关统计信息
$ show status like '%wsrep%';                                  # 查看集群相关所有信息
+----------------------------------+-------------------------+
| Variable_name                    | Value                   |
+----------------------------------+-------------------------+
| wsrep_local_state_uuid           | 0fb0-a5d-11e9-a12c-949  |
| wsrep_protocol_version           | 9                       |
| wsrep_last_applied               | 6                       | # 同步应用次数（创建表，库，视图等等）
| wsrep_last_committed             | 6                       | # 最后提交的事物ID（注意：这个ID是自增的，所以也可以看作是事务的提交次数）
| wsrep_replicated                 | 5                       | # 向其它节点发送同步数据总次数
| wsrep_replicated_bytes           | 1176                    | # 向其它节点发送同步数据总大小
| wsrep_repl_keys                  | 6                       |
| wsrep_repl_keys_bytes            | 168                     |
| wsrep_repl_data_bytes            | 663                     |
| wsrep_repl_other_bytes           | 0                       |
| wsrep_received                   | 11                      | # 接收同步数据总次数
| wsrep_received_bytes             | 970                     | # 接收同步数据总大小
| wsrep_local_commits              | 0                       | 
| wsrep_local_cert_failures        | 0                       |
| wsrep_local_replays              | 0                       |
| wsrep_local_send_queue           | 0                       | # 当前发送数据的队列长度（重要，如果队列过长说明发送数据较慢）
| wsrep_local_send_queue_max       | 1                       | # 发送数据的队列最大长度
| wsrep_local_send_queue_min       | 0                       | # 发送数据的队列最小长度
| wsrep_local_send_queue_avg       | 0.000000                | # 发送数据的队列平均长度（重要，如果队列过长说明发送数据较慢）
| wsrep_local_recv_queue           | 0                       | # 当前接收数据的队列长度（重要，如果队列过长说明接收写入数据较慢）
| wsrep_local_recv_queue_max       | 2                       | # 接收数据的队列最大长度
| wsrep_local_recv_queue_min       | 0                       | # 接收数据的队列最小长度
| wsrep_local_recv_queue_avg       | 0.090909                | # 接收数据的队列平均长度（重要，如果队列过长说明接收写入数据较慢）
| wsrep_local_cached_downto        | 1                       |
| wsrep_flow_control_paused_ns     | 0                       | # 限速状态下的总时间（纳秒）
| wsrep_flow_control_paused        | 0.000000                | # 限速时间的占比（0-1），如果值是0.1，说明该节点10%的时间，处于限速控制状态
| wsrep_flow_control_sent          | 0                       | # 发送限速命令给其它节点的总次数（一般只有当前节点发送数据的队列过长，才会发送限速命令给其它节点）
| wsrep_flow_control_recv          | 0                       | # 收到限速命令的总次数
| wsrep_flow_control_interval      | [ 173, 173 ]            | # 触发限速的上限和下限（当队列到达上限就会拒绝新的同步请求，到达下限才会接收新的同步请求）
| wsrep_flow_control_interval_low  | 173                     | # 触发限速的下限
| wsrep_flow_control_interval_high | 173                     | # 触发限速的上限
| wsrep_flow_control_status        | OFF                     | # 限速状态（OFF没有限速，ON表示正在限速）
| wsrep_cert_deps_distance         | 1.000000                | # 并发执行事务的数量
| wsrep_apply_oooe                 | 0.000000                | # 接收数据的队列当中需要执行事务的数量占比
| wsrep_apply_oool                 | 0.000000                | # 接收数据的队列当中事务乱序执行的频率（当前节点执行事务的顺序和同步节点执行事务的顺序不一致的频率）
| wsrep_apply_window               | 1.000000                | # 接收数据的队列当中含有事务的数据的平均数量
| wsrep_commit_oooe                | 0.000000                | # 发送数据的队列当中需要执行事务的数量占比
| wsrep_commit_oool                | 0.000000                | # 发送数据的队列当中事务乱序执行的频率（无意义，本地不存在乱序提交）
| wsrep_commit_window              | 1.000000                | # 发送数据的队列当中含有事务的数据的平均数量
| wsrep_local_state                | 4                       |
| wsrep_local_state_comment        | Synced                  | # 当前节点状态（Open（启动成功），Primary（加入集群），Joiner（正在同步数据），Joined（数据同步成功），Synced（数据同步成功，可对外提供服务），Doner（正在全量同步数据，不可对外提供服务））
| wsrep_cert_index_size            | 2                       |
| wsrep_cert_bucket_count          | 22                      |
| wsrep_gcache_pool_size           | 3200                    |
| wsrep_causal_reads               | 0                       |
| wsrep_cert_interval              | 0.000000                |
| wsrep_open_transactions          | 0                       |
| wsrep_open_connections           | 0                       |
| wsrep_ist_receive_status         |                         |
| wsrep_ist_receive_seqno_start    | 0                       |
| wsrep_ist_receive_seqno_current  | 0                       |
| wsrep_ist_receive_seqno_end      | 0                       |
| wsrep_incoming_addresses         | s1:3306,s2:3306,s3:3306 | # 集群所有节点的IP或主机名
| wsrep_cluster_weight             | 3                       |
| wsrep_desync_count               | 0                       | # 集群延迟节点数量（延迟同步数据的节点数量）
| wsrep_evs_delayed                |                         |
| wsrep_evs_evict_list             |                         |
| wsrep_evs_repl_latency           | 0/0/0/0/0               |
| wsrep_evs_state                  | OPERATIONAL             |
| wsrep_gcomm_uuid                 | 0fb05-a5d-11e9-b4-e7bfc |
| wsrep_cluster_conf_id            | 3                       |
| wsrep_cluster_size               | 3                       | # 集群节点数量（注意：如果右节点宕机，是不会减少这个数量的）
| wsrep_cluster_state_uuid         | 0fb0-a5d-11e9-a12c-9f49 |
| wsrep_cluster_status             | Primary                 | # 集群状态（Primary（正常），Non_Primary（出现裂脑），Disconnected（集群不可用（原因可能是脑裂所引起）））
| wsrep_connected                  | ON                      | # 节点是否连接到集群（OFF 否，ON 是）
| wsrep_local_bf_aborts            | 0                       |
| wsrep_local_index                | 0                       |
| wsrep_provider_name              | Galera                  |
| wsrep_provider_vendor            | Codersh Oy <io@cp.com>  |
| wsrep_provider_version           | 3.37(rff05089)          |
| wsrep_ready                      | ON                      | # 集群是否可正常使用 OFF 否 ，NO 是
+----------------------------------+-------------------------+
```


