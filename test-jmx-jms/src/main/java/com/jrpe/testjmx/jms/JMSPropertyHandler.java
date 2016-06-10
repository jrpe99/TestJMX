package com.jrpe.testjmx.jms;

import com.jrpe.testjmx.jms.MonitorException;
import com.jrpe.testjmx.jms.PropertyHandler;

import java.util.*;

public class JMSPropertyHandler extends PropertyHandler {

    public final static String TARGET_IDENTIFIER = "jms.target.";

    public static List<JMSMonitorTarget> getJMSMonitorTargetProperties(Properties props) {
        Map<String, JMSMonitorTarget> environments = new HashMap<>();

        Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = (String)props.get(key);

            if(key.startsWith(TARGET_IDENTIFIER)) {
                String targetProperty = key.substring(TARGET_IDENTIFIER.length(), key.length());
                String targetKey = targetProperty.substring(0, targetProperty.indexOf("."));
                String property = targetProperty.substring(targetProperty.indexOf(".")+1, targetProperty.length());

                if(environments.containsKey(targetKey)) {
                    environments.get(targetKey).setProperty(property, value);
                } else {
                    JMSMonitorTarget target = new JMSMonitorTarget();

                    target.setProperty(property, value);
                    environments.put(targetKey, target);
                }
            }
        }

        ArrayList<JMSMonitorTarget> jmsMonitorTargets = new ArrayList<>(environments.values());
        for (JMSMonitorTarget target : new ArrayList<>(environments.values())) {
            if(!target.isValid()) {
                throw new MonitorException("Missing configuration for target " + target.getDesc() + " : " + target.getMissing());
            }
        }
        return jmsMonitorTargets;
    }
}
