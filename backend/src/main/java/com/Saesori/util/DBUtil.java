//DBUtill.java
package com.Saesori.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBUtil {
	private static Properties props = new Properties();

	// 2. 정적 초기화 블록: 클래스가 메모리에 로드될 때 .properties 파일을 읽어옴
	static {
		// 파일 경로 설정 (클래스패스에서 파일을 찾음)
		String resource = "db.properties";

		try (InputStream is = DBUtil.class.getClassLoader().getResourceAsStream(resource)) {
			// 파일을 찾지 못하면 예외 발생
			if (is == null) {
				throw new Exception("Error loading properties file: " + resource);
			}
			props.load(is); // InputStream으로부터 Properties 파일 로드

			// 드라이버 로딩 (Properties 파일에서 읽은 값을 사용)
			Class.forName(props.getProperty("db.driver"));

		} catch (Exception e) {
			e.printStackTrace();
			// 로딩 실패 시 애플리케이션 실행을 중단하거나 적절히 처리해야 함
			throw new RuntimeException("DBUtil Initialization Failed", e);
		}
	}

	// DB 접속
	public static Connection getConnection() {
		Connection conn = null;

		// Properties 객체에서 URL, USER, PASS를 읽어와 사용
		String url = props.getProperty("db.url");
		String user = props.getProperty("db.user");
		String pass = props.getProperty("db.password");

		try {
			conn = DriverManager.getConnection(url, user, pass);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;
	}

	// DB 접속 해제
	public static void close(AutoCloseable... acs) {
		for (AutoCloseable ac : acs) {
			try {
				if (ac != null)
					ac.close(); // Connection, PreparedStatement, ResultSet 등 자원 해제
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}