package ccare.monitoring;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.junit.Test;

public class AbstractMonitoringBeanTest {

	private void createBean(final String name) {
		new AbstractMonitoringBean().register(name);
	}

	private void stubOutRegistrationAndThrow(final Throwable innerException)
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		AbstractMonitoringBean bean = createMock(AbstractMonitoringBean.class);
		ObjectName name = createMock(ObjectName.class);
		MBeanServer mbs = createMock(MBeanServer.class);
		expect(mbs.registerMBean(bean, name)).andThrow(innerException);
		replay(mbs);
		AbstractMonitoringBean.registerAsMXBean(name, bean, mbs);
	}

	@Test(expected = MonitoringException.class)
	public void testAttemptingDoubleRegistrationFails() {
		Example.create("beanC");
		Example.create("beanC");
	}

	@Test(expected = MonitoringException.class)
	public void testCreateMXBeanWithInvalidNameFails() {
		createBean("foo");
	}

	@Test(expected = MonitoringException.class)
	public void testCreateMXBeanWithNullNameFails() {
		createBean(null);
	}

	@Test
	public void testCreateMXBeanWithOkNameDoesntFail() {
		Example.create("beanA");
		Example.create("beanB");
	}

	@Test(expected = MonitoringException.class)
	public void testCreationWrapsCompilanceException()
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		final Throwable innerException = new NotCompliantMBeanException();
		stubOutRegistrationAndThrow(innerException);
	}

	@Test(expected = MonitoringException.class)
	public void testCreationWrapsRegistrationException()
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		final Throwable innerException = new MBeanRegistrationException(null);
		stubOutRegistrationAndThrow(innerException);
	}

}
