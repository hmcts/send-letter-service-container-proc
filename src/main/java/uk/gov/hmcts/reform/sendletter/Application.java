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
            telemetryClient.trackEvent("send letter processed keda container invoked");
            if (telemetryClient.getContext() != null) {
                LOGGER.info("Temp logging Telemetry instrumentationKey {} once working remove this",
                        telemetryClient.getContext().getInstrumentationKey());
            }
            LOGGER.info("send letter processed keda container invoked");
            retrieve.read();
            LOGGER.info("send letter processed keda container finished");
        } catch (Exception e) {
            LOGGER.error("Exception occurred while keda container invoked", e);
        } finally {
            // Initiate flush and give it some time to finish.
            telemetryClient.flush();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                LOGGER.error("Exception in thread sleep", ie);
                Thread.currentThread().interrupt();
            }
        }
    }
}
