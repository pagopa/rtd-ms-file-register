package it.gov.pagopa.rtd.ms.rtdmsfileregister;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.DefaultNamingPolicy;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.NamingConventionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

  @Bean
  NamingConventionPolicy namingConventionPolicy() {
    return new DefaultNamingPolicy();
  }
}
