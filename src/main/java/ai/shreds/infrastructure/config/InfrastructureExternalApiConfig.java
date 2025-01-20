package ai.shreds.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for external API clients.
 * Configures REST template and API properties with proper timeout and retry settings.
 */
@Configuration
public class InfrastructureExternalApiConfig {

    @Value("${aviationstack.api.key}")
    private String apiKey;

    @Value("${aviationstack.api.base-url:http://api.aviationstack.com/v1/flights}")
    private String baseUrl;

    @Value("${aviationstack.api.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${aviationstack.api.read-timeout:5000}")
    private int readTimeout;

    /**
     * Configures RestTemplate with timeout settings.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    /**
     * Configures HTTP client factory with timeout settings.
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    /**
     * Gets the API key for aviation stack.
     * @return the API key
     */
    public String getApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Aviation Stack API key is not configured");
        }
        return apiKey;
    }

    /**
     * Gets the base URL for aviation stack API.
     * @return the base URL
     */
    public String getBaseUrl() {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Aviation Stack base URL is not configured");
        }
        return baseUrl;
    }
}
