package com.dvsts.avaya.processing.transformers;

import com.dvsts.avaya.processing.logic.AvayaPacket;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvroTransformer {

	private final SchemaProvider schemaProvider;

	public AvroTransformer(SchemaProvider schemaProvider) {
		this.schemaProvider = schemaProvider;
	}

	/**
	 *
	 * @param data - Map of fieldKey -> fieldValue.
	 * @param subject - subject in Schema Registry to determine schema to use for GenericRecord
	 * @return GenericRecord to be serialized in Avro format
	 */
	public GenericRecord toEventAvroRecord(Map<String, Object> data, String subject) {
		GenericRecordBuilder recordBuilder = new GenericRecordBuilder(
				schemaProvider.getSchema(subject + "-value"));
		data.forEach((fieldName, value) -> recordBuilder.set(fieldName.toLowerCase(), toSupportedType(value)));


		return recordBuilder.build();
	}

	public GenericRecord toEventAvroRecord(AvayaPacket data, String subject)  {
		GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schemaProvider.getSchema(subject + "-value"));

		long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		System.out.println(data.getSsrc1()+data.getSsrc2()+ now);
		recordBuilder.set("id",(data.getSsrc1()+data.getSsrc2()+ now));
		recordBuilder.set("ssrc1",toSupportedType(data.getSsrc1()));
		recordBuilder.set("ssrc2",toSupportedType(data.getSsrc2()));
		recordBuilder.set("jitter",toSupportedType(data.getJitter()));
		recordBuilder.set("rtd",toSupportedType(data.getRtd()));
		recordBuilder.set("loss",toSupportedType(data.getLoss()));
		recordBuilder.set("mos",toSupportedType(data.getMos1()));
		recordBuilder.set("alarm",toSupportedType(data.getAlarm()));
		//recordBuilder.set("maxJitter",toSupportedType(data.getMaxJitter()));
		//recordBuilder.set("totalJitter",toSupportedType(data.getMaxJitter()));

		return recordBuilder.build();
	}


	public GenericRecord toSessionAvroRecord(AvayaPacket side1, String subject)  {
		GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schemaProvider.getSchema(subject + "-value"));

		recordBuilder.set("ssrc1",toSupportedType(side1.getSsrc1()));
		recordBuilder.set("ssrc2",toSupportedType(side1.getSsrc2()));
		recordBuilder.set("jitter",toSupportedType(side1.getJitter()));
		recordBuilder.set("rtd",toSupportedType(side1.getRtd()));
		recordBuilder.set("loss",toSupportedType(side1.getLoss()));
		recordBuilder.set("mos",toSupportedType(side1.getMos1()));
		recordBuilder.set("alarm",toSupportedType(side1.getAlarm()));
		//recordBuilder.set("maxJitter",toSupportedType(side1.getMaxJitter()));
		//recordBuilder.set("totalJitter",toSupportedType(side1.getMaxJitter()));

		return recordBuilder.build();
	}
	public GenericRecord toSessionAvroRecord(AvayaPacket side1,AvayaPacket side2, String subject)  {
		GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schemaProvider.getSchema(subject + "-value"));

		recordBuilder.set("ssrc1",toSupportedType(side1.getSsrc1()));
		recordBuilder.set("ssrc2",toSupportedType(side1.getSsrc2()));
		recordBuilder.set("jitter",toSupportedType(side1.getJitter()));
		recordBuilder.set("rtd",toSupportedType(side1.getRtd()));
		recordBuilder.set("loss",toSupportedType(side1.getLoss()));
		recordBuilder.set("mos",toSupportedType(side1.getMos1()));
		recordBuilder.set("alarm",toSupportedType(side1.getAlarm()));
		//recordBuilder.set("maxJitter",toSupportedType(side1.getMaxJitter()));
		//recordBuilder.set("totalJitter",toSupportedType(side1.getMaxJitter()));

		return recordBuilder.build();
	}

	public GenericRecord toEventAvroRecord(Object data, String subject) throws IllegalAccessException {
		GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schemaProvider.getSchema(subject + "-value"));

		Class<? extends Object> c = data.getClass();
		Field[] fields = c.getDeclaredFields();

		for(Field field : fields){
			field.setAccessible(true);
			System.out.println("field: "+field.getName());
			recordBuilder.set(field.getName().toLowerCase(),toSupportedType(field.get(data)));
		}

         return recordBuilder.build();
	}

	/**
	 *
	 * @param data - list of SimpleEntry<String, Object>[] where each array represents one logical record.
	 *             One SimpleEntry represents one field of record.
	 * @param subject - subject in Schema Registry to determine schema to use for GenericRecord
	 * @return list of GenericRecord to be serialized in Avro format
	 */
	public List<GenericRecord> toAvroRecords(List<AbstractMap.SimpleEntry<String, Object>[]> data, String subject) {
		return data.stream().map(d -> arrayToAvroRecord(d, subject)).collect(Collectors.toList());
	}

	private GenericRecord arrayToAvroRecord(AbstractMap.SimpleEntry<String, Object>[] data, String subject) {
		GenericRecordBuilder recordBuilder = new GenericRecordBuilder(
				schemaProvider.getSchema(subject + "-value"));
		for (AbstractMap.SimpleEntry<String, Object> datum: data) {
			recordBuilder.set(datum.getKey().toLowerCase(), toSupportedType(datum.getValue()));
		}
		return recordBuilder.build();
	}

	private Object toSupportedType(Object value) {
		if (value == null ) return null;
		if (value instanceof Byte) {
			return ((Byte) value).intValue();
		}

		if (value instanceof Short) {
			return ((Short) value).intValue();
		}
		if (value instanceof BigDecimal) {
			BigInteger ulv = ((BigDecimal) value).unscaledValue();
			return ByteBuffer.wrap(ulv.toByteArray());
		}
		if (value instanceof Timestamp) {
			return ((Timestamp) value).getTime();
		}
		return value;
	}
}
