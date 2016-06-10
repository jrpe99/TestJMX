package com.jrpe.testjmx.jms;

import dk.skat.lur.monitor.MonitorException;
import weblogic.management.ManagementException;
import weblogic.management.jmx.MBeanServerInvocationHandler;
import weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean;
import weblogic.management.runtime.*;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;

public class JMXHelper implements AutoCloseable {
    private ObjectName service = null;
    private MBeanServerConnection connection;
    private JMXConnector connector;
    private DomainRuntimeServiceMBean domainRuntimeServiceMBean;

    public SAFRemoteEndpointRuntimeMBean getSAFRemoteEndpoint(String serverRuntimeName, String safAgent, String queue) {
        ServerRuntimeMBean serverRuntime = getServerRuntimeMBeanFromDomainRuntime(this.domainRuntimeServiceMBean, serverRuntimeName);
        SAFAgentRuntimeMBean safAgentRuntime = getSAFServerRuntimeMBeanFromServerRuntime(serverRuntime, safAgent);
        return getJMSDestinationRuntimeMBeanFromSAFAgentRuntime(safAgentRuntime, queue);
    }

    public JMSDestinationRuntimeMBean getJMSDestination(String serverRuntimeName, String jmsServer, String queue) {
        ServerRuntimeMBean serverRuntime = getServerRuntimeMBeanFromDomainRuntime(domainRuntimeServiceMBean, serverRuntimeName);
        JMSServerRuntimeMBean jmsServerRuntime = getJMSServerRuntimeMBeanFromServerRuntime(serverRuntime, jmsServer);
        return getJMSDestinationRuntimeMBeanFromJMSServerRuntime(jmsServerRuntime, queue);
    }


    public void open(String hostName, int port, String username, String password) {
        try {
            System.out.println("Open JMX connection to: " + hostName);
            System.out.println("Host name: " + hostName);
            System.out.println("Port: " + port);
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            service = new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME);

            JMXServiceURL serviceURL = new JMXServiceURL("t3", hostName, Integer.valueOf(port), "/jndi/" + DomainRuntimeServiceMBean.MBEANSERVER_JNDI_NAME);

            Hashtable<String, String> h = new Hashtable<String, String>();
            h.put(Context.SECURITY_PRINCIPAL, username);
            h.put(Context.SECURITY_CREDENTIALS, password);
            h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
            System.out.println("Connecting ...");
            this.connector = JMXConnectorFactory.connect(serviceURL, h);
            this.connection = connector.getMBeanServerConnection();
            this.domainRuntimeServiceMBean = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.newProxyInstance(connection, service, DomainRuntimeServiceMBean.class, false);
            System.out.println("Connected");
            System.out.println("Open JMX connection finished");
        } catch (MalformedURLException ex) {
            throw new MonitorException(ex.getMessage(), ex);
        } catch (MalformedObjectNameException ex) {
            throw new MonitorException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new MonitorException(ex.getMessage(), ex);
        }
    }

    @Override
    public void close() throws IOException {
        if(connector != null) {
            connector.close();
        }
    }

    private SAFRemoteEndpointRuntimeMBean getJMSDestinationRuntimeMBeanFromSAFAgentRuntime(SAFAgentRuntimeMBean safAgnetRuntime, String queue) {
        SAFRemoteEndpointRuntimeMBean[] remoteEndpoints = safAgnetRuntime.getRemoteEndpoints();
        for(SAFRemoteEndpointRuntimeMBean remoteEndpoint:remoteEndpoints) {
            if(remoteEndpoint.getName().contains(queue)) {
                return remoteEndpoint;
            }
        }

        throw new MonitorException("Unable to find queue: " + queue);
    }

    private ServerRuntimeMBean getServerRuntimeMBeanFromDomainRuntime(DomainRuntimeServiceMBean domainRuntimeServiceMBean, String serviceRuntimeName) {
        ServerRuntimeMBean[] serverRuntimes = domainRuntimeServiceMBean.getServerRuntimes();
        for(ServerRuntimeMBean serverRuntime:serverRuntimes) {
            if(serverRuntime.getName().equalsIgnoreCase(serviceRuntimeName)) {
                return serverRuntime;
            }
        }

        throw new MonitorException("Unable to find server: " + serviceRuntimeName);
    }

    private SAFAgentRuntimeMBean getSAFServerRuntimeMBeanFromServerRuntime(ServerRuntimeMBean serverRuntime, String safAgent) {
        SAFAgentRuntimeMBean[] agents = serverRuntime.getSAFRuntime().getAgents();
        for(SAFAgentRuntimeMBean safAgentRuntime:agents) {
            if(safAgentRuntime.getName().equalsIgnoreCase(safAgent)) {
                return safAgentRuntime;
            }
        }

        throw new MonitorException("Unable to find JMS server: " + safAgent);
    }

    private JMSServerRuntimeMBean getJMSServerRuntimeMBeanFromServerRuntime(ServerRuntimeMBean serverRuntime, String jmsServer) {
        JMSServerRuntimeMBean[] jmsServers = serverRuntime.getJMSRuntime().getJMSServers();
        for(JMSServerRuntimeMBean jmsServerRuntime:jmsServers) {
            if(jmsServerRuntime.getName().equalsIgnoreCase(jmsServer)) {
                return jmsServerRuntime;
            }
        }

        throw new MonitorException("Unable to find JMS server: " + jmsServer);
    }

	private JMSDestinationRuntimeMBean getJMSDestinationRuntimeMBeanFromJMSServerRuntime(JMSServerRuntimeMBean jmsServerRuntime, String queue) {
		JMSDestinationRuntimeMBean[] destinations = jmsServerRuntime.getDestinations();
		for(JMSDestinationRuntimeMBean jmsDestination:destinations) {
			if(jmsDestination.getName().equalsIgnoreCase(queue)) {
				return jmsDestination;
			}
		}

		throw new MonitorException("Unable to find queue: " + queue);
	}

	private long getQueueLengthFromDomainRuntime(DomainRuntimeServiceMBean domainRuntimeServiceMBean, String serviceRuntimeName, String jmsServer, String queue) {
		ServerRuntimeMBean serverRuntime = getServerRuntimeMBeanFromDomainRuntime(domainRuntimeServiceMBean, serviceRuntimeName);
		JMSServerRuntimeMBean jmsServerRuntime = getJMSServerRuntimeMBeanFromServerRuntime(serverRuntime, jmsServer);
		JMSDestinationRuntimeMBean jmsDestination = getJMSDestinationRuntimeMBeanFromJMSServerRuntime(jmsServerRuntime, queue);

		return jmsDestination.getMessagesCurrentCount();
	}
}
