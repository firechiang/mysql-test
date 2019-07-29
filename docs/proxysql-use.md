#### 一、下载依赖安装包，[官方使用文档](https://github.com/sysown/proxysql/wiki)
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

#### 四、配置文件简要说明（注意：默认配置文件是：/etc/proxysql.cnf，ProxySQL首次启动使用配置文件，以后启动使用ProxySQL配置库里面的数据，而且ProxySQL配置库里面的数据修改了，会实时更新，可以不用重启ProxySQL）
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
	monitor_username="monitor"
	monitor_password="monitor"
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

# 要代理的MySQL服务列表
mysql_servers =
(
#	{
#		address = "127.0.0.1"     # 主机名或IP（注意：这个参数是必填的）
#		port = 3306               # 端口（注意：这个参数是必填的）
#		hostgroup = 0	          # 分组ID，同一类机器的值需要一致（注意：这个参数是必填的）
#		status = "ONLINE"         # 状态（默认值 ONLINE）（注意：这个参数是必填的）
#		weight = 1                # 权重（默认值1）（注意：这个参数是必填的）
#		compression = 0           # 压缩标识（默认0）（注意：这个参数是必填的）
#     max_replication_lag = 10  # 如果大于0且复制延迟超过此阈值，则服务器将被回避（默认值 0）
#     address = "/var/lib/mysql/mysql.sock"
#     max_connections=200
#	},
#	{ address="127.0.0.2" , port=3306 , hostgroup=0, max_connections=5 }
)


# 要代理的MySQL的所有用户
mysql_users:
(
#	{
#		username = "username"     # 用户名（注意：这个参数是必填的）
#		password = "password"     # 密码（注意：这个参数是必填的）
#		default_hostgroup = 0     # 分组（默认值0）
#	   active = 1                # 是否启用（默认值 1。0不启用，1启用）
#     max_connections = 1000    # 最大连接数
#     default_schema = "test"   # 默认连接库
#	},
#	{ username = "user1" , password = "password" , default_hostgroup = 0 , active = 0 }
)

# defines MySQL Query Rules
mysql_query_rules:
(
#	{
#		rule_id=1
#		active=1
#		match_pattern="^SELECT .* FOR UPDATE$"
#		destination_hostgroup=0
#		apply=1
#	},
#	{
#		rule_id=2
#		active=1
#		match_pattern="^SELECT"
#		destination_hostgroup=1
#		apply=1
#	}
)

# 定时任务相关配置
scheduler=
(
#  {
#    id=1
#    active=0
#    interval_ms=10000
#    filename="/var/lib/proxysql/proxysql_galera_checker.sh"
#    arg1="0"
#    arg2="0"
#    arg3="0"
#    arg4="1"
#    arg5="/var/lib/proxysql/proxysql_galera_checker.log"
#  }
)


mysql_replication_hostgroups=
(
#        {
#                writer_hostgroup=30
#                reader_hostgroup=40
#                comment="test repl 1"
#       },
#       {
#                writer_hostgroup=50
#                reader_hostgroup=60
#                comment="test repl 2"
#        }
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
$ mysql -u admin -padmin -h 127.0.0.1 -P6032             # 进入ProxySQL服务
$ show databases;
+-----+---------------+-------------------------------------+
| seq | name          | file                                |
+-----+---------------+-------------------------------------+
| 0   | main          |                                     | # ProxySQL服务管理配置相关库 
| 2   | disk          | /var/lib/proxysql/proxysql.db       |
| 3   | stats         |                                     |
| 4   | monitor       |                                     | # ProxySQL监控相关库
| 5   | stats_history | /var/lib/proxysql/proxysql_stats.db |
+-----+---------------+-------------------------------------+

$ use main;show tables;
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

$ show create table global_variables;           # 查看表global_variables的建表语句
```