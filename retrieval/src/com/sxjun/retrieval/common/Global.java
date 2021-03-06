/**
 * Copyright &copy; 2012-2013 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.sxjun.retrieval.common;

import java.util.Properties;



/**
 * 全局配置类
 * @author ThinkGem
 * @version 2013-03-23
 */
public class Global {
	public static Integer pageSize;
	
	public static String adminPath;
	public static String frontPath;
	public static String urlSuffix;
	
	public static Properties properties;
	
	public static String getAdminPath() {
		return adminPath;
	}
	public static String getFrontPath() {
		return frontPath;
	}
	public static String getUrlSuffix() {
		return urlSuffix;
	}
	/**
	 * 获取配置
	 */
	public static String getConfig(String key) {
		return properties.getProperty(key);
	}
}
