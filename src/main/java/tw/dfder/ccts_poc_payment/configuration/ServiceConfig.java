package tw.dfder.ccts_poc_payment.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * To load application.yml setting
 * @Author df
 * @version v2
 */
@Configuration
@ConfigurationProperties(prefix = "serviceinfo")
public class ServiceConfig {

    public String name;
//
//    @Value("${serviceInfo.pact}")
//    public static String correspondingPact;

    public List<String> destinations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<String> destinations) {
        this.destinations = destinations;
    }
}
