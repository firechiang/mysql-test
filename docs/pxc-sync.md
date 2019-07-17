#### 一、Percona XtraDB Cluster 同步流程（注意：GTID是Percona XtraDB Cluster的集群ID+事务ID，是记录单个操作的唯一标识）
![object](https://github.com/firechiang/mysql-test/blob/master/image/pxc-sync.svg)
```bash
# 查看节点最后提交的事物ID（注意：可根据最后提交的事物ID，计算出当前节点要同步多少数据）
$ show status like 'wsrep_last_committed%';
```
#### 二、普通MySQL集群的GTID是MySQL节点ID+事务ID也是记录单个操作的唯一标识，从节点同步数据就是看哪些GTID我没有，就把哪些GTID的操作数据同步过来（注意：普通MySQL集群，是从节点每隔一段时间去拉取主节点的BinLog日志数据，而不是主节点往从节点推送数据）
