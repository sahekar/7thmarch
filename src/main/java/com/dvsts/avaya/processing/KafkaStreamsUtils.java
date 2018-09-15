package com.dvsts.avaya.processing;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import com.dvsts.avaya.processing.transformers.JsonPOJODeserializer;
import com.dvsts.avaya.processing.transformers.JsonPOJOSerializer;
import com.dvsts.avaya.processing.transformers.SchemaProvider;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.util.HashMap;
import java.util.Map;

import static com.dvsts.avaya.processing.AppConfig.db;

public class KafkaStreamsUtils {

    /**
     * The method which configure state store for kafka streams. Under statestore we use embedded rockdb
     * look the link
     * @return
     */
    public static StoreBuilder initStore(String storeName){

        final Serde<String> stringSerde = Serdes.String();
        Map<String, Object> serdeProps = new HashMap<>();
        final Serializer<AvayaPacket> jsonPOJOSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", AvayaPacket.class);
        jsonPOJOSerializer.configure(serdeProps, false);
        final Deserializer<AvayaPacket> jsonDeserializer = new JsonPOJODeserializer<>();
        jsonDeserializer.configure(serdeProps,false);
        final Serde<AvayaPacket> serde = Serdes.serdeFrom(jsonPOJOSerializer, jsonDeserializer);

        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(storeName),stringSerde,serde);
    }

    public static SchemaProvider schemaRegistryClient(SchemaRegistryClient client) {
        SchemaProvider provider = new SchemaProvider(client,1);
        return provider;
    }

    public static SchemaProvider schemaRegistryClient(String schemaRegistry) {
        SchemaProvider provider = new SchemaProvider(createSchemaRegistryClient(schemaRegistry),1);
        return provider;
    }

    public static SchemaRegistryClient createSchemaRegistryClient(String schemaRegistry){
       return new CachedSchemaRegistryClient(schemaRegistry, 2);
    }
}
