#### 一、安装（8.0为例）
```bash
$ wget https://dev.mysql.com/get/mysql80-community-release-el7-2.noarch.rpm          # 下载MySQL的repo源
$ yum localinstall mysql80-community-release-el7-2.noarch.rpm -y                     # 安装下载好的rpm包（包名一般都是最后那一段）
$ yum repolist enabled | grep "mysql.*-community.*"                                  # 查看yum repository是否安装成功
$ yum install -y mysql-community-server                                              # MySQL5.7以前使用yum install mysql-server
$ service mysqld start                                                               # 启动MySQL服务
$ chkconfig mysqld on                                                                # 开机启动MySQL服务
$ grep 'temporary password' /var/log/mysqld.log                                      # 查看MySQL临时密码
$ mysql -uroot -p                                                                    # 进入MySQL服务
$ ALTER USER 'root'@'localhost' IDENTIFIED BY 'Jiang@123';                           # 设置root用户密码为 Jiang@123，且只有本地能登录                 
$ show databases;                                                                    # 查看所有库
$ use mysql;                                                                         # 进入MySQL系统库                                                           
$ select user,host from user;                                                        # 查看MySQL授权用户信息（字段 host允许远程访问的ip）
$ update user set host = '%' where user = 'root';                                    # 修改root用户允许所有IP访问
$ select user,host from user;                                                        # 现在应该只有一个用户信息了就是我刚刚加的
$ flush privileges;                                                                  # 刷新权限
$ quit                                                                               # 退出登录（或使用exit退出登录）
$ mysql -uroot -pJiang@123                                                           # 登录MySQL可以故意把密码写错看能不能登入
```

#### 二、优化配置[vi /etc/my.cnf]
```bash
default-time-zone='+8:00'
###################base###########################
max_connections=8000                       # 允许最大连接数
max_connect_errors=10                      # 允许连接失败的次数。这是为了防止有人试图攻击数据库

###################innodb#########################
innodb_buffer_pool_size = 2048M
innodb_flush_log_at_trx_commit = 1
innodb_io_capacity = 600
innodb_lock_wait_timeout = 120
innodb_log_buffer_size = 8M
innodb_log_file_size = 200M
innodb_log_files_in_group = 3
innodb_max_dirty_pages_pct = 85
innodb_read_io_threads = 8
innodb_write_io_threads = 8
innodb_thread_concurrency = 32
innodb_open_files = 300                    # 它指定了MySQL一次可以打开的最大文件数。最小值为10

###################session###########################
join_buffer_size = 8M
key_buffer_size = 256M
bulk_insert_buffer_size = 8M
max_heap_table_size = 96M
tmp_table_size = 96M
read_buffer_size = 8M
sort_buffer_size = 2M
max_allowed_packet = 64M
read_rnd_buffer_size = 32M
```

#### 三、重启MySQL
```bash
$ service mysqld restart
```
