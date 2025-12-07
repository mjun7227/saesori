package com.Saesori.util;

import java.security.MessageDigest;

public class PasswordUtil {
	// SHA-256 단방향 암호화 메소드
	public static String hashPassword(String pwd) {
		String hashed = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(pwd.getBytes()); 
			byte[] bytes = md.digest();  // 암호화 
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02x", b)); // 1 byte --> 16진수 문자 변환
			}
			hashed = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hashed;
	}
	public static boolean checkPassword(String plainPassword, String hashedPassword) {
		return hashPassword(plainPassword).equals(hashedPassword);
	}
}




