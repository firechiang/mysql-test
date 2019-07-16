#### 一、Tpcc-MySQL所模拟的业务如下图所示
![object](https://github.com/firechiang/mysql-test/blob/master/image/tpcc.svg)
#### 二、下载源码包（建议使用下载地址：https://github.com/firechiang/mysql-test/raw/master/data/tpcc-mysql-master.tar.gz）
```bash
$ wget -P /home/tools https://github.com/Percona-Lab/tpcc-mysql/archive/master.zip
```

#### 三、编译安装
```bash
$ yum -y remove mari*                                               # 卸载 MariaDB 所有依赖包
$ yum -y install gcc mysql-devel                                    # 安装编译依赖
```

#### 四、编译源码（注意：编译好的文件，可以直接拷贝到其它机器运行）
```bash
$ cd /home/tools
$ tar -zxvf tpcc-mysql-master.tar.gz -C ./                          # 解压到当前目录
$ cd tpcc-mysql-master/src                                          # 进入源码目录
$ make                                                              # 编译源码
```

#### 五、修改[vi /etc/my.cnf] Percona-Server 的SQL执行模式（注意：要重启MySQL才会生效。如果不是Percona-Server就不用修改。如果是Percona-Server集群，每个节点都要修改）
```bash
pxc_strict_mode=DISABLED                                            # 禁用严格模式，可以有没有主键的表
```

#### 六、建立测试库
```bash
$ mysql -h127.0.0.1 -P 3306 -uroot -p                               # 进入MySQL
$ create database tpcc;                                             # 创建数据库 tpcc
```

#### 七、建立测试表（注意：建表语句其实是tpcc-mysql-master.tar.gz解压目录里面的create_table.sql文件内容）
```bash
# 进入tpcc库
$ use tpcc      

# 建立如下数据表       
                                             
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

drop table if exists warehouse;

create table warehouse (
    w_id smallint not null,
    w_name varchar(10), 
    w_street_1 varchar(20), 
	w_street_2 varchar(20), 
	w_city varchar(20), 
	w_state char(2), 
	w_zip char(9), 
	w_tax decimal(4,2), 
	w_ytd decimal(12,2),
    primary key (w_id) 
) Engine=InnoDB;

drop table if exists district;

create table district (
	d_id tinyint not null, 
	d_w_id smallint not null, 
	d_name varchar(10), 
	d_street_1 varchar(20), 
	d_street_2 varchar(20), 
	d_city varchar(20), 
	d_state char(2), 
	d_zip char(9), 
	d_tax decimal(4,2), 
	d_ytd decimal(12,2), 
	d_next_o_id int,
	primary key (d_w_id, d_id)
) Engine=InnoDB;

drop table if exists customer;

create table customer (
	c_id int not null, 
	c_d_id tinyint not null,
	c_w_id smallint not null, 
	c_first varchar(16), 
	c_middle char(2), 
	c_last varchar(16), 
	c_street_1 varchar(20), 
	c_street_2 varchar(20), 
	c_city varchar(20), 
	c_state char(2), 
	c_zip char(9), 
	c_phone char(16), 
	c_since datetime, 
	c_credit char(2), 
	c_credit_lim bigint, 
	c_discount decimal(4,2), 
	c_balance decimal(12,2), 
	c_ytd_payment decimal(12,2), 
	c_payment_cnt smallint, 
	c_delivery_cnt smallint, 
	c_data text,
	PRIMARY KEY(c_w_id, c_d_id, c_id) 
) Engine=InnoDB;

drop table if exists history;

create table history (
	h_c_id int, 
	h_c_d_id tinyint, 
	h_c_w_id smallint,
	h_d_id tinyint,
	h_w_id smallint,
	h_date datetime,
	h_amount decimal(6,2), 
	h_data varchar(24) 
) Engine=InnoDB;

drop table if exists new_orders;

create table new_orders (
	no_o_id int not null,
	no_d_id tinyint not null,
	no_w_id smallint not null,
	PRIMARY KEY(no_w_id, no_d_id, no_o_id)
) Engine=InnoDB;

drop table if exists orders;

create table orders (
	o_id int not null, 
	o_d_id tinyint not null, 
	o_w_id smallint not null,
	o_c_id int,
	o_entry_d datetime,
	o_carrier_id tinyint,
	o_ol_cnt tinyint, 
	o_all_local tinyint,
	PRIMARY KEY(o_w_id, o_d_id, o_id) 
) Engine=InnoDB ;

drop table if exists order_line;

create table order_line ( 
	ol_o_id int not null, 
	ol_d_id tinyint not null,
	ol_w_id smallint not null,
	ol_number tinyint not null,
	ol_i_id int, 
	ol_supply_w_id smallint,
	ol_delivery_d datetime, 
	ol_quantity tinyint, 
	ol_amount decimal(6,2), 
	ol_dist_info char(24),
	PRIMARY KEY(ol_w_id, ol_d_id, ol_o_id, ol_number) 
) Engine=InnoDB ;

drop table if exists item;

create table item (
	i_id int not null, 
	i_im_id int, 
	i_name varchar(24), 
	i_price decimal(5,2), 
	i_data varchar(50),
	PRIMARY KEY(i_id) 
) Engine=InnoDB;

drop table if exists stock;

create table stock (
	s_i_id int not null, 
	s_w_id smallint not null, 
	s_quantity smallint, 
	s_dist_01 char(24), 
	s_dist_02 char(24),
	s_dist_03 char(24),
	s_dist_04 char(24), 
	s_dist_05 char(24), 
	s_dist_06 char(24), 
	s_dist_07 char(24), 
	s_dist_08 char(24), 
	s_dist_09 char(24), 
	s_dist_10 char(24), 
	s_ytd decimal(8,0), 
	s_order_cnt smallint, 
	s_remote_cnt smallint,
	s_data varchar(50),
	PRIMARY KEY(s_w_id, s_i_id) 
) Engine=InnoDB ;

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
```

#### 八、建立外键约束和索引（注意：SQL语句其实是tpcc-mysql-master.tar.gz解压目录里面的add_fkey_idx.sql文件内容）
```bash
# 进入tpcc库
$ use tpcc      

# 建立如下外键约束和索引 

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;


CREATE INDEX idx_customer ON customer (c_w_id,c_d_id,c_last,c_first);
CREATE INDEX idx_orders ON orders (o_w_id,o_d_id,o_c_id,o_id);
CREATE INDEX fkey_stock_2 ON stock (s_i_id);
CREATE INDEX fkey_order_line_2 ON order_line (ol_supply_w_id,ol_i_id);

ALTER TABLE district  ADD CONSTRAINT fkey_district_1 FOREIGN KEY(d_w_id) REFERENCES warehouse(w_id);
ALTER TABLE customer ADD CONSTRAINT fkey_customer_1 FOREIGN KEY(c_w_id,c_d_id) REFERENCES district(d_w_id,d_id);
ALTER TABLE history  ADD CONSTRAINT fkey_history_1 FOREIGN KEY(h_c_w_id,h_c_d_id,h_c_id) REFERENCES customer(c_w_id,c_d_id,c_id);
ALTER TABLE history  ADD CONSTRAINT fkey_history_2 FOREIGN KEY(h_w_id,h_d_id) REFERENCES district(d_w_id,d_id);
ALTER TABLE new_orders ADD CONSTRAINT fkey_new_orders_1 FOREIGN KEY(no_w_id,no_d_id,no_o_id) REFERENCES orders(o_w_id,o_d_id,o_id);
ALTER TABLE orders ADD CONSTRAINT fkey_orders_1 FOREIGN KEY(o_w_id,o_d_id,o_c_id) REFERENCES customer(c_w_id,c_d_id,c_id);
ALTER TABLE order_line ADD CONSTRAINT fkey_order_line_1 FOREIGN KEY(ol_w_id,ol_d_id,ol_o_id) REFERENCES orders(o_w_id,o_d_id,o_id);
ALTER TABLE order_line ADD CONSTRAINT fkey_order_line_2 FOREIGN KEY(ol_supply_w_id,ol_i_id) REFERENCES stock(s_w_id,s_i_id);
ALTER TABLE stock ADD CONSTRAINT fkey_stock_1 FOREIGN KEY(s_w_id) REFERENCES warehouse(w_id);
ALTER TABLE stock ADD CONSTRAINT fkey_stock_2 FOREIGN KEY(s_i_id) REFERENCES item(i_id);



SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
```

#### 九、生成测试数据（注意：建立的仓库越多，数据量就越大，生成速度就越慢）
```bash
$ cd /home/tools/tpcc-mysql-master                            # 进入tpcc-mysql解压目录
$ ./tpcc_load -h 192.168.0.120 -d tpcc -u root -p jiang -w 1  # -w是指建立多少个仓库的数据（生产测试建议 >= 1000），-d是指数据库名称
```