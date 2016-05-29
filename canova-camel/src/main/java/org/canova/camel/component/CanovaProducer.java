package org.canova.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.canova.api.conf.Configuration;
import org.canova.api.formats.output.OutputFormat;
import org.canova.api.records.writer.RecordWriter;
import org.canova.api.writable.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * The canova producer.
 */
public class CanovaProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(CanovaProducer.class);
    private CanovaEndpoint endpoint;
    private Class<? extends OutputFormat> outputFormatClazz;
    private Configuration configuration;
    private OutputFormat outputFormat;

    public CanovaProducer(CanovaEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
        if(endpoint.getOutputFormat() != null)
            try {
                outputFormatClazz = (Class<? extends OutputFormat>) Class.forName(endpoint.getOutputFormat());
                configuration = new Configuration();
                for(String prop : endpoint.getConsumerProperties().keySet())
                    configuration.set(prop,endpoint.getConsumerProperties().get(prop).toString());
                outputFormat = outputFormatClazz.newInstance();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if(endpoint.getOutputFormat().isEmpty())
            throw new IllegalStateException("Output format must not be empty.");
        Collection<Writable> record = (Collection<Writable>)  exchange.getIn().getBody();
        RecordWriter writer = outputFormat.createWriter(configuration);
        writer.write(record);
    }

}
