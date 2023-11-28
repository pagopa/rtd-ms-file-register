package it.gov.pagopa.rtd.ms.rtdmsfileregister.telemetry;


import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.KafkaTelemetry;
import io.opentelemetry.instrumentation.spring.kafka.v2_7.SpringKafkaTelemetry;
import org.springframework.cloud.stream.binder.BinderCustomizer;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.binder.kafka.config.ClientFactoryCustomizer;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

/**
 * The `SpringCloudKafkaBinderInstrumentation` class is responsible for configuring and customizing
 * Kafka-related components to use opentelemetry tracing.
 * <p>
 * The `binderCustomizer` method configures the Kafka binder to be traced with opentelemetry
 * by setting a custom client factory and by customizing the kafka container.
 * The `customizedKafkaClient` wrap the producer using opentelemetry instrumentation kafka library,
 * while `containerCustomizer` customize the container (consumer) using a spring kafka opentelemetry library
 * <p>
 * Overall, this class allows to observe and trace all Kafka interactions.
 */
public class SpringCloudKafkaBinderInstrumentation {

  @Bean
  public BinderCustomizer binderCustomizer(
      ApplicationContext context
  ) {
    return (binder, binderName) -> {
      if ((Object) binder instanceof KafkaMessageChannelBinder kafkaMessageChannelBinder) {
        kafkaMessageChannelBinder.setContainerCustomizer(
            context.getBean("containerCustomizer", ListenerContainerCustomizer.class)
        );
        kafkaMessageChannelBinder.addClientFactoryCustomizer(context.getBean(
            "customizedKafkaClient", ClientFactoryCustomizer.class
        ));
      }
    };
  }

  @Bean
  public ClientFactoryCustomizer customizedKafkaClient(OpenTelemetry openTelemetry) {
    final var kafkaClientTelemetry = KafkaTelemetry.builder(openTelemetry)
        .setCaptureExperimentalSpanAttributes(true)
        .setPropagationEnabled(true)
        .build();

    return new ClientFactoryCustomizer() {
      @Override
      public void configure(ProducerFactory<?, ?> pf) {
        pf.addPostProcessor(kafkaClientTelemetry::wrap);
      }
    };
  }

  @Bean
  public ListenerContainerCustomizer<AbstractMessageListenerContainer<?, ?>> containerCustomizer(
      OpenTelemetry openTelemetry
  ) {
    final var springKafkaTelemetry = SpringKafkaTelemetry.builder(openTelemetry)
        .setMessagingReceiveInstrumentationEnabled(true)
        .setCaptureExperimentalSpanAttributes(true)
        .build();
    return (container, destinationName, group) -> {
      container.setRecordInterceptor(springKafkaTelemetry.createRecordInterceptor());
      container.setBatchInterceptor(springKafkaTelemetry.createBatchInterceptor());
    };
  }
}
