package uk.gov.hmcts.reform.sendletter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import uk.gov.hmcts.reform.sendletter.exceptions.ServiceConfigNotFoundException;

import java.util.List;

@ConfigurationProperties("accesstoken")
public class AccessTokenProperties {
    private List<TokenConfig> serviceConfig;

    public List<TokenConfig> getServiceConfig() {
        return serviceConfig;
    }

    public TokenConfig getTokenConfigForService(String containerName) {
        return this.getServiceConfig().stream()
                .filter(tokenConfig -> tokenConfig.getContainerName().equalsIgnoreCase(containerName))
                .findFirst()
                .orElseThrow(
                        () -> new ServiceConfigNotFoundException(
                                "No service configuration found for container " + containerName)
                );
    }

    public void setServiceConfig(List<TokenConfig> serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public static class TokenConfig {
        private String containerName;
        private int validity;

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
    }
}
