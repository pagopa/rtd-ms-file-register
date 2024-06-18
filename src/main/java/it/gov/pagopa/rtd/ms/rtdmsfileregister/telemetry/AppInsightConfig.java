package it.gov.pagopa.rtd.ms.rtdmsfileregister.telemetry;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.mongo.v3_1.MongoTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
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

  private final AzureMonitorExporterBuilder azureMonitorExporterBuilder;

  public AppInsightConfig(
      @Value("${applicationinsights.connection-string}") String applicationInsights) {
    this.azureMonitorExporterBuilder = new AzureMonitorExporterBuilder().connectionString(
        applicationInsights);
  }

  @Bean
  public AutoConfigurationCustomizerProvider otelCustomizer() {
    return p -> {
      if (p instanceof AutoConfiguredOpenTelemetrySdkBuilder) {
        this.azureMonitorExporterBuilder.install((AutoConfiguredOpenTelemetrySdkBuilder) p);
      }
    };
  }

  @Bean
  MongoClientSettingsBuilderCustomizer mongoOpenTelemetryBridge(
      OpenTelemetry openTelemetry
  ) {
    return clientSettingsBuilder -> clientSettingsBuilder
        .addCommandListener(MongoTelemetry.builder(openTelemetry).build().newCommandListener());
  }
}
