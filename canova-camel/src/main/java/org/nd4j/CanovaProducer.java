package org.nd4j;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The canova producer.
 */
public class CanovaProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(CanovaProducer.class);
    private CanovaEndpoint endpoint;

    public CanovaProducer(CanovaEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        System.out.println(exchange.getIn().getBody());    
    }

}
