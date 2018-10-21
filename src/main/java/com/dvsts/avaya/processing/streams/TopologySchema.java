package com.dvsts.avaya.processing.streams;

import com.dvsts.avaya.core.domain.event.AvayaEvent;
import com.dvsts.avaya.processing.KafkaStreamsUtils;
import com.dvsts.avaya.processing.logic.MainComputationModel;
import com.dvsts.avaya.processing.processors.SideCreatorProcessor;
import com.dvsts.avaya.processing.processors.SessionCreatorProcessor;
import com.dvsts.avaya.processing.processors.SessionFinisherProcessor;
import com.dvsts.avaya.processing.transformers.AvroTransformer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.Processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.dvsts.avaya.processing.AppConfig.*;
import static com.dvsts.avaya.processing.KafkaStreamsUtils.initStore;


/**
 *
 */
public class TopologySchema {

    private Properties properties;
    private String schemaRegistry;
    private String bootstrapServers;
    final private String transformationProcessor = "transformationProcessor";
    final private String sessionFinisherProcessor = "sessionFinisherProcessor";
    final private String sessionCreatorProcessor = "sessionCreatorProcessor";

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

        Serde<AvayaEvent> serdeCustomer = new SpecificAvroSerde<>();


        serdeCustomer.configure(serdeConfig,false);

        Topology builder = createTopology(schemaRegistryClient,serdeCustomer.deserializer(),avroSerializer);

        KafkaStreams streams = new KafkaStreams(builder,createProps());


        streams.start();



    }

    public Topology createTopology( final SchemaRegistryClient schemaRegistryClient,final Deserializer avroDeserializer,final Serializer avroSerializer) {

        final AvroTransformer transformer = new AvroTransformer(KafkaStreamsUtils.schemaRegistryClient(schemaRegistryClient));
        Processor sideCreatorProcessor = new SideCreatorProcessor(transformer, mainComputationModel);
        Processor sessionFinisher = new SessionFinisherProcessor(transformer);
        Processor sessionCreator = new SessionCreatorProcessor();

        Topology builder = new Topology();

        builder

                .addSource("Source",new StringDeserializer(),avroDeserializer,initialAvayaSourceTopic)

                .addProcessor(transformationProcessor,  () -> sideCreatorProcessor,"Source")
                .addProcessor(sessionCreatorProcessor,() -> sessionCreator,transformationProcessor)
                .addProcessor(sessionFinisherProcessor,  () -> sessionFinisher,sessionCreatorProcessor)

                .addStateStore(initStore(db),transformationProcessor)

                .addSink("ChangeState",detailsEventTopic,new StringSerializer(),avroSerializer,transformationProcessor)
                .addSink("session", sessionEventTopic ,new StringSerializer(),avroSerializer, sessionCreatorProcessor)
                .addSink("session_finisher", sessionEventTopic ,new StringSerializer(),avroSerializer, sessionFinisherProcessor)

                // link store to first processor
                .connectProcessorAndStateStores(transformationProcessor,db)
                .connectProcessorAndStateStores(sessionCreatorProcessor,db)
                .connectProcessorAndStateStores(sessionFinisherProcessor,db);

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
