package uk.gov.hmcts.reform.sendletter.blob.component;

import com.azure.storage.blob.models.BlobItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sendletter.config.AccessTokenProperties;
import uk.gov.hmcts.reform.sendletter.model.ProcessedBlobInfo;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class BlobReader {

    private static final Logger LOG = LoggerFactory.getLogger(BlobReader.class);
    private static final String PROCESSES_CONTAINER = "processed";
    private final BlobManager blobManager;
    private final AccessTokenProperties accessTokenProperties;

    public BlobReader(BlobManager blobManager, AccessTokenProperties accessTokenProperties) {
        this.blobManager = blobManager;
        this.accessTokenProperties = accessTokenProperties;
    }

    public List<ProcessedBlobInfo> retrieveManifestsToProcess() {
        LOG.info("About to read processed blobs from '{}' container", PROCESSES_CONTAINER);
        var tokenConfig =
                accessTokenProperties.getTokenConfigForService(PROCESSES_CONTAINER);
        var containerName = tokenConfig.getContainerName();
        var containerClient = blobManager.getContainerClient(containerName);
        return containerClient.listBlobs().stream()
                .map(BlobItem::getName)
                .map(fileName ->
                        new ProcessedBlobInfo(
                                containerName,
                                fileName)
                )
                .collect(toList());

    }
}
