package ccare.monitoring;

public class Example extends AbstractMonitoringBean implements ExampleMXBean{
	
	private static final String stem = "ccare.monitoring:type=Example,name=";

	public static void create(final String name) {
		new Example().register(stem + name);		
	}

}
