package com.github.viktornar.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    @Value("${server.port}")
    @Getter
    @Setter
    private String port;
    @Value("${atlas.hostname}")
    @Getter
    @Setter
    private String hostname;
    @Value("${server.contextPath}")
    @Getter
    @Setter
    private String contextPath;
}
