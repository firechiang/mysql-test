#### 一、下载依赖安装包
```bash
$ cd /home/tools
# 下载 Percona-Server 5.7.22 安装包
wget -P /home/tools/percona-server https://www.percona.com/downloads/Percona-Server-5.7/Percona-Server-5.7.26-29/binary/redhat/7/x86_64/Percona-Server-5.7.26-29-r11ad961-el7-x86_64-bundle.tar
# 内存分配器依赖用于解决内存碎片问题
$ wget -P /home/tools/percona-server https://repo.percona.com/release/7/RPMS/x86_64/jemalloc-3.6.0-1.el7.x86_64.rpm
```

#### 二、安装
```bash
# 卸载 MariaDB 所有依赖包
$ yum -y remove mari*
$ cd /home/tools/percona-server
# 解压 Percona-Server 安装包
$ tar -xvf Percona-Server-5.7.26-29-r11ad961-el7-x86_64-bundle.tar
# 安装所有安装包
$ yum localinstall *.rpm
```

#### 三、修改[vi /etc/my.cnf]配置
```bash
character_set_server=utf-8
bind-address=0.0.0.0
# 跳过DNS解析
skip-name-resolve
```

#### 四、开放3306端口
```bash
$ firewall-cmd --zone=public --add-port=3306/tcp --permanent   # 开放3306端口
$ firewall-cmd --reload                                        # 刷新配置
```

#### 五、启动服务（注意：如果是Percona-Server集群建议不要设置开机启动，原因是：Percona-Server节点在启动后会随机找一台机器同步数据，如果数据量过大，Percona-Server集群会限制其它的写入操作，直到数据同步完成）
```bash
$ service mysqld start                                         # 启动服务
$ service mysqld restart                                       # 重启服务
$ service mysqld stop                                          # 停止服务
$ chkconfig mysqld on                                          # 开启开机启动
$ chkconfig mysqld off                                         # 禁止开机启动
```

#### 六、修改root账号密码
```bash
$ grep 'temporary password' /var/log/mysqld.log                # 查看mysql默认root账号密码
$ mysql -uroot -p                                              # 进入MySQL服务
$ ALTER USER 'root'@'localhost' IDENTIFIED BY 'Jiang@123';     # 设置root用户密码为 Jiang@123，且只有本地能登录                 
$ show databases;                                              # 查看所有库
$ use mysql;                                                   # 进入MySQL系统库
# 以下修改看实际情况而定
$ select user,host from user;                                  # 查看MySQL授权用户信息（字段 host允许远程访问的ip）
$ update user set host = '%' where user = 'root';              # 修改root用户允许所有IP访问
$ select user,host from user;                                  # 现在应该只有一个用户信息了就是我刚刚加的
$ flush privileges;                                            # 刷新权限
$ quit                                                         # 退出登录（或使用exit退出登录）
$ mysql -uroot -pJiang@123                                     # 登录MySQL可以故意把密码写错看能不能登入
```

#### 七、创建用户
```bash
$ mysql -u root -p                                             # 进入MySQL服务
$ CREATE USER 'admin'@'%' IDENTIFIED BY 'Jiang@123';           # 创建用户admin密码Jiang@123，%是指所有IP都可以连接
$ GRANT all privileges ON *.* TO 'admin'@'%';                  # 将所有权限都赋给admin账号
$ flush privileges;                                            # 刷新权限
$ quit                                                         # 退出登录（或使用exit退出登录）
```
