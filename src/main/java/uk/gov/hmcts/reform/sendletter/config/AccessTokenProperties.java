package uk.gov.hmcts.reform.sendletter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sendletter.exceptions.ServiceConfigNotFoundException;

import java.util.List;

@Configuration
@ConfigurationProperties("accesstoken")
public class AccessTokenProperties {
    private List<TokenConfig> serviceConfig;

    public List<TokenConfig> getServiceConfig() {
        return serviceConfig;
    }

    public String getContainerForGivenType(String containerType) {
        return getServiceConfig().stream()
                .filter(tokenConfig -> tokenConfig.getContainerType().equals(containerType))
                .map(AccessTokenProperties.TokenConfig::getContainerName)
                .findFirst()
                .orElseThrow(() ->
                        new ServiceConfigNotFoundException(
                                "No service configuration found for container " + containerType));
    }

    public void setServiceConfig(List<TokenConfig> serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public static class TokenConfig {
        private String containerName;
        private int validity;
        private String containerType;

        public int getValidity() {
            return validity;
        }

        public void setValidity(int validity) {
            this.validity = validity;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public String getContainerType() {
            return containerType;
        }

        public void setContainerType(String containerType) {
            this.containerType = containerType;
        }
    }
}
