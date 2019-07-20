#### 一、下载依赖安装包（注意：可以安装在没有MySQL的机器上）
```bash
$ wget -P /home/tools/pt-archiver https://repo.percona.com/release/7/RPMS/x86_64/percona-toolkit-3.0.13-1.el7.x86_64.rpm
$ wget -P /home/tools/pt-archiver https://repo.percona.com/release/7/RPMS/x86_64/percona-toolkit-debuginfo-3.0.13-1.el7.x86_64.rpm
```

#### 二、安装依赖包（注意：可以安装在没有MySQL的机器上）
```bash
$ cd /home/tools/pt-archiver
$ yum localinstall *.rpm              # 安装所有安装包
```

#### 三、修改[vi /usr/bin/pt-archiver]脚本，解决不会迁移max(id)那条数据的问题
```bash
# 修改前 $first_sql .= " AND ($col < " . $q->quote_val($val) . ")";
# 修改后如下（注意：其实就是家里一个等号，可使用$col <搜索到要修改的位置）
$first_sql .= " AND ($col <= " . $q->quote_val($val) . ")";
```

#### 四、验证 PT-Archiver 是否安装成功
```bash
$ pt-archiver --version               # 查看 PT-Archiver 版本
$ pt-archiver --help                  # 查看 PT-Archiver 使用帮助
```

#### 五、PT-Archiver 简单使用（注意：\反斜杠表示命令换行）
```bash
# --source           源数据库描述信息（h=主机名或IP，P=端口，p=密码，D=数据库名，t=数据表名称）
# --dest             目标库描述信息（h=主机名或IP，P=端口，p=密码，D=数据库名，t=数据表名称）
# --where            源数据表过滤条件
# --limit            每次迁移N条数据，直到迁移完成为止
# --no-check-charset 迁移过程中不校验字符集
# --progress         每迁移N条数据往控制台打印状态信息
# --bulk-insert      批量写入
# --bulk-delete      迁移完成后批量删除源数据（注意：迁移和删除在同一个事务里完成）
# --statistics       最后打印迁移的统计信息
$ pt-archiver --source h=server001,P=3306,u=root,p=Jiang@123,D=test_test,t=test_user \
              --dest h=server002,P=3306,u=root,p=Jiang@123,D=test_test,t=test_user   \
              --where "create_time>'2019-07-19'"                                     \
              --limit=10000                                                          \
              --no-check-charset                                                     \
              --progress 5000                                                        \
              --bulk-insert                                                          \
              --bulk-delete                                                          \
              --statistics
             
```