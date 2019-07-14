package com.firecode.mysqltest.mycat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.mysql.jdbc.Driver;

/**
 * 测试Mycat根据指定列的值，进行数据切片
 * 建表语句
 * create table m_dept(id int(11) PRIMARY KEY,sharding_id int(11) NOT NULL,name char(20) NOT NULL)ENGINE=InnoDB DEFAULT CHARSET=utf8;
 * @author JIANG
 */
public class MycatCustomInsertTest {
	
	/**
	 * 注意先建表
	 * @param args
	 * @throws SQLException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws SQLException, InterruptedException {
		DriverManager.registerDriver(new Driver());
		String url = "jdbc:mysql://192.168.229.132:8066/test_test";
		String username = "test_admin";
		String password = "Jiang@123";
		Connection connection = DriverManager.getConnection(url,username,password);
		String sql = "insert into m_dept(id,sharding_id,name) values(?,?,?)";
		PreparedStatement pst = connection.prepareStatement(sql);
			for (int i = 1; i < 10; i++) {
				pst.setInt(1, i);
				pst.setInt(2, i%2==0?1000:2000);
				pst.setString(3, "www"+i);
				pst.execute();
			}
			TimeUnit.SECONDS.sleep(1);
	}
}
