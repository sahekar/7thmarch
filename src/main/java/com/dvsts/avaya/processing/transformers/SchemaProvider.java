package com.dvsts.avaya.processing.transformers;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.Schema;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SchemaProvider {

	private final SchemaRegistryClient registryClient;
	private final int schemaVersion;
	private final Map<String, Schema> schemasMap;

	public SchemaProvider(SchemaRegistryClient registryClient, int schemaVersion) {
		this.registryClient = registryClient;
		this.schemaVersion = schemaVersion;
		this.schemasMap = new ConcurrentHashMap<>();
	}

	public Schema getSchema(String subject) {
		return schemasMap.computeIfAbsent(subject, k -> fetchSchema(subject, schemaVersion));
	}

	private Schema fetchSchema(String subject, int version) {
		try {
			SchemaMetadata schemaMetadata = registryClient.getSchemaMetadata(subject, version);
			return registryClient.getById(schemaMetadata.getId());
		} catch (IOException | RestClientException e) {
			e.printStackTrace();
		}
		return null;
	}
}
