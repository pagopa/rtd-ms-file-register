package it.gov.pagopa.rtd.ms.rtdmsfileregister.telemetry;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.mongo.v3_1.MongoTelemetry;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.telemetry.AppInsightConfig.CustomRuntimeHints;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ConditionalOnProperty(value = "applicationinsights.enabled", havingValue = "true", matchIfMissing = false)
@Import(SpringCloudKafkaBinderInstrumentation.class)
@ImportRuntimeHints(CustomRuntimeHints.class)
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

  // Probably due to an instability of snapshot version some reflection data are missing,
  // this register the missing class.
  public static class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(org.springframework.aot.hint.RuntimeHints hints,
        ClassLoader classLoader) {
      hints.reflection().registerType(
          TypeReference.of("org.springframework.integration.config.ConverterRegistrar$IntegrationConverterRegistration"),
          MemberCategory.values()
      );
    }
  }
}
