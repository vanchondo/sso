package com.vanchondo.sso.configs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "mail.smtp")
@Getter
@Setter
public class EmailConfiguration {
    private boolean auth;
    @Value("${mail.smtp.starttls.enable}")
    private boolean starttls;
    private String host;
    private String port;
    @Value("${mail.smtp.ssl.trust}")
    private String ssl;
    private String username;
    private String password;

    public Properties getProperties() {
        Properties prop = new Properties();
        String prefix = "mail.smtp.";
        prop.put(prefix + "auth", auth);
        prop.put(prefix + "starttls.enable", starttls);
        prop.put(prefix + "host", host);
        prop.put(prefix + "port", port);
        prop.put(prefix + "ssl.trust", ssl);
        prop.put(prefix + "username", username);
        prop.put(prefix + "password", password);

        return prop;
    }
}
