package com.dvsts.avaya.processing;

import com.dvsts.avaya.processing.streams.StreamCreator;

import java.io.IOException;
import java.util.Properties;

import static com.dvsts.avaya.processing.AppConfig.detailsEventTopic;
import static com.dvsts.avaya.processing.AppConfig.initialAvayaSourceTopic;

public class Application {




    public static void main(String[] args) throws IOException {

        Properties properties =new Properties();
        properties.load(Application.class.getClassLoader().getResourceAsStream("application.properties"));

     //TopologySchema topologySchema = new TopologySchema(properties);

    // topologySchema.createSimpleStorage();

         StreamCreator creator = new StreamCreator(properties);
         creator.streamWithTransformer(initialAvayaSourceTopic,detailsEventTopic);



    }

}
