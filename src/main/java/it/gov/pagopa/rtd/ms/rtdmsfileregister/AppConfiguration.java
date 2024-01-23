package it.gov.pagopa.rtd.ms.rtdmsfileregister;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.AppConfiguration.CustomRuntimeHints;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter.FileChangedEventListener;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.DefaultNamingPolicy;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.NamingConventionPolicy;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events.FileChangedFactory;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(CustomRuntimeHints.class)
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


  // Probably due to an instability of snapshot version some reflection data are
  // missing,
  // this register the missing class.
  public static class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(org.springframework.aot.hint.RuntimeHints hints,
        ClassLoader classLoader) {
      hints.reflection().registerType(
          TypeReference
              .of("org.springframework.integration.config.ConverterRegistrar$IntegrationConverterRegistration"),
          MemberCategory.values());
    }
  }
}
