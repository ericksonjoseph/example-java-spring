package com.ericksonjoseph.assignment.fs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import com.ericksonjoseph.assignment.config.Config;

@ConfigurationProperties("fs")
public class StorageProperties {

    private String location = Config.get("app.video.uploadDir");

    public String getLocation() {
        return location;
    }
}
