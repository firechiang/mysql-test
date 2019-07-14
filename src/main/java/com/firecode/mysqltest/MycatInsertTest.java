package com.firecode.mysqltest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.mysql.jdbc.Driver;

/**
 * 测试数据插入
 * 建表语句
 * create table m_user(id int(11) PRIMARY KEY,name char(20) NOT NULL)ENGINE=InnoDB DEFAULT CHARSET=utf8;
 * @author JIANG
 */
public class MycatInsertTest {
	
	/**
	 * 注意先建表
	 * @param args
	 * @throws SQLException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws SQLException, InterruptedException {
		DriverManager.registerDriver(new Driver());
		String url = "jdbc:mysql://192.168.229.133:3306/test_test";
		String username = "admin";
		String password = "Jiang@123";
		Connection connection = DriverManager.getConnection(url,username,password);
		String sql = "insert into user(name) values(?)";
		PreparedStatement pst = connection.prepareStatement(sql);
		while(true){
			for (int i = 0; i < 100; i++) {
				pst.setString(1, "www"+i);
				pst.execute();
			}
			TimeUnit.SECONDS.sleep(1);
		}
	}
}
