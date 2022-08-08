package it.gov.pagopa.rtd.ms.rtdmsfileregister.event;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter.BlobRegisterAdapter;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * Component defining the processing steps in response to storage events.
 */
@Slf4j
@Configuration
@Getter
public class EventHandler {

  /**
   * Constructor.
   *
   * @return a consumer for Event Grid events.
   */
  @Bean
  public Consumer<Message<List<EventGridEvent>>> blobStorageConsumer(BlobRegisterAdapter blobRegisterAdapter) {
    return message -> message.getPayload().stream()
//        .filter(e -> "Microsoft.Storage.BlobCreated".equals(e.getEventType()))
        .map(blobRegisterAdapter::evaluateEvent)
        .collect(Collectors.toList());
  }
}