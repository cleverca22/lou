package com.angeldsis.lounative;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Config {
	private static Config self;
	Properties props;
	private Config() throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
		File config = getConfigFile();
		File dir = config.getParentFile();
		if (!dir.exists()) dir.mkdir();
		if (config.exists()) {
			try {
				props.loadFromXML(new FileInputStream(config));
			} catch (Exception e) {
				if (e instanceof SAXParseException) {
					config.delete();
					props = new Properties();
				}
			}
		}
	}
	static void init() throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
		Config.self = new Config();
	}
	static Config getConfig() {
		return self;
	}
	String getUsername() {
		return props.getProperty("username","");
	}
	String getPassword() {
		return props.getProperty("password","");
	}
	public void setUsername(String username) {
		props.setProperty("username", username);
	}
	public void setPassword(String password) {
		props.setProperty("password", password);
	}
	private File getConfigFile() {
		Properties p = new Properties(System.getProperties());
		String home = p.getProperty("user.home");
		props = new Properties();
		return new File(home+"/lou-native/config.xml");
	}
	public void flush() {
		try {
			props.storeToXML(new FileOutputStream(getConfigFile()), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void clearCredentials() {
		props.remove("username");
		props.remove("password");
	}
	public void setRememberMe(String cookie) {
		props.setProperty("remember_me", cookie);
	}
	public String getRememberMe() {
		return props.getProperty("remember_me", null);
	}
}
