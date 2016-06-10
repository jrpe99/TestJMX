package com.jrpe.testjmx.jms;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class PropertyHandler {
    public final static String MAILSMTPHOST = "jms.mail.smtp.host";
    public final static String MAILFROM = "jms.mail.from";
    public final static String MAILTO = "jms.mail.to";
    public final static String MAILENVIRONMENT = "jms.mail.environment";

    /**
     * Loads properties file and creates a Properties object from it.
     * @return Properties object containing the loaded properties.
     */
    protected static Properties loadProperties(String propertyFile) {
        Properties props = new Properties();

        Reader reader = null;
        try {
            reader = new FileReader(propertyFile);
            props.load(reader);
        } catch (IOException ex) {
            throw new MonitorException(ex.getMessage(), ex);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }

        return props;
    }

    /**
     * Validates that all properties have been loaded.
     * @param props properties object that need to be validated.
     * @param intValidate name of items that need to be validated as an integer.
     * @param args list of values that need to be validated.
     */
    protected static void validateProperties(String propertyFile, Properties props, String[] intValidate, String... args) {
        ArrayList<String> keys = new ArrayList<String>(Arrays.asList(args));

        for(final String key:keys) {
            if(!props.containsKey(key) || props.getProperty(key) == null) {
                throw new MonitorException(propertyFile + " is missing property: " + key);
            }
        }

        for(final String intName:intValidate) {
            try {
                Integer.parseInt(props.getProperty(intName));
            } catch (NumberFormatException ex) {
                throw new MonitorException("The '" + intName + "' property in '" + propertyFile + "' is not a int.", ex);
            }
        }
    }
}
