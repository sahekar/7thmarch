package com.dvsts.avaya.processing;

public class KafkaStreamConfigTest {
    public final static String outputSchema = "{\n" +
            "  \"type\": \"record\",\n" +
            "  \"name\": \"test_upd3\",\n" +
            "  \"namespace\": \"com.dvsts\",\n" +
            "  \"doc\": \"this is a sample avro schema to get you started. please edit\",\n" +
            "  \"fields\": [\n" +
            "    {\n" +
            "      \"name\": \"ssrc1\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"ssrc2\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"jitter\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"int\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"rtd\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"int\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"loss\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"int\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"mos\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"float\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"alarm\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"int\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    public final static String inputSchema = "{\n" +
            "  \"type\": \"record\",\n" +
            "  \"name\": \"test_upd3\",\n" +
            "  \"namespace\": \"com.dvsts\",\n" +
            "  \"doc\": \"this is a sample avro schema to get you started. please edit\",\n" +
            "  \"fields\": [\n" +
            "    {\n" +
            "      \"name\": \"ssrc1\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"ssrc2\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"jitter\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"rtt\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"loss\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"cumulativeloss\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"time\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"long\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"lsr\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"dlsr\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"codec\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"sr\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"name1\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"name2\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"transpondername\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"type1\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"type2\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"hopnamelookup\",\n" +
            "      \"type\": \"boolean\",\n" +
            "      \"default\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"fromrtcp\",\n" +
            "      \"type\": \"boolean\",\n" +
            "      \"default\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"reportedip\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"reportedport\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"owd\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"burstloss\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"burstdensity\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"gaploss\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"gapdensity\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        \"string\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
