package ccare.monitoring;

public class MonitoringException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MonitoringException(String msg, Exception innerException) {
		super(msg, innerException);
	}

}
