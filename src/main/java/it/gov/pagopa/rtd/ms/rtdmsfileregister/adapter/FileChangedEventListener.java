package it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events.FileChanged;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

@Slf4j
public class FileChangedEventListener {

  private final StreamBridge streamBridge;
  private final String bindingName;

  public FileChangedEventListener(StreamBridge streamBridge, String bindingName) {
    this.streamBridge = streamBridge;
    this.bindingName = bindingName;
  }

  /**
   * Declared just to let know Spring to connect the producer at startup
   */
  @Configuration
  static class ProjectorProducerConfig {

    @Bean
    public Supplier<Message<Object>> fileRegisterProjector() {
      return () -> null;
    }
  }

  @EventListener
  public void handleFileChanged(FileChanged event) {
    log.info("Firing file changed event {}", event);
    streamBridge.send(bindingName, MessageBuilder.withPayload(event).build());
    log.info("File changed event sent");
  }
}
