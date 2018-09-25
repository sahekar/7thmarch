package com.dvsts.avaya.processing.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonUtils {

    public static   String  getJsonString(String jsonPath) throws URISyntaxException, IOException {
        URL url = JsonUtils.class.getResource(jsonPath);

        Path path = Paths.get(url.toURI());

        return new String(Files.readAllBytes(path),"UTF8");

    }

}
