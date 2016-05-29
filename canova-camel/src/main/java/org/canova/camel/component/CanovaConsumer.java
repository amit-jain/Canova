package org.canova.camel.component;

import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.canova.api.conf.Configuration;
import org.canova.api.formats.input.InputFormat;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.records.writer.RecordWriter;
import org.canova.api.split.InputSplit;

/**
 * The canova consumer.
 */
public class CanovaConsumer extends ScheduledPollConsumer {
    private final CanovaEndpoint endpoint;
    private Class<? extends InputFormat> inputFormatClazz;
    private InputFormat inputFormat;
    private Configuration configuration;
    private InputSplit inputSplit;
    public CanovaConsumer(CanovaEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        try {
            inputFormatClazz = (Class<? extends InputFormat>) Class.forName(endpoint.getInputFomrat());
            inputFormat = inputFormatClazz.newInstance();
            configuration = new Configuration();
            for(String prop : endpoint.getConsumerProperties().keySet())
                configuration.set(prop,endpoint.getConsumerProperties().get(prop).toString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //stub, still need to fill out more of the end point yet..endpoint will likely be initialized with a split
    protected InputSplit inputFromExchange(Exchange exchange) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int poll() throws Exception {
        Exchange exchange = endpoint.createExchange();
        InputSplit split = inputFromExchange(exchange);
        RecordReader reader = inputFormat.createReader(split,configuration);
        int numMessagesPolled = 0;
        while(reader.hasNext()) {
            // create a message body
            Date now = new Date();
            exchange.getIn().setBody(reader.next());

            try {
                // send message to next processor in the route
                getProcessor().process(exchange);
                numMessagesPolled++; // number of messages polled
            } finally {
                // log exception if an exception occurred and was not handled
                if (exchange.getException() != null) {
                    getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
                }
            }

        }

        return numMessagesPolled;
    }
}
