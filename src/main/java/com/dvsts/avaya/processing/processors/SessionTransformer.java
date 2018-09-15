package com.dvsts.avaya.processing.processors;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

public class SessionTransformer implements Processor<String, GenericRecord> {

    private ProcessorContext context;
    private KeyValueStore<String, Long> kvStore;

    @Override
    public void init(ProcessorContext context) {
        // keep the processor context locally because we need it in punctuate() and commit()
        this.context = context;

        // retrieve the key-value store named "Counts"
        kvStore = (KeyValueStore) context.getStateStore("Counts");

        // schedule a punctuate() method every 1000 milliseconds based on event-time
        this.context.schedule(1000, PunctuationType.STREAM_TIME, (timestamp) -> {
            KeyValueIterator<String, Long> iter = this.kvStore.all();
            while (iter.hasNext()) {
                KeyValue<String, Long> entry = iter.next();
                context.forward(entry.key, entry.value.toString());
            }
            iter.close();

            // commit the current processing progress
            context.commit();
        });
    }

    @Override
    public void process(String key, GenericRecord value) {

    }

    @Override
    public void close() {

    }
}
