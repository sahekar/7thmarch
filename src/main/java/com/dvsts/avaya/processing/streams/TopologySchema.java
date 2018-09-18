package com.dvsts.avaya.processing.streams;

import com.dvsts.avaya.processing.KafkaStreamsUtils;
import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import com.dvsts.avaya.processing.processors.AvayaTransformationProcessor;
import com.dvsts.avaya.processing.processors.SessionCreatorProcessor;
import com.dvsts.avaya.processing.transformers.AvroTransformer;
import com.dvsts.avaya.processing.transformers.JsonPOJODeserializer;
import com.dvsts.avaya.processing.transformers.JsonPOJOSerializer;
import com.dvsts.avaya.processing.transformers.SchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.dvsts.avaya.processing.AppConfig.*;
import static com.dvsts.avaya.processing.KafkaStreamsUtils.initStore;

public class TopologySchema {

    private Properties properties;
    private String schemaRegistry;
    private String bootstrapServers;
    final private String transformationProcessor = "transformationProcessor";
    final private String sessionProcessor = "sessionCreatorProcessor";

    private MainComputationModel mainComputationModel = new MainComputationModel();


    public TopologySchema( Properties properties) {
        this.schemaRegistry = properties.getProperty("kafka.schema.registry.url");
        this.bootstrapServers = properties.getProperty("camel.component.kafka.brokers");
    }




    public void createSimpleStorage() {
        final SchemaRegistryClient schemaRegistryClient = KafkaStreamsUtils.createSchemaRegistryClient(schemaRegistry);


        GenericAvroDeserializer avroDeserializer = new GenericAvroDeserializer();
        GenericAvroSerializer avroSerializer = new GenericAvroSerializer();
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",schemaRegistry);
        avroDeserializer.configure(serdeConfig,false);
        avroSerializer.configure(serdeConfig,false);


        Topology builder = createTopology(schemaRegistryClient,avroDeserializer,avroSerializer);

        KafkaStreams streams = new KafkaStreams(builder,createProps());


        streams.start();



    }

    public Topology createTopology( final SchemaRegistryClient schemaRegistryClient,final Deserializer avroDeserializer,final Serializer avroSerializer){

        final AvroTransformer transformer = new AvroTransformer(KafkaStreamsUtils.schemaRegistryClient(schemaRegistryClient));
        Processor avayaTransformationProcessor = new AvayaTransformationProcessor(transformer, mainComputationModel);
        Processor sessionCreatorProcessor = new SessionCreatorProcessor(transformer);

        Topology builder = new Topology();

        builder

                .addSource("Source",new StringDeserializer(),avroDeserializer,initialAvayaSourceTopic)

                .addProcessor(transformationProcessor,  () -> avayaTransformationProcessor,"Source")
                .addProcessor(sessionProcessor,  () -> sessionCreatorProcessor,transformationProcessor)

                .addStateStore(initStore(db),transformationProcessor)

                .addSink("ChangeState",detailsEventTopic,new StringSerializer(),avroSerializer,transformationProcessor)
                .addSink("session",sessionEventTopic,new StringSerializer(),avroSerializer,sessionProcessor)

                // link store to first processor
                .connectProcessorAndStateStores(transformationProcessor,db)
                .connectProcessorAndStateStores(sessionProcessor,db);

        return builder;

    }


    private Properties createProps(){
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test878ff");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  GenericAvroSerde.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);

        return props;
    }






    private Serializer createSerializer(){
        Serializer serializer = new KafkaAvroSerializer();
        Map<String, Object> serProps = new HashMap<>();
        serProps.put("schema.registry.url",schemaRegistry);
        serializer.configure(serProps,false);

        return serializer;
    }




}
