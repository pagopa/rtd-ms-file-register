package it.gov.pagopa.rtd.ms.rtdmsfileregister;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter.FileChangedEventListener;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.DefaultNamingPolicy;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.NamingConventionPolicy;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events.FileChangedFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

  private static final String PROJECTOR_BINDING_NAME = "fileRegisterProjector-out-0";

  @Bean
  NamingConventionPolicy namingConventionPolicy() {
    return new DefaultNamingPolicy();
  }

  @Bean
  FileChangedFactory fileChangedFactory() {
    return FileChangedFactory.typeStatusBased();
  }

  @Bean
  FileChangedEventListener fileChangedEventListener(StreamBridge streamBridge) {
    return new FileChangedEventListener(streamBridge, PROJECTOR_BINDING_NAME);
  }

}
