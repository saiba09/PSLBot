package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertyLoader {
	private static Properties Festival;
	private static Properties emailProperty;
	private static Properties apiaiProperty;
	private static Properties FAQProperty;

	private static final Logger log = Logger.getLogger(PropertyLoader.class
			.getName());
	static {
		log.info("inside get property method");
		InputStream queryPropertyStream = getPropertyStream("Festivals.property");
		if (queryPropertyStream == null) {
			log.severe("input stream null");
			throw new RuntimeException("Unable to load property file: Festivals.property");
		}
		/*InputStream emailPropertyStream = getPropertyStream(EYConstants.EMAIL_PROPERTY);
		if (emailPropertyStream == null) {
			log.severe("input stream null");
			throw new RuntimeException("Unable to load property file: "
					+ EYConstants.EMAIL_PROPERTY);
		}
		InputStream apiapiPropertyStream = getPropertyStream(EYConstants.API_AI_PROPERTY);
		if (apiapiPropertyStream == null) {
			log.severe("input stream null");
			throw new RuntimeException("Unable to load property file: "
					+ EYConstants.API_AI_PROPERTY);
		}
		InputStream FAQPropertyStream = getPropertyStream(EYConstants.FAQ_PROPERTY);
		if (FAQPropertyStream == null) {
			log.severe("input stream null");
			throw new RuntimeException("Unable to load property file: "
					+ EYConstants.FAQ_PROPERTY);
		}*/
		// singleton
		if (Festival == null) {
			Festival = new Properties();
			try {
				Festival.load(queryPropertyStream);
			} catch (IOException e) {
				throw new RuntimeException("Unable to load property file: "
						+ "Festivals.property" + "\n" + e.getMessage());
			} catch (Exception e1) {
				log.info("error  : " + e1);
			}
		}/*
		if (emailProperty == null) {
			emailProperty = new Properties();
			try {
				emailProperty.load(emailPropertyStream);
			} catch (IOException e) {
				throw new RuntimeException("Unable to load property file: "
						+ EYConstants.EMAIL_PROPERTY + "\n" + e.getMessage());
			} catch (Exception e1) {
				log.info("error  : " + e1);
			}
		}
		if (apiaiProperty == null) {
			apiaiProperty = new Properties();
			try {
				apiaiProperty.load(apiapiPropertyStream);
			} catch (IOException e) {
				throw new RuntimeException("Unable to load property file: "
						+ EYConstants.API_AI_PROPERTY + "\n" + e.getMessage());
			} catch (Exception e1) {
				log.info("error  : " + e1);
			}
		}
		if (FAQProperty == null) {
			FAQProperty = new Properties();
			try {
				FAQProperty.load(FAQPropertyStream);
			} catch (IOException e) {
				throw new RuntimeException("Unable to load property file: "
						+ EYConstants.FAQ_PROPERTY + "\n" + e.getMessage());
			} catch (Exception e1) {
				log.info("error  : " + e1);
			}
		}*/
	}

	private static InputStream getPropertyStream(String propertyFileName) {
		InputStream inputStream = PropertyLoader.class.getResourceAsStream("/"
				+ propertyFileName);
		return inputStream;
	}

	private static Properties getFestivals() {
		return Festival;
	}

	/*private static Properties getEmailProperty() {
		return emailProperty;
	}

	private static Properties getApiaiProperty() {
		return apiaiProperty;
	}

	private static Properties getFAQProperty() {
		return FAQProperty;
	}*/

	public static String getList(String query) throws SQLException {
		log.info("get querries");
		return getFestivals().getProperty(query);
	}

	/*public static String getApiProperty(String query) throws SQLException {
		log.info("get API AI property");
		return getApiaiProperty().getProperty(query);

	}

	public static String getEmailProperty(String query) throws SQLException {
		log.info("get email property");
		return getEmailProperty().getProperty(query);

	}

	public static String getFAQProperty(String query) throws SQLException {
		log.info("get FAQ property");
		return getFAQProperty().getProperty(query);

	}*/
}
