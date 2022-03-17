package tw.dfder.ccts_poc_payment.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * To load application.yml setting
 * @Author df
 * @version v1
 */
@Configuration
public class ServiceConfig {
    @Value("${serviceInfo.name}")
    public String serviceName;
//
//    @Value("${serviceInfo.pact}")
//    public static String correspondingPact;

    @Value("${serviceInfo.destinations}")
    public List<String> destinations;

}
