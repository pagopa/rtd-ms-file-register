package it.gov.pagopa.rtd.ms.rtdmsfileregister.telemetry;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration for AppInsight OpenTelemetry. Connection string will automatically take from
 * environment variable APPLICATIONINSIGHTS_CONNECTION_STRING
 */
@Configuration
@ConditionalOnProperty(value = "applicationinsights.enabled", havingValue = "true", matchIfMissing = false)
@Import(SpringCloudKafkaBinderInstrumentation.class)
public class AppInsightConfig {

  
  @Bean
  public AutoConfigurationCustomizerProvider otelCustomizer(
      @Value("${applicationinsights.connection-string}") String applicationInsights) {
    return p -> {
      if (p instanceof AutoConfiguredOpenTelemetrySdkBuilder) {
        AzureMonitorAutoConfigure.customize(p, applicationInsights);
      }
    };
  }


}
