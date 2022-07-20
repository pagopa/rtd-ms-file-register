package it.gov.pagopa.rtd.ms.rtdmsfileregister.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter.BlobRegisterAdapter;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridData;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

  ObjectMapper mapper = new ObjectMapper();

  String eventGridDataJson = "{\"api\": \"PutBlockList\",\n"
      + "    \"clientRequestId\": \"6d79dbfb-0e37-4fc4-981f-442c9ca65760\",\n"
      + "    \"requestId\": \"831e1650-001e-001b-66ab-eeb76e000000\",\n"
      + "    \"eTag\": \"\\\"0x8D4BCC2E4835CD0\\\"\",\n"
      + "    \"contentType\": \"text/plain\",\n"
      + "    \"contentLength\": 524288,\n"
      + "    \"blobType\": \"BlockBlob\",\n"
      + "    \"url\": \"https://my-storage-account.blob.core.windows.net/testcontainer/new-file.txt\",\n"
      + "    \"sequencer\": \"00000000000004420000000000028963\",\n"
      + "    \"storageDiagnostics\": {\n"
      + "       \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\""
      + "     }"
      + "}";

  String eventGridDataJsonNoContentLength = "{\"api\": \"PutBlockList\",\n"
      + "    \"clientRequestId\": \"6d79dbfb-0e37-4fc4-981f-442c9ca65760\",\n"
      + "    \"requestId\": \"831e1650-001e-001b-66ab-eeb76e000000\",\n"
      + "    \"eTag\": \"\\\"0x8D4BCC2E4835CD0\\\"\",\n"
      + "    \"contentType\": \"text/plain\",\n"
      + "    \"blobType\": \"BlockBlob\",\n"
      + "    \"url\": \"https://my-storage-account.blob.core.windows.net/testcontainer/new-file.txt\",\n"
      + "    \"sequencer\": \"00000000000004420000000000028963\",\n"
      + "    \"storageDiagnostics\": {\n"
      + "       \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\""
      + "     }"
      + "}";

  String eventGridDataJsonNegativeContentLength = "{\"api\": \"PutBlockList\",\n"
      + "    \"clientRequestId\": \"6d79dbfb-0e37-4fc4-981f-442c9ca65760\",\n"
      + "    \"requestId\": \"831e1650-001e-001b-66ab-eeb76e000000\",\n"
      + "    \"eTag\": \"\\\"0x8D4BCC2E4835CD0\\\"\",\n"
      + "    \"contentType\": \"text/plain\",\n"
      + "    \"contentLength\": -1,\n"
      + "    \"blobType\": \"BlockBlob\",\n"
      + "    \"url\": \"https://my-storage-account.blob.core.windows.net/testcontainer/new-file.txt\",\n"
      + "    \"sequencer\": \"00000000000004420000000000028963\",\n"
      + "    \"storageDiagnostics\": {\n"
      + "       \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\""
      + "     }"
      + "}";

  String eventGridDataJsonMalformedContent = "{\"api\": \"PutBlockList\",\n"
      + "    \"clientRequestId\": \"6d79dbfb-0e37-4fc4-981f-442c9ca65760\",\n"
      + "    \"requestId\": \"831e1650-001e-001b-66ab-eeb76e000000\",\n"
      + "    \"eTag\": \"\\\"0x8D4BCC2E4835CD0\\\"\",\n"
      + "    \"contentType\": \"text/plain\",\n"
      + "    \"blobType\": \"BlockBlob\",\n"
      + "    \"url\": \"https://my-storage-account.blob.core.windows.net/testcontainer/new-file.txt\",\n"
      + "    \"sequencer\": \"00000000000004420000000000028963\",\n"
      + "    \"storageDiagnostics\": {\n"
      + "       \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\""
      + "     }"
      + "}";

  @BeforeEach
  void setUp() {
    when(fileMetadataService.storeFileMetadata(any())).thenAnswer(i -> i.getArguments()[0]);

    myEvent = new EventGridEvent();
    myEvent.setId(myId);
    myEvent.setTopic(myTopic);
    myEvent.setEventType(myEventType);

    EventGridData eventGridData = null;
    try {
      eventGridData = mapper.readValue(eventGridDataJson, EventGridData.class);
    } catch (JsonProcessingException ex) {
      ex.printStackTrace();
    }
    myEvent.setData(eventGridData);


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
      "ade/in, ADE.99999.TRNLOG.20220503.172038.001.csv.pgp.0.decrypted.gz",
      "ade/ack, CSTAR.ADEACK.20220503.172038.001.csv",
      "sender-ade-ack, ADE.99999.ADEACK.20220607.163518.001.csv",
  })
  void consumeEvent(String container, String blob) {
    String uri = "/blobServices/default/containers/" + container + "/blobs/" + blob;

    myEvent.setSubject(uri);
System.err.println(myEvent.getData().getContentLength());
    myList = List.of(myEvent);

    stream.send("blobStorageConsumer-in-0", MessageBuilder.withPayload(myList).build());
    verify(blobRegisterAdapter, times(1)).evaluateEvent(any());
    verify(blobRegisterAdapter, times(1)).evaluateContainer(any());
    verify(blobRegisterAdapter, times(1)).evaluateApplication(any());
    verify(blobRegisterAdapter, times(1)).extractParent(any(), any());
    verify(blobRegisterAdapter, times(1)).extractSender(any(), any());
  }

  @Test
  void consumeEventNoSize() {
    String uri = "/blobServices/default/containers/rtd-transactions-32489876908u74bh781e2db57k098c5ad00000000000/blobs/CSTAR.99999.TRNLOG.20220419.121045.001.csv.pgp";

    EventGridData eventGridData = null;
    try {
      eventGridData = mapper.readValue(eventGridDataJsonNoContentLength, EventGridData.class);
    } catch (JsonProcessingException ex) {
      ex.printStackTrace();
    }
    myEvent.setData(eventGridData);

    myEvent.setSubject(uri);

    myList = List.of(myEvent);

    stream.send("blobStorageConsumer-in-0", MessageBuilder.withPayload(myList).build());
    verify(blobRegisterAdapter, times(1)).evaluateEvent(any());
    verify(blobRegisterAdapter, times(1)).evaluateContainer(any());
    verify(blobRegisterAdapter, times(1)).evaluateApplication(any());
    verify(blobRegisterAdapter, times(1)).extractParent(any(), any());
    verify(blobRegisterAdapter, times(1)).extractSender(any(), any());

  }

  @Test
  void consumeEventNegativeSize() {
    String uri = "/blobServices/default/containers/rtd-transactions-32489876908u74bh781e2db57k098c5ad00000000000/blobs/CSTAR.99999.TRNLOG.20220419.121045.001.csv.pgp";

    EventGridData eventGridData = null;
    try {
      eventGridData = mapper.readValue(eventGridDataJsonNegativeContentLength, EventGridData.class);
    } catch (JsonProcessingException ex) {
      ex.printStackTrace();
    }
    myEvent.setData(eventGridData);

    myEvent.setSubject(uri);

    myList = List.of(myEvent);

    stream.send("blobStorageConsumer-in-0", MessageBuilder.withPayload(myList).build());
    verify(blobRegisterAdapter, times(1)).evaluateEvent(any());
    verify(blobRegisterAdapter, times(1)).evaluateContainer(any());
    verify(blobRegisterAdapter, times(1)).evaluateApplication(any());
    verify(blobRegisterAdapter, times(1)).extractParent(any(), any());
    verify(blobRegisterAdapter, times(1)).extractSender(any(), any());

  }

  @Test
  void consumeEventMalformedEventData() {
    String uri = "/blobServices/default/containers/rtd-transactions-32489876908u74bh781e2db57k098c5ad00000000000/blobs/CSTAR.99999.TRNLOG.20220419.121045.001.csv.pgp";

    EventGridData eventGridData = null;
    try {
      eventGridData = mapper.readValue(eventGridDataJsonMalformedContent, EventGridData.class);
    } catch (JsonProcessingException ex) {
      ex.printStackTrace();
    }
    myEvent.setData(eventGridData);

    myEvent.setSubject(uri);

    myList = List.of(myEvent);

    stream.send("blobStorageConsumer-in-0", MessageBuilder.withPayload(myList).build());
    verify(blobRegisterAdapter, times(1)).evaluateEvent(any());
    verify(blobRegisterAdapter, times(1)).evaluateContainer(any());
    verify(blobRegisterAdapter, times(1)).evaluateApplication(any());
    verify(blobRegisterAdapter, times(1)).extractParent(any(), any());
    verify(blobRegisterAdapter, times(1)).extractSender(any(), any());
  }

  @ParameterizedTest
  @CsvSource({"bpd-terms-and-conditions, bpd-tc.pdf",
      "cstar-exports, hashedPans.zip",
      "fa-terms-and-conditions, fa-tc.pdf",
      "info-privacy, info-privacy.pdf"})
  void notConsumeEvent(String container, String blob) {
    String uri = "/blobServices/default/containers/" + container + "/blobs/" + blob;

    myEvent.setSubject(uri);

    myList = List.of(myEvent);

    stream.send("blobStorageConsumer-in-0", MessageBuilder.withPayload(myList).build());
    verify(blobRegisterAdapter, times(1)).evaluateEvent(any());
    verify(blobRegisterAdapter, times(1)).evaluateContainer(any());
    verify(blobRegisterAdapter, never()).evaluateApplication(any());
    verify(blobRegisterAdapter, never()).extractParent(any(), any());
    verify(blobRegisterAdapter, never()).extractSender(any(), any());

  }

}
