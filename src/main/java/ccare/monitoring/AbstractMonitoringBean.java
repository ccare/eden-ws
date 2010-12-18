package ccare.monitoring;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

public class AbstractMonitoringBean extends NotificationBroadcasterSupport {
		
	protected final void register(final String group, final String type, final String name) {
		final String beanName = group + ":type=" + type + ",name=" + name;
    	register(beanName);
	}
	
	final void register(final String beanName) {
		final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		registerAsMXBean(createName(beanName), this, mbs);		
	}

	static ObjectName createName(final String beanName) {
		try {
			return new ObjectName(beanName);
		} catch (MalformedObjectNameException e) {
			throw new MonitoringException("Invalid MXBean Name: " + beanName, e);
		} catch (NullPointerException e) {
			throw new MonitoringException("Cannot have null MXBean Name", e);
		}
	}
	
	static void registerAsMXBean(final ObjectName beanName, final AbstractMonitoringBean bean, final MBeanServer mbs) {
    	try {
	        mbs.registerMBean(bean, beanName);
		} catch (InstanceAlreadyExistsException e) {
			throw new MonitoringException("Instance of bean already exists", e);
		} catch (MBeanRegistrationException e) {
			throw new MonitoringException("Exception thrown when registering MXBean", e);
		} catch (NotCompliantMBeanException e) {
			throw new MonitoringException("Exception thrown when registering MXBean - bean not compliant", e);
		}
    }
	
}
