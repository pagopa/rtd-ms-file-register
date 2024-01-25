package it.gov.pagopa.rtd.ms.rtdmsfileregister.event;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter.BlobRegisterAdapter;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * Component defining the processing steps in response to storage events.
 */
@Configuration
@Getter
public class EventHandler {

  /**
   * Constructor.
   *
   * @return a consumer for Event Grid events.
   */
  @Bean
  public Consumer<Message<String>> blobStorageConsumer(BlobRegisterAdapter blobRegisterAdapter) {
    return (message) -> {
      System.out.println("test_graalvm_error: "+message);
    };
  }
}
