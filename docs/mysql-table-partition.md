#### 一、表分区的优点简要说明（注意：表分区实际是将数据存储到不同磁盘上以提高效率和存储量，所以需要机器有多块磁盘，一张表最大1024个分区）
```bash
1，有助数据写入和读取的效率
2，避免出现表锁，只会锁住相关分区
```
#### 二、表分区的缺点简要说明（注意：表分区实际是将数据存储到不同磁盘上以提高效率和存储量，所以需要机器有多块磁盘，一张表最大1024个分区）
```bash
1，不支持存储过程，存储函数和某些特殊函数
2，不支持按位运算
3，分区不能子查询
4，分区建立后尽量不要修改数据库模式
```

#### 三、挂载新硬盘以及创建分区（注意：一块磁盘最多只能有4个分区，至少要有一个主分区，最多只能有一个扩展分区。主分区格式化之后就可以使用，扩展分区必须先划分逻辑分区，格式化所有的逻辑分区以后才能使用。主分区和扩展分区都可以安装系统。建议都创建主分区）
```bash
$ fdisk -l                            # 查看机器硬盘信息，找到新添加的硬盘名称
$ fdisk /dev/sdb                      # 管理磁盘

Welcome to fdisk (util-linux 2.23.2).

Changes will remain in memory only, until you decide to write them.
Be careful before using the write command.

Device does not contain a recognized partition table
Building a new DOS disklabel with disk identifier 0x700dc500.

Command (m for help): n               # 创建新的分区（n=创建新的分区，d=删除分区，p=列出分区列表，w=保存分区信息并退出，q=退出而不保存）
Partition type:
   p   primary (0 primary, 0 extended, 4 free)
   e   extended
Select (default p): p                 #（p=创建主分区，e=创建扩展分区）
Partition number (1-4, default 1): 1  # 当前磁盘的分区号（一块磁盘最多4个分区，所以最大是4，详情请看上面的注意事项）
First sector (2048-20971519, default 2048): 
Using default value 2048              # 分区容量从磁盘容量的哪个位置开始
Last sector, +sectors or +size{K,M,G} (2048-20971519, default 20971519): 
Using default value 20971519          # 分区容量到磁盘容量的哪个位置结束
Partition 1 of type Linux and of size 10 GiB is set

Command (m for help): w               # 保存分区信息并退出（n=创建新的分区，d=删除分区，p=列出分区列表，w=保存分区信息并退出，q=退出而不保存）
The partition table has been altered!

Calling ioctl() to re-read partition table.
Syncing disks.

$ fdisk -l                            # 查看机器硬盘信息，找到新添加的硬盘和分区名称
$ mkfs -t ext4 /dev/sdb1              # 格式化新建分区（将新建分区/dev/sdb1格式化成ext4格式）

# 修改/etc/fstab文件，将磁盘分区/dev/sdb1，永久的挂载到/mnt/test_data目录，磁盘格式是ext4
# 注意：要重启机器才能生效（命令：reboot）
$ echo /dev/sdb1 /mnt/test_data ext4 defaults 0 0 >> /etc/fstab                       
```

#### 四、给MySQL表分区，数据存储目录分配用户和用户组（注意：如果不分配用户和用户组是无法在数据存储目录保存数据的）
```bash
# 创建分区数据存放目录
$ mkdir /mnt/test_data/mysql_data1
# 给分区数据存储目录分配用户和用户组
$ chown -R mysql:mysql /mnt/test_data/mysql_data1
```

#### 五、修改[vi /etc/my.cnf]配置（注意：以下修改是针对Percona-XtraDB-Cluster集群的节点，如果不是就不同修改，Percona-Server也不用修改）
```bash
# PERMISSIVE（宽容模式，允许PXC节点作任何操作）和DISABLED（禁用）都可以
pxc_strict_mode=PERMISSIVE
```

#### 六、表分区方式说明和简单使用（注意：data directory=分区数据存储目录，如果是MySQL集群，每个节点的数存储目录都要存在否则会报错）
##### 6.1、Range分区是根据某个字段的值（一定要是整数，而且要是主键字段）的范围进行分区
```bash
# 创建表并使用ID的值的范围进行分区，，1-1000的id在t0分区，1001-2000的id在t1分区，大于2000的id在t3分区，分区名字都可以顺便起，（注意：这种分区方式不推荐使用，因为ID一直在增长，那么分区也要随之增加）
$ create table test_range_1(
    id int unsigned primary key,
    name varchar(64) not null
)partition by range(id)(
    partition t0 values less than(1000) data directory="/mnt/test_data/mysql_data1",
    partition t1 values less than(2000) data directory="/mnt/test_data/mysql_data2",
    partition t3 values less than maxvalue data directory="/mnt/test_data/mysql_data3"
);

# 创建表并使用create_time的月份的值的范围进行分区，1-6月在t0分区，7-12月在t1分区，分区名字都可以顺便起，这种方式推荐生产使用（注意：因为只能根据主键字段分区，所以作成了复合主键）
$ create table test_range_2(
    id int unsigned,
    name varchar(64) not null,
    create_time date not null,
    primary key(id,create_time)
)partition by range(month(create_time))(
    partition t0 values less than(6) data directory="/mnt/test_data/mysql_data1",
    partition t1 values less than(12) data directory="/mnt/test_data/mysql_data2"
);

# 查看分区表信息，以及数据信息
$ select * from information_schema.`partitions` where table_schema=schema() and table_name='test_range_2';
```

##### 6.2、List分区是根据某个字段（一定要是主键字段）的某些值（值一定要是整数）进行分区
```bash
# 创建表并使用category_id的某些值进行分区，值等于1，2的在t0分区，值等于4，5的在t1分区，分区名字都可以顺便起，这种方式推荐生产使用（注意：因为只能根据主键字段分区，所以可以复合主键字段）
$ create table test_list_1(
    id int unsigned,
    name varchar(64) not null,
    category_id int not null,
    primary key(id,category_id)
)partition by list(category_id)(
    partition t0 values in(1,2) data directory="/mnt/test_data/mysql_data1",
    partition t1 values in(4,5) data directory="/mnt/test_data/mysql_data2"
);

# 查看分区表信息，以及数据信息
$ select * from information_schema.`partitions` where table_schema=schema() and table_name='test_list_1';
```

##### 6.3、Hash分区是根据某个字段（一定要是主键字段）的值求模进行分区（注意：值要是整数或者是日期，时间戳，字符串经过函数运算结果是整数，也可以）
```bash
# 创建表并使用id与2求模的值进行分区（注意：与N求模，就要配置N个分区，以供数据存储），分区名字都可以顺便起，这种方式推荐生产使用（注意：可以使用复合主键的某个字段进行取模）
$ create table test_hash_1(
    id int unsigned primary key,
    name varchar(64) not null,
    category_id int not null
)partition by hash(id) partitions 2(
    partition t0 data directory="/mnt/test_data/mysql_data1",
    partition t1 data directory="/mnt/test_data/mysql_data2"
);

# 创建表并使用create_time的年份与2求模的值进行分区（注意：与N求模，就要配置N个分区，以供数据存储），分区名字都可以顺便起，这种方式推荐生产使用（注意：因为只能根据主键字段分区，所以作成了复合主键）
$ create table test_hash_2(
    id int not null,
    name varchar(64) not null,
    create_time date not null,
    primary key(id,create_time)
)partition by hash(year(create_time)) partitions 2(
    partition t0 data directory="/mnt/test_data/mysql_data1",
    partition t1 data directory="/mnt/test_data/mysql_data2"
);

# 查看分区表信息，以及数据信息
$ select * from information_schema.`partitions` where table_schema=schema() and table_name='test_hash_2';
```

##### 6.4、Key分区是根据某个字段（一定要是主键字段）的值求模进行分区，如果不指明字段默认是使用主键字段（注意：字段值的类型可以是任意的，所以这个可以看成是Hash分区的增强版）
```bash
# 创建表并使用name的值与2求模的值进行分区（注意：与N求模，就要配置N个分区，以供数据存储），分区名字都可以顺便起，这种方式推荐生产使用（注意：因为只能根据主键字段分区，所以作成了复合主键）
$ create table test_key_1(
    id int not null,
    name varchar(64) not null,
    create_time date not null,
    primary key(id,name)
)partition by key(name) partitions 2(
    partition t0 data directory="/mnt/test_data/mysql_data1",
    partition t1 data directory="/mnt/test_data/mysql_data2"
);

# 创建表，因为我们没有指定分区字段所以默认是使用主键的值与2求模的值进行分区（注意：与N求模，就要配置N个分区，以供数据存储），分区名字都可以顺便起，这种方式推荐生产使用
$ create table test_key_2(
    id int not null primary key,
    name varchar(64) not null,
    create_time date not null
)partition by key() partitions 2(
    partition t0 data directory="/mnt/test_data/mysql_data1",
    partition t1 data directory="/mnt/test_data/mysql_data2"
);

# 查看分区表信息，以及数据信息
$ select * from information_schema.`partitions` where table_schema=schema() and table_name='test_key_1';
```

##### 6.5、子分区是在已有的分区上再建立分区，目前只有Range和List分区上可建立子分区，而且子分区只能是Hash或Key分区
```bash
# 创建表并使用ID的值的范围进行分区，1-1000的id在t0分区，1001-2000的id在t1分区，大于2000的id在t2分区，分区名字都可以顺便起。
# t0，t1，t2分区里面的数据，再通过name的值与4取模进行分区（也就是每个分区里面有4个子分区）注意：每个分区里面的子分区个数要相同，否则会报错。
$ create table test_child_2(
    id int not null,
    name varchar(64) not null,
    create_time date not null,
    primary key(id,name)
)partition by range(id) 
 subpartition by key(name) subpartition 4(
    partition t0 values less than(1000) data directory="/mnt/test_data/mysql_data1",
    partition t1 values less than(2000) data directory="/mnt/test_data/mysql_data2",
    partition t2 values less than maxvalue data directory="/mnt/test_data/mysql_data3"
);


# 创建表并使用ID的值的范围进行分区，1-1000的id在t0分区，1001-2000的id在t1分区，大于2000的id在t2分区，分区名字都可以顺便起。
# t0，t1，t2每个分区里面，还有通过key(name)分区方式的2个分区（也就是每个分区里面有2个子分区）注意：每个分区里面的子分区个数要相同，否则会报错。
$ create table test_child_2(
    id int not null,
    name varchar(64) not null,
    create_time date not null,
    primary key(id,name)
)partition by range(id) 
 subpartition by key(name)(
    partition t0 values less than(1000) data directory="/mnt/test_data/mysql_data1" (subpartition s0,subpartition s1),
    partition t1 values less than(2000) data directory="/mnt/test_data/mysql_data2" (subpartition q0,subpartition q1),
    partition t2 values less than maxvalue data directory="/mnt/test_data/mysql_data3 (subpartition w2,subpartition w1)"
);
```

#### 七、管理表分区（注意：添加和创建新的分区MySQL会自动迁移数据，删除分区MySQL会删除分区里面的数据）
```bash
# 为 Range 分区方式的test_range_1表添加新的分区t3
$ alter table test_range_1 add partition(
      partition t3 values less than(3000) data directory="/mnt/test_data/mysql_data2"
);

# 为 List 分区方式的test_list_1表添加新的分区t3
$ alter table test_list_1 add partition(
      partition t3 values in(7,8) data directory="/mnt/test_data/mysql_data3"
);

# 为 Hash 分区方式的test_hash_1表添加新的分区t3
$ alter table test_hash_1 add partition(
      partition t3 data directory="/mnt/test_data/mysql_data3"
);

# 为 Key 分区方式的test_key_1表添加新的分区t3
$ alter table test_hash_1 add partition(
      partition t3 data directory="/mnt/test_data/mysql_data3"
);

# 为没有分区规则的test_test_1表创建Range方式分区（注意：语法和建表时，所使用的分区定义语法相同）
$ alter table test_test_1 partition by range(id)(
      partition t1 values less than(1000) data directory="/mnt/test_data/mysql_data2",
      partition t2 values less than(2000) data directory="/mnt/test_data/mysql_data3"
);

# 为没有分区规则的test_test_1表创建List方式分区（注意：语法和建表时，所使用的分区定义语法相同）
$ alter table test_test_1 partition by list(category_id)(
    partition t0 values in(1,2) data directory="/mnt/test_data/mysql_data1",
    partition t1 values in(4,5) data directory="/mnt/test_data/mysql_data2"
);

# 为没有分区规则的test_test_1表创建Hash方式分区（注意：语法和建表时，所使用的分区定义语法相同）
$ alter table test_test_1 partition by hash(id) partitions 2(
    partition t0 data directory="/mnt/test_data/mysql_data1",
    partition t1 data directory="/mnt/test_data/mysql_data2"
);

# 为没有分区规则的test_test_1表创建Key方式分区（注意：语法和建表时，所使用的分区定义语法相同）
$ alter table test_test_1 partition by key(id) partitions 2(
    partition t0 data directory="/mnt/test_data/mysql_data1",
    partition t1 data directory="/mnt/test_data/mysql_data2"
);

# 拆分Range 分区方式的test_range_1表的t0分区（以下是将t0分区，拆分成c1和c2两个分区（注意：MySQL会自动迁移数据） ）
$ alter table test_range_1 peorganize partition t0 into(
      partition c1 values less than(500) data directory="/mnt/test_data/mysql_data2",
      partition c2 values less than(1000) data directory="/mnt/test_data/mysql_data3"
);

# 拆分Range 分区方式的test_range_1表里面带有子分区的t0分区（以下是将t0分区，拆分成c1和c2两个分区（注意：MySQL会自动迁移数据） ）
# 注意：最后的参数是子分区的描述，每个分区里面的子分区个数要相同，否则会报错
$ alter table test_range_1 peorganize partition t0 into(
      partition c1 values less than(500) data directory="/mnt/test_data/mysql_data2" (subpartition s0,subpartition s1),
      partition c2 values less than(1000) data directory="/mnt/test_data/mysql_data3" (subpartition d0,subpartition d1)
);

# 拆分List 分区方式的test_list_1表的t0分区（以下是将t0分区，拆分成c1和c2两个分区（注意：MySQL会自动迁移数据） ）
$ alter table test_list_1 peorganize partition t0 into(
      partition c1 values in(1,2) data directory="/mnt/test_data/mysql_data1",
      partition c2 values in(3,4) data directory="/mnt/test_data/mysql_data2"
);

# 拆分List 分区方式的test_list_1表里面带有子分区的t0分区（以下是将t0分区，拆分成c1和c2两个分区（注意：MySQL会自动迁移数据） ）
# 注意：最后的参数是子分区的描述，每个分区里面的子分区个数要相同，否则会报错
$ alter table test_list_1 peorganize partition t0 into(
      partition c1 values in(1,2) data directory="/mnt/test_data/mysql_data1" (subpartition s0,subpartition s1),
      partition c2 values in(3,4) data directory="/mnt/test_data/mysql_data2" (subpartition d0,subpartition d1)
);

# 拆分Hash 分区方式的test_hash_1表的t0分区（以下是将t0分区，拆分成c1和c2两个分区（注意：MySQL会自动迁移数据） ）
$ alter table test_hash_1 peorganize partition t0 into(
      partition c1 data directory="/mnt/test_data/mysql_data1",
      partition c2 data directory="/mnt/test_data/mysql_data2"
);

# 拆分Key 分区方式的test_key_1表的t0分区（以下是将t0分区，拆分成c1和c2两个分区（注意：MySQL会自动迁移数据） ）
$ alter table test_key_1 peorganize partition t0 into(
      partition c1 data directory="/mnt/test_data/mysql_data1",
      partition c2 data directory="/mnt/test_data/mysql_data2"
);

# 合并Range 分区方式的test_range_1表的c1和c2分区（以下是将c1和c2两个分区合并成t0分区（注意：MySQL会自动迁移数据） ）
$ alter table test_range_1 peorganize partition c1,c2 into(
      partition t0 values less than(1000) data directory="/mnt/test_data/mysql_data2"
);

# 合并Range 分区方式的test_range_1表里面带有子分区的c1和c2分区（以下是将c1和c2两个分区合并成t0分区（注意：MySQL会自动迁移数据） ）
# 注意：最后的参数是子分区的描述，每个分区里面的子分区个数要相同，否则会报错
$ alter table test_range_1 peorganize partition c1,c2 into(
      partition t0 values less than(1000) data directory="/mnt/test_data/mysql_data2" (subpartition s0,subpartition s1)
);

# 合并List 分区方式的test_list_1表的c1和c2分区（以下是将c1和c2两个分区合并成t0分区（注意：MySQL会自动迁移数据） ）
$ alter table test_list_1 peorganize partition c1,c2 into(
      partition t0 values in(1,2,3,4) data directory="/mnt/test_data/mysql_data1"
);

# 合并List 分区方式的test_list_1表里面带有子分区的c1和c2分区（以下是将c1和c2两个分区合并成t0分区（注意：MySQL会自动迁移数据） ）
# 注意：最后的参数是子分区的描述，每个分区里面的子分区个数要相同，否则会报错
$ alter table test_list_1 peorganize partition c1,c2 into(
      partition t0 values in(1,2,3,4) data directory="/mnt/test_data/mysql_data1" (subpartition s0,subpartition s1)
);

# 合并Hash 分区方式的test_hash_1表的c1和c2分区（以下是将c1和c2两个分区合并成t0分区（注意：MySQL会自动迁移数据） ）
$ alter table test_hash_1 peorganize partition c1,c2 into(
      partition t0 data directory="/mnt/test_data/mysql_data1"
);

# 合并Key 分区方式的test_key_1表的c1和c2分区（以下是将c1和c2两个分区合并成t0分区（注意：MySQL会自动迁移数据） ）
$ alter table test_hash_1 peorganize partition c1,c2 into(
      partition t0 data directory="/mnt/test_data/mysql_data1"
);

# 删除test_range_1表t0和t1分区（注意：删除表分区会默认删除分区里面的数据，而且最后一个表分区是不能删除的）
$ alter table test_range_1 drop partition t0,t1;

# 移除test_range_1表分区定义（注意：移除表分区定义是不会删除数据的，只是删除表分区定义（就是没有了表分区））
$ alter table test_range_1 remove partitioning;
```