package ccare.monitoring;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.junit.Test;

import ccare.symboltable.SymbolDefinition;

public class AbstractMonitoringBeanTest {

	@Test(expected=MonitoringException.class)
	public void testCreateMXBeanWithNullNameFails() {
		createBean(null);
	}
	
	@Test(expected=MonitoringException.class)
	public void testCreateMXBeanWithInvalidNameFails() {
		createBean("foo");
	}
	
	@Test
	public void testCreateMXBeanWithOkNameDoesntFail() {
		Example.create("beanA");
		Example.create("beanB");
	}
	
	@Test(expected=MonitoringException.class)
	public void testAttemptingDoubleRegistrationFails() {
		Example.create("beanC");
		Example.create("beanC");
	}
	
	@Test(expected=MonitoringException.class)
	public void testCreationWrapsRegistrationException() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final Throwable innerException = new MBeanRegistrationException(null);
		stubOutRegistrationAndThrow(innerException);
	}
	
	@Test(expected=MonitoringException.class)
	public void testCreationWrapsCompilanceException() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final Throwable innerException = new NotCompliantMBeanException();
		stubOutRegistrationAndThrow(innerException);
	}


	private void stubOutRegistrationAndThrow(
			final Throwable innerException)
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		AbstractMonitoringBean bean = createMock(AbstractMonitoringBean.class);
		ObjectName name = createMock(ObjectName.class);
		MBeanServer mbs = createMock(MBeanServer.class);
		expect(mbs.registerMBean(bean, name)).andThrow(innerException);
        replay(mbs);
		AbstractMonitoringBean.registerAsMXBean(name, bean, mbs);
	}
	


	private void createBean(final String name) {
		new AbstractMonitoringBean().register(name);
	}

}
