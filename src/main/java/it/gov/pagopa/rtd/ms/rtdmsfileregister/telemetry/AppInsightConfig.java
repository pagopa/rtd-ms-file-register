package it.gov.pagopa.rtd.ms.rtdmsfileregister.telemetry;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.mongo.v3_1.MongoTelemetry;
import io.opentelemetry.instrumentation.spring.integration.v4_1.SpringIntegrationTelemetry;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptor;

@Configuration
@ConditionalOnProperty(value = "applicationinsights.enabled", havingValue = "true", matchIfMissing = false)
public class AppInsightConfig {

  private final AzureMonitorExporterBuilder azureMonitorExporterBuilder;

  public AppInsightConfig() {
    this.azureMonitorExporterBuilder = new AzureMonitorExporterBuilder();
  }

  @Bean
  public SpanExporter azureSpanProcessor() {
    return azureMonitorExporterBuilder.buildTraceExporter();
  }

  @Bean
  public MetricExporter azureMetricExporter() {
    return azureMonitorExporterBuilder.buildMetricExporter();
  }

  @Bean
  public LogRecordExporter azureLogRecordExporter() {
    return azureMonitorExporterBuilder.buildLogRecordExporter();
  }

  @Bean
  public MongoClientSettingsBuilderCustomizer mongoOpenTelemetryBridge(
      OpenTelemetry openTelemetry
  ) {
    return clientSettingsBuilder -> clientSettingsBuilder
        .addCommandListener(MongoTelemetry.builder(openTelemetry).build().newCommandListener());
  }

  @Bean
  @GlobalChannelInterceptor(order = Integer.MIN_VALUE, patterns = "*")
  public ChannelInterceptor openTelemetryChannelInterceptor(OpenTelemetry openTelemetry) {
    return SpringIntegrationTelemetry.builder(openTelemetry)
        .setProducerSpanEnabled(true)
        .build().newChannelInterceptor();
  }
}
