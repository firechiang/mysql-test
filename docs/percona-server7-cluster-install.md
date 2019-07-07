#### 一、下载依赖安装包
```bash
# 下载 Percona-XtraDB-Cluster 5.7.26 安装包
$ wget -P /home/tools/Percona-XtraDB-Cluster https://www.percona.com/downloads/Percona-XtraDB-Cluster-LATEST/Percona-XtraDB-Cluster-5.7.26-31.37/binary/redhat/7/x86_64/Percona-XtraDB-Cluster-5.7.26-31.37-r505-el7-x86_64-bundle.tar
# 下载 qpree解压缩工具
$ wget -P /home/tools/Percona-XtraDB-Cluster https://repo.percona.com/release/7/RPMS/x86_64/qpress-11-1.el7.x86_64.rpm
```
#### 二、开放 Percona XtraDB Cluster 所使用的端口
```bash
$ firewall-cmd --zone=public --add-port=3306/tcp --permanent   # 开放3306（MySQL 服务端口）
$ firewall-cmd --zone=public --add-port=4444/tcp --permanent   # 开放4444（请求全量同步数据（SST）端口（注意：全量同步会限制其它节点的写入操作，一般在数据库启动时会执行全量同步））
$ firewall-cmd --zone=public --add-port=4567/tcp --permanent   # 开放4567（数据库节点之间的通信端口）
$ firewall-cmd --zone=public --add-port=4568/tcp --permanent   # 开放4568（请求增量同步（IST）端口）
$ firewall-cmd --reload                                        # 刷新配置
```

#### 二、修改[vi /etc/selinux/config]关闭SELinux安全验证（不建议关闭）（注意：需要重启机器才能生效）
```bash
SELINUX=disabled
```
