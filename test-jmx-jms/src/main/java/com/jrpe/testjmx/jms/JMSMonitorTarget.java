package com.jrpe.testjmx.jms;

import com.jrpe.testjmx.jms.MonitorException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The class {@code JMSMonitorTarget} representing a monitor target. It is populated using reflation by the method
 */
public class JMSMonitorTarget {
    private String desc;
    private Boolean enable;
    private String hostname;
    private Integer port;
    private String username;
    private String password;
    private String server;
    // Not camel-case because of reflection
    private String jmsserver;
    private String safagent;
    // ----------

    private List<QueueInformation> queueList = new ArrayList<>();


    /**
     * Set a pr
     * @param name
     * @param value
     */
    public void setProperty(String name, String value) {
        try {
            if(name.equals("queuelist")) {
                populateQueueList(value);
            } else {
                Field field = this.getClass().getDeclaredField(name);
                field.setAccessible(true);
                if(field.getType().equals(Integer.class)) {
                    field.set(this, Integer.valueOf(value));
                } else if(field.getType().equals(Boolean.class)) {
                    field.set(this, Boolean.valueOf(value));
                } else {
                    field.set(this, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MonitorException("Could not set value for property " + name, e);
        }
    }

    /**
     * @return
     */
    public String getMissing() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        if(desc == null || desc.length() == 0) sb.append("desc\n");
        if(enable == null) sb.append("enable\n");
        if(hostname == null || hostname.length() == 0) sb.append("hostname\n");
        if(port == null) sb.append("port\n");
        if(username == null || username.length() == 0) sb.append("username\n");
        if(password == null || password.length() == 0) sb.append("password\n");
        if(server == null || server.length() == 0) sb.append("server\n");
        if(queueList.size() == 0) sb.append("queuelist\n");
        if((safagent == null || safagent.length() == 0) && (jmsserver == null || jmsserver.length() == 0)) sb.append("safagent or jmsserver\n");
        return sb.toString();
    }

    /**
     * @return
     */
    public boolean isValid() {
        String missing = getMissing();
        if(missing.length() == 1) return true;
        return false;
    }

    public String getDesc() {
        return desc;
    }

    public Boolean getEnable() {
        return enable;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getServer() {
        return server;
    }

    public String getSafAgent() {
        return safagent;
    }

    public String getJmsServer() {
        return jmsserver;
    }

    public List<QueueInformation> getQueueList() {
        return queueList;
    }

    @Override
    public String toString() {
        return "JMSMonitorTarget{" +
                "desc='" + desc + '\'' +
                ", enable=" + enable +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", server='" + server + '\'' +
                ", jmsserver='" + jmsserver + '\'' +
                ", safagent='" + safagent + '\'' +
                ", queueList=" + queueList +
                '}';
    }

    private void populateQueueList(String queueList) {
        if(queueList != null && queueList.length() > 1) {
            String[] queues = queueList.split(",");
            for(int i=0;i<queues.length;i++) {
                this.queueList.add(new QueueInformation(queues[i].trim()));
            }
        }
    }

    /**
     *
     */
    public class QueueInformation {
        private final String queue;
        private long countCurrent;
        private long count24H;
        private long highCount;
        private long receivedCount;

        public QueueInformation(String queue) {
            this.queue = queue;
        }

        public long getCountCurrent() {
            return countCurrent;
        }

        public long getCount24H() {
            return count24H;
        }

        public String getQueue() {
            return queue;
        }

        public long getHighCount() {
            return highCount;
        }

        public long getReceivedCount() {
            return receivedCount;
        }

        public void setCountCurrent(long countCurrent) {
            this.countCurrent = countCurrent;
        }

        public void setCount24H(long count24H) {
            this.count24H = count24H;
        }

        public void setHighCount(long highCount) {
            this.highCount = highCount;
        }

        public void setReceivedCount(long receivedCount) {
            this.receivedCount = receivedCount;
        }

        @Override
        public String toString() {
            return "QueueInformation{" +
                    "queue='" + queue + '\'' +
                    ", countCurrent=" + countCurrent +
                    ", count24H=" + count24H +
                    ", highCount=" + highCount +
                    ", receivedCount=" + receivedCount +
                    '}';
        }
    }
}
