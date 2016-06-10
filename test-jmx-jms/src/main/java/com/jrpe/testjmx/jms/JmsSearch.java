package com.jrpe.testjmx.jms;

import com.jrpe.testjmx.jms.JMSMonitorTarget;
import com.jrpe.testjmx.jms.JMSPropertyHandler;
import com.jrpe.testjmx.jms.JMXHelper;
import weblogic.management.ManagementException;
import weblogic.management.runtime.JMSDestinationRuntimeMBean;
import weblogic.management.runtime.SAFRemoteEndpointRuntimeMBean;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class JmsSearch {
	private final static List<String> searchFor = Arrays.asList("Test1");

	private final static String PROPERTIES_FILE = "com/jrpe/testjmx/jms/jms-search.properties";

	private final static String JMS_ALL_SELECTOR = "JMSTimestamp < " + (System.currentTimeMillis());
	private final static String generatedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

	public static void main(String[] args) {
		try {
			System.out.println("### START JMS MONITOR " + generatedTime);
			Properties props = JMSPropertyHandler.loadProperties(PROPERTIES_FILE);

			List<JMSMonitorTarget> jmsMonitorTargets = JMSPropertyHandler.getJMSMonitorTargetProperties(props);
			if(jmsMonitorTargets.size() > 0) {
				for (JMSMonitorTarget target:jmsMonitorTargets) {
					System.out.println("Set target information for " +target.getDesc());
					searchTargets(target);
					System.out.println("Set target information for " +target.getDesc()+" finished");
				}
			} else {
				throw new MonitorException("No monitoring targets found in the property file");
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			System.out.println("### END JMS MONITOR " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
		}
	}

	private JmsSearch() {
	}

	public static void searchTargets(JMSMonitorTarget target) {
		try (JMXHelper jmxHelper = new JMXHelper()) {
			jmxHelper.open(target.getHostname(), target.getPort(), target.getUsername(), target.getPassword());
			for(JMSMonitorTarget.QueueInformation queueInfo : target.getQueueList()) {
				System.out.println("Set information for queue" +queueInfo.getQueue());
				if(target.getSafAgent() != null) {
					SAFRemoteEndpointRuntimeMBean safRemoteEndpoint = jmxHelper.getSAFRemoteEndpoint(target.getServer(), target.getSafAgent(), queueInfo.getQueue());
					getMessages(safRemoteEndpoint);
				} else {
					JMSDestinationRuntimeMBean jmsDestination = jmxHelper.getJMSDestination(target.getServer(), target.getJmsServer(), queueInfo.getQueue());
					getMessages(jmsDestination);
				}
			}
		} catch (IOException e) {
			throw new MonitorException("Cannot open JMX connection", e);
		}
	}
	private static void getMessages(SAFRemoteEndpointRuntimeMBean jmsDestination) {

	}

	private static void getMessages(JMSDestinationRuntimeMBean jmsDestination) {
		try {
			CompositeData[] messageInfos = null;
			String cursor = jmsDestination.getMessages(JMS_ALL_SELECTOR, 1000);

			while ((messageInfos = jmsDestination.getNext(cursor,new Integer(20))) != null) {
				for (int i = 0; i < messageInfos.length; i++) {
					CompositeData messageInfo = messageInfos[i];
					Long handle = (Long)messageInfo.get("Handle");
					CompositeData m = jmsDestination.getMessage(cursor, handle);
					String content = m.toString();
					//System.out.println(content);

					for(String searchString:searchFor) {
						if(content.contains(searchString)) {
							String JMSCorrelationID = content.substring(content.indexOf("<mes:JMSCorrelationID>")+23, content.lastIndexOf("</mes:JMSCorrelationID>"));
							System.out.println(JMSCorrelationID);
						}
					}
				}
			}
		} catch (ManagementException ex) {
			throw new MonitorException(ex.getMessage(), ex);
		}
	}
}
