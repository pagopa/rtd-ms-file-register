package it.gov.pagopa.rtd.ms.rtdmsfileregister.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EmbeddedKafka(topics = {"rtd-platform-events", "rtd-trx"}, partitions = 1,
    bootstrapServersProperty = "spring.embedded.kafka.brokers")
@EnableAutoConfiguration(exclude = {TestSupportBinderAutoConfiguration.class})
//@ContextConfiguration(classes = {EventHandler.class})
@TestPropertySource(value = {"classpath:application-test.yml"}, inheritProperties = false)
@DirtiesContext
@ExtendWith(OutputCaptureExtension.class)
class EventHandlerTest {

  @Autowired
  private StreamBridge stream;

  private final String container = "rtd-transactions-decrypted";
  private final String blob = "CSTAR.99999.TRNLOG.20220316.103107.001.csv.pgp";
  private final String blobUri = "/blobServices/default/containers/" + container + "/blobs/" + blob;

  private final String myId = "myId";
  private final String myTopic = "myTopic";
  private final String myEventType = "Microsoft.Storage.BlobCreated";

  List<EventGridEvent> myList;
  EventGridEvent myEvent;

  ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

  Validator validator = factory.getValidator();

  @BeforeEach
  void setUp() {
    myEvent = new EventGridEvent();
    myEvent.setId(myId);
    myEvent.setTopic(myTopic);
    myEvent.setSubject(blobUri);
    myEvent.setEventType(myEventType);

    OffsetDateTime off = OffsetDateTime.parse("2020-08-06T12:19:16.500+03:00");
    ZonedDateTime zoned = off.atZoneSameInstant(ZoneId.of("Europe/Rome"));
    LocalDateTime localDateTime = zoned.toLocalDateTime();
    myEvent.setEventTime(localDateTime);

    myList = List.of(myEvent);
  }

  @Test
  void failInstantiateEvent(CapturedOutput output) {
    stream.send("blobStorageConsumer-in-0", MessageBuilder.withPayload(myList).build());
    assertThat(output.getOut(), containsString("Received event: "));
  }

}
