#### 一、下载依赖安装包，[官方使用文档](https://github.com/sysown/proxysql/wiki)，[中文翻译文档](https://github.com/malongshuai/proxysql/wiki)
```bash
# 官方下载地址：https://repo.proxysql.com/ProxySQL/proxysql-2.0.x/centos/7/proxysql-2.0.5-1-centos7.x86_64.rpm
$ wget -P /home/tools/proxysql https://github.com/sysown/proxysql/releases/download/v2.0.5/proxysql-2.0.5-1-centos7.x86_64.rpm
```

#### 二、安装依赖包
```bash
$ cd /home/tools/proxysql
$ yum localinstall proxysql-2.0.5-1-centos7.x86_64.rpm   # 安装所有安装包
```

#### 三、proxysql命令简单使用（注意：默认配置文件是：/etc/proxysql.cnf，默认启动脚本是：/etc/rc.d/init.d/proxysql）
```bash
$ proxysql -V                                            # 查看ProxySQL版本号
$ proxysql -help                                         # 查看proxysql命令使用帮助
```

#### 四、配置文件简要说明（注意：除了ProxySQL服务数据存储目录和日志目录在配置文件里面修改，其它的配置都不要在配置文件里面配置，而是使用ProxySQL管理服务进行配置； 默认配置文件是：/etc/proxysql.cnf，ProxySQL首次启动使用配置文件，以后启动使用的是ProxySQL配置库里面的数据）
```bash
# ProxySQL服务数据存储目录
datadir="/var/lib/proxysql"
# ProxySQL日志目录
errorlog="/var/lib/proxysql/proxysql.log"

# ProxySQL管理服务相关配置（就是我们要管理ProxySQL，就连接这个服务）
admin_variables=
{
	admin_credentials="admin:admin"
    #mysql_ifaces="127.0.0.1:6032;/tmp/proxysql_admin.sock"
	mysql_ifaces="0.0.0.0:6032"
    #refresh_interval=2000
    #debug=true
}

# ProxySQL代理服务相关配置（就是我们的应用要连接这个服务，执行SQL，这个服务会帮我们把SQL再做转发）
mysql_variables=
{
	threads=4
	max_connections=2048
	default_query_delay=0
	default_query_timeout=36000000
	have_compress=true
	poll_timeout=2000
    #interfaces="0.0.0.0:6033;/tmp/proxysql.sock"
	interfaces="0.0.0.0:6033"
	default_schema="information_schema"
	stacksize=1048576
	server_version="5.5.30"
	connect_timeout_server=3000
    #make sure to configure monitor username and password
    #https://github.com/sysown/proxysql/wiki/Global-variables#mysql-monitor_username-mysql-monitor_password
	monitor_username="monitor"                 # 监控用户账号
	monitor_password="monitor"                 # 监控用户密码
	mysql-servers_stats=true                   # 开启记录监控数据
	monitor_history=600000
	monitor_connect_interval=60000
	monitor_ping_interval=10000
	monitor_read_only_interval=1500
	monitor_read_only_timeout=500
	ping_interval_server_msec=120000
	ping_timeout_server=500
	commands_stats=true
	sessions_sort=true
	connect_retries_on_failure=10
}

# 要代理的MySQL服务列表（注意：多个以逗号隔开）
mysql_servers =
(
    {
	    address = "127.0.0.1"                  # 主机名或IP（注意：这个参数是必填的）
		port = 3306                            # 端口（注意：这个参数是必填的）
		hostgroup = 0	                       # 分组ID（默认值0），同一类机器的值需要一致（注意：这个参数是必填的）
		status = "ONLINE"                      # 状态（默认值 ONLINE）（注意：这个参数是必填的）
		weight = 1                             # 流量权重（默认值1）（注意：这个参数是必填的）
		compression = 0                        # 压缩标识（默认0）（注意：这个参数是必填的）
        max_replication_lag = 10               # 如果这个值设置大于0，且从库复制延迟大于此阈值，则不会将读请求的SQL转发到这台机器（默认值 0）
        max_latency_ms = 5000                  # 如果机器响应时间超过阈值（默认0不做限制，单位毫秒），将不会将SQL转发到这台机器上去（可以防止数据库雪崩）
        max_connections=200                    # 最大连接数
        #address = "/var/lib/mysql/mysql.sock"
    }
)


# 要代理的MySQL的所有用户（注意：多个以逗号隔开）
mysql_users:
(
    {
	    username = "root"                      # 用户名（注意：这个参数是必填的）
		password = "Jiang@123"                 # 密码（注意：这个参数是必填的）
		default_hostgroup = 0                  # 这个用户用在哪个分组ID的机器上（注意：默认值0，且这个参数是必填的）
	    active = 1                             # 是否启用（默认值 1。0不启用，1启用）
        max_connections = 1000                 # 最大连接数
        default_schema = "test"                # 默认连接库
        backend=1                              # ProxySQL代理服务是否可以用该用户连接MySQL（默认值1，0否，1是）
        frontend=1                             # 是否可以用该用户连接ProxySQL代理服务（默认值1，0否，1是）
	}
)

# SQL转发规则（注意：如果机器组没有配置SQL转发规则，那就是所有的SQL都可以执行）
mysql_query_rules:
(
    {
	    rule_id=1                              # 这个ID在库里面是自增的，所以插入数据时最好不要设置值
		active=1                               # 是否启用（默认值1，0否，1是）
		match_pattern="^SELECT .* FOR UPDATE$" # 匹配SQL的正则表达式（匹配查询For Update（查询加锁））
		destination_hostgroup=0                # 这条规则用在哪个分组ID的机器上
		apply=1
        cache_ttl=10000                        # SQL查询到的数据缓存时间（单位毫秒），null表示不缓存（注意：不建议配置缓存）
	},
	{
		rule_id=2                              # 这个ID在库里面是自增的，所以插入数据时最好不要设置值
		active=1                               # 是否启用（默认值1，0否，1是）
		match_pattern="^SELECT"                # 匹配SQL的正则表达式（匹配所有查询）
		destination_hostgroup=1                # 这条规则用在哪个分组ID的机器上
		apply=1
		cache_ttl=10000                        # SQL查询到的数据缓存时间（单位毫秒），null表示不缓存（注意：不建议配置缓存）
    }
)

# 定义传统的异步、半同步主从复制主机组
# 注意：如果MySQL是基于组复制(group replication)或者InnoDB Cluster，请使用mysql_group_replication_hostgroups表
# 注意：如果是Galera或者PXC(Percona XtraDB Cluster)，请使用mysql_galera_hostgroups表
mysql_replication_hostgroups=
(
    {
        writer_hostgroup=30
        reader_hostgroup=40
        comment="test repl 1"
    },
    {
        writer_hostgroup=50
        reader_hostgroup=60
        comment="test repl 2"
    }
)

# 定时任务相关配置（注意：多个以逗号隔开）
scheduler=
(
    {
        id=1
        active=0                               # 是否启动该任务（0否，1是）
        interval_ms=10000                      # 任务执行间隔时间（单位毫秒）
        filename="/var/lib/proxysql/test_t.sh" # 任务要执行的脚本
        arg1="0"                               # 任务执行时脚本所使用的参数
        arg2="0"
        arg3="0"
        arg4="1"
        arg5="/var/lib/proxysql/proxysql_galera_checker.log"
    }
)

```

#### 五、启动和停止ProxySQL服务
```bash
$ service proxysql start                                 # 启动ProxySQL服务
$ service proxysql restart                               # 重启ProxySQL服务
$ service proxysql stop                                  # 停止ProxySQL服务
```

#### 六、ProxySQL服务，简单操作（注意：默认6032是服务管理绑定端口，3306是SQL转发绑定端口，ProxySQL也是模拟MySQL节点，再做SQL转发）
```bash
$ mysql -u admin -padmin -h 127.0.0.1 -P6032             # 进入ProxySQL服务（注意：admin账号，只能本地登录）
$ show databases;
+-----+---------------+-------------------------------------+
| seq | name          | file                                |
+-----+---------------+-------------------------------------+
| 0   | main          |                                     | # ProxySQL服务管理配置相关库 
| 2   | disk          | /var/lib/proxysql/proxysql.db       | # ProxySQL服务数据文件
| 3   | stats         |                                     | # 监控相关状态和数据储库
| 4   | monitor       |                                     | # ProxySQL监控相关库
| 5   | stats_history | /var/lib/proxysql/proxysql_stats.db | # 监控历史数据文件
+-----+---------------+-------------------------------------+

$ show tables from main;                        # 查看main库里面的所有表
+--------------------------------------------+
| tables                                     |
+--------------------------------------------+
| global_variables                           |  # ProxySQL服务管理相关数据表
| mysql_aws_aurora_hostgroups                |
| mysql_collations                           |
| mysql_galera_hostgroups                    |
| mysql_group_replication_hostgroups         |
| mysql_query_rules                          |
| mysql_query_rules_fast_routing             |
| mysql_replication_hostgroups               |  # MySQL主从分组数据表
| mysql_servers                              |  # 要代理的MySQL列表数据表
| mysql_users                                |  # 要代理的MySQL所使用的用户数据表
| proxysql_servers                           |
| runtime_checksums_values                   |
| runtime_global_variables                   |
| runtime_mysql_aws_aurora_hostgroups        |
| runtime_mysql_galera_hostgroups            |
| runtime_mysql_group_replication_hostgroups |
| runtime_mysql_query_rules                  |
| runtime_mysql_query_rules_fast_routing     |
| runtime_mysql_replication_hostgroups       |
| runtime_mysql_servers                      |
| runtime_mysql_users                        |
| runtime_proxysql_servers                   |
| runtime_scheduler                          |
| scheduler                                  |
+--------------------------------------------+

$ show tables from monitor;                     #  查看monitor监控库里面的所有表

+--------------------------------------+
| tables                               |
+--------------------------------------+
| mysql_server_aws_aurora_check_status |
| mysql_server_aws_aurora_failovers    |
| mysql_server_aws_aurora_log          |
| mysql_server_connect_log             |
| mysql_server_galera_log              |        # 节点监控记录
| mysql_server_group_replication_log   |
| mysql_server_ping_log                |
| mysql_server_read_only_log           |
| mysql_server_replication_lag_log     |
+--------------------------------------+

$ show tables from stats;                       #  查看stats监控状态库里面的所有表

+--------------------------------------+
| tables                               |
+--------------------------------------+
| global_variables                     |
| stats_memory_metrics                 |
| stats_mysql_commands_counters        |        # SQL语句执行频率表 
| stats_mysql_connection_pool          |        # 连接池相关记录
| stats_mysql_connection_pool_reset    |        # 清空连接池记录数据（注意：访问一下即可清空）
| stats_mysql_errors                   |
| stats_mysql_errors_reset             |
| stats_mysql_free_connections         |
| stats_mysql_global                   |
| stats_mysql_gtid_executed            |
| stats_mysql_prepared_statements_info |
| stats_mysql_processlist              |
| stats_mysql_query_digest             |        # SQL语句执行记录（注意：会有执行次数）
| stats_mysql_query_digest_reset       |        # 清空状态记录数据（注意：访问一下即可清空）
| stats_mysql_query_rules              |        # SQL转发规则记录
| stats_mysql_users                    |
| stats_proxysql_servers_checksums     |
| stats_proxysql_servers_metrics       |
| stats_proxysql_servers_status        |
+--------------------------------------+

$ show create table global_variables;           # 查看表global_variables的建表语句
```

#### 七、使配置立即生效和持久化
```bash
$ mysql -u admin -padmin -h 127.0.0.1 -P6032    # 进入ProxySQL服务（注意：admin账号，只能本地登录）

$ load admin variables to runtime;              # 将main库global_variables表里面admin开头的配置加载到运行时（立即生效）
$ load mysql variables to runtime;              # 将main库global_variables表里面mysql开头的配置加载到运行时（立即生效）
$ load mysql servers to runtime;                # 将main库mysql_servers表里面所有的配置加载到运行时（立即生效）
$ load mysql users to runtime;                  # 将main库mysql_users表里面所有的配置加载到运行时（立即生效）
$ load mysql query rules to runtime;            # 将main库mysql_query_rules表里面所有的配置加载到运行时（立即生效）


$ save admin variables to mem;                  # 将main库global_variables表里面admin开头的配置加载到内存（提高效率）
$ save mysql variables to mem;                  # 将main库global_variables表里面mysql开头的配置加载到内存（提高效率）
$ save mysql servers to mem;                    # 将main库mysql_servers表里面所有的配置加载到内存（提高效率）
$ save mysql users to mem;                      # 将main库mysql_users表里面所有的配置加载到内存（提高效率）
$ save mysql query rules to mem;                # 将main库mysql_query_rules表里面所有的配置加载到内存（提高效率）

$ save admin variables to disk;                 # 将main库global_variables表里面admin开头的配置持久化（ProxySQL服务重启还有效）
$ save mysql variables to disk;                 # 将main库global_variables表里面mysql开头的配置持久化（ProxySQL服务重启还有效）
$ save mysql servers to disk;                   # 将main库mysql_servers表里面所有的配置持久化（ProxySQL服务重启还有效）
$ save mysql users to disk;                     # 将main库mysql_users表里面所有的配置持久化（ProxySQL服务重启还有效）
$ save mysql query rules to disk;               # 将main库mysql_query_rules表里面所有的配置持久化（ProxySQL服务重启还有效）
```

