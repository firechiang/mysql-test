![object](https://github.com/firechiang/mysql-test/blob/master/image/mycat-sharding.svg)
#### 一、数据分片算法简要说明
```bash
1，主键求模切分（适用于数据增长熟读慢，有明确组件的数据（难于增加分片，要增加分片数建议使用2*（原有分片数）增加），有利于数据切片）
2，根据某个字段的值（数字）切分（适用于归类存储数据，使用大多数业务（容易增加分片））
3，主键值范围切分（适用于数据快速增长（容易增加分片））
4，日期切分（适用于数据快速增长（容易增加分片））
```
#### 二、下载 Mycat-Server 安装包
```bash
$ wget -P /home/tools http://dl.mycat.io/1.6.7.1/Mycat-server-1.6.7.1-release-20190627191042-linux.tar.gz
```

#### 三、安装Mycat-Server（注意：Mycat-Server实际是模拟MySQL节点）
```bash
$ cd /home/tools
# 解压到上层目录
$ tar -zxvf Mycat-server-1.6.7.1-release-20190627191042-linux.tar.gz -C ../
```

#### 四、修改[vi /home/mycat/conf/server.xml]配置虚拟用户名，密码，逻辑库。供客户端使用（注意：先删除所有<user>标签，然后在文件末尾添加）
```bash
<!-- 用户名 test_admin，密码 Jiang@123，可使用虚拟库test_test（就是schema.xml配置文件里面配置的那个schema名称），拥有所有权限 -->
<user name="test_admin" defaultAccount="true">
    <property name="password">Jiang@123</property>
    <property name="schemas">test_test</property>
    <!-- 表级 DML 权限设置 -->
    <!--
    <privileges check="false">
	    <schema name="TESTDB" dml="0110" >
		    <table name="tb01" dml="0000"></table>
		    <table name="tb02" dml="1111"></table>
	    </schema>
    </privileges>
    -->
</user>

<!-- 用户名 tes_user，密码 Jiang@123，可使用 test_test 库，拥有只读权限 -->
<user name="tes_user">
    <property name="password">Jiang@123</property>
    <property name="schemas">test_test</property>
    <property name="readOnly">true</property>
</user>
```

#### 五、修改[vi /home/mycat/conf/schema.xml]配置连接，读写分离，负载均衡，数据表映射（注意：先删除原有的配置信息，然后在文件末尾添加）
```bash
<!-- 配置数据库 test_test -->
<schema name="test_test" checkSQLschema="false" sqlMaxLimit="100">
    <!-- 以下可以配置多张表，每个表一个切片算法-->
    <!-- 表m_dept，两个全量集群或节点 dn1,dn2，切片算法custom-field-int是我们在下面自定义的rule.xml配置文件tableRule标签name的值（根据sharding_id的值，进行切片）-->
    <table name="m_dept" dataNode="dn1,dn2" rule="custom-field-sharding" />
</schema>

<!-- 配置分片关系 -->
<dataNode name="dn1" dataHost="cluster001" database="test_test" />
<dataNode name="dn2" dataHost="cluster002" database="test_test" />

<!-- 配置 cluster001 连接信息 -->
<!-- maxCon=连接池最大数；minCon=连接池最小数 ；balance=负载均衡（0不开启，1保证一个写节点，其它可读可写，2所有节点可读可写，3要么只读，要么只写（根据节点配置的标签来分））；-->
<!-- writeType=0所有写操作分发给第一个节点（如果宕机则选择第二个），1所有的写操作由所有的写节点分担；switchType=1根据mycat心跳信息来判断宕机切换节点，2根据数据集群信息来判断宕机切换节点-->
<dataHost name="cluster001" maxCon="1000" minCon="10" balance="2" writeType="1" dbType="mysql" dbDriver="native" switchType="1"  slaveThreshold="100">

    <!-- 心跳检测的sql语句 -->
    <heartbeat>select user()</heartbeat>
    
    <!-- 配置读写节点（注意：如果写节点宕机了，要自动切换到其它写节点，所以我们在下面配置了3个写节点 C1W1，C1W2，C1W3） -->
	<!-- 配置当前写节点带2个读节点 -->
	<writeHost host="C1W1" url="server001:3306" user="root" password="Jiang@123">
		<!-- 读节点 -->
		<readHost host="C1W1R1" url="server002:3306" user="root" password="Jiang@123" />
		<!-- 读节点 -->
		<readHost host="C1W1R2" url="server003:3306" user="root" password="Jiang@123" />
	</writeHost>
	
	<!-- 配置当前写节点带2个读节点 -->
	<writeHost host="C1W2" url="server002:3306" user="root" password="Jiang@123">
		<!-- 读节点 -->
		<readHost host="C1W2R1" url="server001:3306" user="root" password="Jiang@123" />
		<!-- 读节点 -->
		<readHost host="C1W2R2" url="server003:3306" user="root" password="Jiang@123" />
	</writeHost>
	
	<!-- 配置当前写节点带2个读节点 -->
	<writeHost host="C1W3" url="server003:3306" user="root" password="Jiang@123">
		<!-- 读节点 -->
		<readHost host="C1W3R1" url="server001:3306" user="root" password="Jiang@123" />
		<!-- 读节点 -->
		<readHost host="C1W3R2" url="server002:3306" user="root" password="Jiang@123" />
	</writeHost>
</dataHost>


<!-- 配置 cluster002 连接信息-->
<!-- maxCon=连接池最大数；minCon=连接池最小数 ；balance=负载均衡（0不开启，1保证一个写节点，其它可读可写，2所有节点可读可写，3要么只读，要么只写（根据节点配置的标签来分））；-->
<!-- writeType=0所有写操作分发给第一个节点（如果宕机则选择第二个），1所有的写操作由所有的写节点分担；switchType=1根据mycat心跳信息来判断宕机切换节点，2根据数据集群信息来判断宕机切换节点-->
<dataHost name="cluster002" maxCon="1000" minCon="10" balance="2" writeType="1" dbType="mysql" dbDriver="native" switchType="1"  slaveThreshold="100">
    <!-- 心跳检测的sql语句 -->
    <heartbeat>select user()</heartbeat>
    <!-- 配置读写节点（注意：如果写节点宕机了，要自动切换到其它写节点，所以我们在下面配置了2个写节点 C2W1，C2W2） -->
	<!-- 配置当前写节点带1个读节点 -->
	<writeHost host="C2W1" url="server005:3306" user="root" password="Jiang@123">
		<!-- 读节点 -->
		<readHost host="C2W1R1" url="server006:3306" user="root" password="Jiang@123" />
	</writeHost>
	
	<!-- 配置当前写节点带1个读节点 -->
	<writeHost host="C2W2" url="server006:3306" user="root" password="Jiang@123">
		<!-- 读节点 -->
		<readHost host="C2W2R1" url="server005:3306" user="root" password="Jiang@123" />
	</writeHost>
</dataHost>
```

#### 六、修改[vi /home/mycat/conf/rule.xml]配置切片算法相关信息（注意：tableRule标签配置要和配置文件里面的tableRule标签放到一起，function标签配置要和配置文件里面的function标签放到一起，否则会报错）
```bash
<!-- 配置切片规则名字叫 custom-field-sharding（注意：最好和算法的名称一致） -->
<tableRule name="custom-field-sharding">
    <rule>
         <!-- 用于切片字段 -->
        <columns>sharding_id</columns>
        <!-- 算法的名称（就是我们下面定义的那个名称） -->
        <algorithm>custom-field-sharding</algorithm>
    </rule>
</tableRule>

<!-- 定义切片算法，它的名字叫 custom-field-sharding -->
<function name="custom-field-sharding" class="io.mycat.route.function.PartitionByFileMap">
    <!-- 切片规则配置文件名称（文件在conf目录下定义文件内容：1000=0 说明切片字段sharding_id的值等于1000的数据，放到第0个切片）-->
    <property name="mapFile">custom-field-sharding.txt</property>
</function>
```

#### 七、修改[vi /home/mycat/conf/custom-field-sharding.txt]配置切片规则文件（注意：这个文件没有，要手动创建）
```bash
1000=0
2000=1
```

#### 八、修改[vi /etc/selinux/config]关闭SELinux安全验证（注意：集群每个节点都要修改，需要重启机器才能生效）
```bash
#SELINUX=enforcing
SELINUX=disabled
```

#### 九、开放 Mycat-Server 所使用的端口（注意：集群每个节点都要配置）
```bash
$ firewall-cmd --zone=public --add-port=8066/tcp --permanent # 开放8066（Mycat-Server 数据服务端口（数据增删改查的端口））
$ firewall-cmd --zone=public --add-port=9066/tcp --permanent # 开放9066（Mycat-Server 管理服务端口）
$ firewall-cmd --reload                                      # 刷新配置
```

#### 十、Mycat-Server 基本操作
```bash
$ cd /home/mycat/bin                                         # 到 Mycat-Server bin 目录
$ chmod -R 777 ./*.sh                                        # 赋予最高权限
$ ./mycat start                                              # 启动 Mycat-Server
$ ./mycat stop                                               # 停止 Mycat-Server
$ ./mycat restart                                            # 重启 Mycat-Server
```

#### 十一、Mycat-Server 热加载配置文件
```bash
$ mysql -h127.0.0.1 -P 9066 -utest_admin -p                  # 进入MyCat Server
$ reload @@config_all;                                       # 热加载所有配置文件
```
