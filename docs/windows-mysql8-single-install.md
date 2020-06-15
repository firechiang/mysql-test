#### 一、下载安装包
```bash
$ wget -P https://dev.mysql.com/get/Downloads/MySQL-8.0/mysql-8.0.20-winx64.zip
```
#### 二、解压安装包（注意：解压完成以后到MySQL目录创建data文件夹，用来存储MySQL数据文件）
#### 三、配置MySQL环境变量
#### 四、到MySQL解压目录创建my.ini配置文件
```bash
[mysqld]
default-time-zone='+8:00'
# 设置3306端口
port=3306
# 设置mysql的安装目录（注意修改）
basedir=C:\MySQL8\mysql-8.0.20-winx64
# 设置mysql数据库的数据的存放目录（注意修改）
datadir=C:\MySQL8\mysql-8.0.20-winx64\data
# 允许最大连接数
max_connections=200
# 允许连接失败的次数。这是为了防止有人从该主机试图攻击数据库系统
max_connect_errors=10
# 服务端使用的字符集默认为UTF8
character-set-server=utf8mb4
# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB
# 默认使用“mysql_native_password”插件认证
default_authentication_plugin=mysql_native_password
[mysql]
# 设置mysql客户端默认字符集
default-character-set=utf8mb4
[client]
# 设置mysql客户端连接服务端时默认使用的端口
port=3306
default-character-set=utf8mb4
```

#### 五、安装MySQL（注意：先定位到MySQL解压的bin目录（比如：C:\MySQL8\mysql-8.0.20-winx64\bin），再执行命令。如果报没有VCRUNTIME140_1.dll错误，请到百度云下载：微软常用运行库合集_2019.07.20_X64.exe 安装）
```bash
# 初始化MySQL（注意：这一步会生成一个初始密码）
$ mysqld --initialize --console

2020-06-15T03:52:11.380095Z 6 [Note] [MY-010454] [Server] A temporary password is generated for root@localhost: t;y2XYWDr%V;

t;y2XYWDr%V; 就是MySQL初始密码

# 创建名字叫mysql的MySQL服务
$ mysqld --install mysql
# 删除MySQL服务，如果有需要的话
$ sc delete mysql
```

#### 六、启动MySQL服务
```bash
# 启动MySQL服务
$ net start mysql
# 停止MySQL服务
$ net stop mysql
# 查看端口绑定情况（注意查看MySQL的3306端口是否绑定）
$ netstat -a
```

#### 七、初始化MySQL相关设置
```bash
$ mysql -uroot -p                                          # 连接mysql（注意：密码就是上面的初始密码）
$ ALTER USER 'root'@'localhost' IDENTIFIED BY 'Jiang@123'; # 设置root用户密码为 Jiang@123，且只有本地能登录
$ show databases;                                          # 查看所有库
$ use mysql;                                               # 进入MySQL系统库                                                           
$ select user,host from user;                              # 查看MySQL授权用户信息（字段 host允许远程访问的ip）
$ update user set host = '%' where user = 'root';          # 修改root用户允许所有IP访问
$ select user,host from user;                              # 现在应该只有一个用户信息了就是我刚刚加的
$ flush privileges;                                        # 刷新权限
$ quit                                                     # 退出登录（或使用exit退出登录）
$ mysql -uroot -pJiang@123                                 # 登录MySQL可以故意把密码写错看能不能登入
```
