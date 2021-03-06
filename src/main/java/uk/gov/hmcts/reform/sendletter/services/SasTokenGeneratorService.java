package uk.gov.hmcts.reform.sendletter.services;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sendletter.config.AccessTokenProperties;
import uk.gov.hmcts.reform.sendletter.config.AccessTokenProperties.TokenConfig;
import uk.gov.hmcts.reform.sendletter.exceptions.ServiceConfigNotFoundException;
import uk.gov.hmcts.reform.sendletter.exceptions.UnableToGenerateSasTokenException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@EnableConfigurationProperties(AccessTokenProperties.class)
@Service
public class SasTokenGeneratorService {

    private static final Logger LOG = LoggerFactory.getLogger(SasTokenGeneratorService.class);

    private final BlobServiceClient blobServiceClient;
    private final AccessTokenProperties accessTokenProperties;
    private static final String PERMISSION_WRITE_LIST = "wlrd";

    public SasTokenGeneratorService(
            BlobServiceClient blobServiceClient,
            AccessTokenProperties accessTokenProperties
    ) {
        this.blobServiceClient = blobServiceClient;
        this.accessTokenProperties = accessTokenProperties;
    }

    public String generateSasToken(String containerName) {
        var config = getTokenConfigForService(containerName);
        LOG.info("SAS Token request received for container '{}'", config.getContainerName());

        try {
            return blobServiceClient
                    .getBlobContainerClient(config.getContainerName())
                    .generateSas(createSharedAccessPolicy(config));
        } catch (Exception e) {
            throw new UnableToGenerateSasTokenException(e);
        }
    }

    private BlobServiceSasSignatureValues createSharedAccessPolicy(TokenConfig config) {

        return new BlobServiceSasSignatureValues(
                OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(config.getValidity()),
                BlobContainerSasPermission.parse(PERMISSION_WRITE_LIST)
        );
    }

    private TokenConfig getTokenConfigForService(String containerName) {
        return accessTokenProperties.getServiceConfig().stream()
            .filter(tokenConfig -> tokenConfig.getContainerName().equalsIgnoreCase(containerName))
            .findFirst()
            .orElseThrow(
                    () -> new ServiceConfigNotFoundException(
                            "No service configuration found for container " + containerName)
            );
    }

}
