package org.nd4j;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;

import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages {@link CanovaEndpoint}.
 */
public class CanovaComponent extends UriEndpointComponent {
    
    public CanovaComponent() {
        super(CanovaEndpoint.class);
    }

    public CanovaComponent(CamelContext context) {
        super(context, CanovaEndpoint.class);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new CanovaEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
