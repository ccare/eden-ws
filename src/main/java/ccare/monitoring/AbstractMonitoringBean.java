package ccare.monitoring;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

public abstract class AbstractMonitoringBean extends NotificationBroadcasterSupport {
	
	public AbstractMonitoringBean() {
		registerAsMXBean();
	}

	private void registerAsMXBean() {
    	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    	ObjectName beanName;
		try {
			beanName = new ObjectName(getMxBeanName());
	        mbs.registerMBean(this, beanName);
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	protected abstract String getMxBeanName();
	
}
