package it.gov.pagopa.rtd.ms.rtdmsfileregister.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter.BlobRegisterAdapter;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(topics = {"rtd-platform-events"}, partitions = 1,
    bootstrapServersProperty = "spring.embedded.kafka.brokers")
@EnableAutoConfiguration(exclude = {TestSupportBinderAutoConfiguration.class})
@ContextConfiguration(classes = {EventHandler.class})
@TestPropertySource(value = {"classpath:application-test.yml"}, inheritProperties = false)
@DirtiesContext
@ExtendWith(OutputCaptureExtension.class)
class EventHandlerTest {

  @Autowired
  private StreamBridge stream;

  @MockBean
  FileMetadataService fileMetadataService;

  @SpyBean
  BlobRegisterAdapter blobRegisterAdapter;

  private final String myId = "myId";
  private final String myTopic = "myTopic";
  private final String myEventType = "Microsoft.Storage.BlobCreated";

  List<EventGridEvent> myList;
  EventGridEvent myEvent;

  @BeforeEach
  void setUp() {
    when(fileMetadataService.storeFileMetadata(any())).thenAnswer(i -> i.getArguments()[0]);

    myEvent = new EventGridEvent();
    myEvent.setId(myId);
    myEvent.setTopic(myTopic);
    myEvent.setEventType(myEventType);

    OffsetDateTime off = OffsetDateTime.parse("2020-08-06T12:19:16.500+03:00");
    ZonedDateTime zoned = off.atZoneSameInstant(ZoneId.of("Europe/Rome"));
    LocalDateTime localDateTime = zoned.toLocalDateTime();
    myEvent.setEventTime(localDateTime);

  }

  @ParameterizedTest
  @CsvSource({
      "rtd-transactions-32489876908u74bh781e2db57k098c5ad00000000000, CSTAR.99999.TRNLOG.20220419.121045.001.csv.pgp",
      "rtd-transactions-decrypted, CSTAR.99999.TRNLOG.20220419.121045.001.csv.pgp.0.decrypted",
      "ade-transactions-32489876908u74bh781e2db57k098c5ad00000000000, ADE.99999.TRNLOG.20220503.172038.001.csv.pgp",
      "ade-transactions-decrypted, ADE.99999.TRNLOG.20220503.172038.001.csv.pgp.0.decrypted",
      "ade/in, ADE.99999.TRNLOG.20220503.172038.001.csv.pgp.0.decrypted",
      "ade/ack, CSTAR.ADEACK.20220503.172038.001.csv.pgp.0.decrypted",
      "sender-ade-ack, ADE.99999.ADEACK.20220607.163518.001.csv",
  })
  void consumeEvent(String container, String blob) {
    String uri = "/blobServices/default/containers/" + container+ "/blobs/" + blob;

    myEvent.setSubject(uri);

    myList = List.of(myEvent);

    stream.send("blobStorageConsumer-in-0", MessageBuilder.withPayload(myList).build());
    verify(blobRegisterAdapter, times(1)).validateContainer(any());
    verify(blobRegisterAdapter, times(1)).evaluateEvent(any());
  }

  @ParameterizedTest
  @CsvSource({"bpd-terms-and-conditions, bpd-tc.pdf",
      "cstar-exports, hashedPans.zip",
      "fa-terms-and-conditions, fa-tc.pdf",
      "info-privacy, info-privacy.pdf"})
  void notConsumeEvent(String container, String blob) {
    String uri = "/blobServices/default/containers/" + container+ "/blobs/" + blob;

    myEvent.setSubject(uri);

    myList = List.of(myEvent);

    stream.send("blobStorageConsumer-in-0", MessageBuilder.withPayload(myList).build());
    verify(blobRegisterAdapter, times(1)).validateContainer(any());
    verify(blobRegisterAdapter, never()).evaluateEvent(any());
  }

}
