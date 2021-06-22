package uk.gov.hmcts.reform.sendletter;

import com.microsoft.applicationinsights.TelemetryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.hmcts.reform.sendletter.blob.BlobProcessor;

@SuppressWarnings("HideUtilityClassConstructor")
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Autowired
    private BlobProcessor retrieve;

    @Autowired
    private TelemetryClient telemetryClient;

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            telemetryClient.trackEvent("send letter processed KEDA container invoked");
            if (telemetryClient.getContext() != null) {
                LOGGER.info("Telemetry instrumentationKey {}",
                        telemetryClient.getContext().getInstrumentationKey());
            }
            LOGGER.info("send letter processed KEDA container invoked");
            retrieve.read();
            LOGGER.info("send letter processed KEDA container finished");
            // Initiate flush and give it some time to finish.
            telemetryClient.flush();
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LOGGER.error("Exception occured while KEDA container invoked", e);
            Thread.currentThread().interrupt();
        }
    }
}
