#### 一、下载源码包（备用下载地址：https://github.com/firechiang/mysql-test/raw/master/data/sysbench-1.0.17.tar.gz）
```bash
$ wget -P /home/tools https://github.com/akopytov/sysbench/archive/1.0.17.tar.gz
```

#### 二、编译安装
```bash
$ yum -y remove mari*                                                                   # 卸载 MariaDB 所有依赖包
$ yum -y install make automake libtool pkgconfig libaio-devel mysql-devel openssl-devel # 安装编译依赖
```

#### 三、安装（注意：编译安装好的文件，可以直接拷贝到其它机器运行）
```bash
$ cd /home/tools
$ tar -zxvf sysbench-1.0.17.tar.gz -C ./                 # 解压到当前目录
$ cd sysbench-1.0.17
$ ./autogen.sh && ./configure && make -j && make install # 安装（注意：默认安装在 /usr/local/bin 目录）
```

#### 三、简单使用（注意：要先创建好 sbtest 库，否则无法测试）
```bash
$ sysbench --help                                        # 查看使用帮助
$ sysbench --version                                     # 查看 Sysbench 版本

# 生成测试数据 oltp-tables-count（自动生成测试表数量），oltp-table-size（每张表数据插入数量），prepare（生成测试数据）
$ sysbench /usr/local/share/sysbench/tests/include/oltp_legacy/oltp.lua --mysql-host=192.168.0.120 --mysql-port=3306 --mysql-user=root --mysql-password=jiang --oltp-tables-count=10 --oltp-table-size=100000 prepare

# 测试读写性能，参数说明如下
# oltp-test-mode（测试模式：simple（只测试查询），nontrx（无事物读写） complex（事物读写））
# threads（并发连接数）
# time（测试执行时间，单位秒，（注意：测试时间越长结果越准确，如果是生产测试建议大于等于24小时））
# report-interval（生成测试报告的间隔时间，单位秒）
$ sysbench /usr/local/share/sysbench/tests/include/oltp_legacy/oltp.lua --mysql-host=192.168.0.120 --mysql-port=3306 --mysql-user=root --mysql-password=jiang --oltp-test-mode=complex --threads=10 --time=30 --report-interval=10 run >> /home/sysbenchtest.log

# 每10秒一次的统计读写（我们上面配的就是10秒）结果如下
[ 10s ] thds: 10 tps: 97.57 qps: 1957.88 (r/w/o: 1371.04/390.70/196.15) lat (ms,95%): 267.41 err/s: 0.00 reconn/s: 0.00  
[ 20s ] thds: 10 tps: 111.91 qps: 2250.73 (r/w/o: 1575.69/451.23/223.81) lat (ms,95%): 164.45 err/s: 0.00 reconn/s: 0.00
[ 30s ] thds: 10 tps: 107.90 qps: 2161.39 (r/w/o: 1513.46/431.92/216.01) lat (ms,95%): 173.58 err/s: 0.20 reconn/s: 0.00
# 最终的测试结果
SQL statistics:
    queries performed（执行测试的次数）:
        read:                            44604
        write:                           12739
        other:                           6370
        total:                           63713
    transactions:                        3184   (105.93 per sec.)
    queries:                             63713  (2119.79 per sec.)
    ignored errors:                      2      (0.07 per sec.)
    reconnects:                          0      (0.00 per sec.)

General statistics:
    total time:                          30.0550s
    total number of events:              3184

Latency (ms):
         min:                                   31.93
         avg:                                   94.31
         max:                                  435.18
         95th percentile:                      189.93
         sum:                               300290.94

Threads fairness:
    events (avg/stddev):           318.4000/1.36
    execution time (avg/stddev):   30.0291/0.02

```