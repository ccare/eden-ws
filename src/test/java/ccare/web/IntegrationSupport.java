package ccare.web;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: carecx
 * Date: 11-Nov-2010
 * Time: 07:28:12
 * To change this template use File | Settings | File Templates.
 */
public class IntegrationSupport {
    protected WebResource getResource(final String resourcePath) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource resource = client.resource(getBaseURI(resourcePath));
        return resource;
    }

    private static URI getBaseURI(final String resourcePath) {
        return UriBuilder.fromUri(
				"http://localhost:8080/services/" + resourcePath).build();
	}
}
