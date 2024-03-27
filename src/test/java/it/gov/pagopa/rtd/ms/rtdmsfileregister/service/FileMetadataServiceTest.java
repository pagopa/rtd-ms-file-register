package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.AppConfiguration;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.RtdMsFileRegisterApplication;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter.FileChangedEventListener;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.DTOViolationException;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.EmptyFilenameException;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameAlreadyPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameNotPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events.FileChanged;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileApplication;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataEntity;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileStatus;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.SenderAdeAckListDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.FileMetadataRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@DataMongoTest
@ContextConfiguration(classes = {AppConfiguration.class, FileMetadataServiceImpl.class,
    RtdMsFileRegisterApplication.class})
@EntityScan("it.gov.pagopa.rtd.ms.rtdmsfileregister.model")
class FileMetadataServiceTest {

  @MockBean
  private FileChangedEventListener fileChangedEventListener;

  @MockBean
  private FileMetadataRepository fileMetadataRepository;

  @Autowired
  private FileMetadataServiceImpl service;

  private final ModelMapper modelMapper = new ModelMapper();

  private FileMetadataDTO testFileMetadataDTO;
  private FileMetadataEntity testFileMetadataEntity;
  private FileMetadataDTO newTestFileMetadataDTO;

  private FileMetadataEntity senderAdeACKFileMetadataDTO1;

  private FileMetadataEntity senderAdeACKFileMetadataDTO2;

  static String testFileMetadataJSON = "{\"name\":\"presentFilename\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"application\":0,\"size\":0,\"type\":0}";

  static String senderAdeACKFileMetadataJSON1 = "{\"name\":\"presentSenderADEACK1\",\"receiveTimestamp\":\"2020-08-06T12:19:16.500\",\"status\":0,\"application\":1,\"size\":0,\"type\":6}";

  static String senderAdeACKFileMetadataJSON2 = "{\"name\":\"presentSenderADEACK2\",\"receiveTimestamp\":\"2020-08-06T12:19:16.500\",\"status\":0,\"application\":1,\"size\":0,\"type\":6}";

  static String newTestFileMetadataJSON = "{\"name\":\"newFilename\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"hash\":\"090ed8c1103eb1dc4bae0ac2aa608fa5c085648438b7d38cfc238b9a98eba545\",\"status\":1,\"application\":0,\"size\":5555,\"type\":0}";

  private String metadataUpdatesJSON = "{\"name\":\"presentFilename\",\"container\": \"ade-decrypted\",\"status\":1,\"application\":0,\"size\":0,\"type\":0}";

  private String notPresentMetadataUpdatesJSON = "{\"name\":\"notPresentFilename\",\"container\": \"ade-decrypted\",\"status\":1,\"application\":0,\"size\":0,\"type\":0}";


  @BeforeEach
  public void setUpTest() throws JsonProcessingException {
    reset(fileMetadataRepository);

    ObjectMapper objectMapper = new ObjectMapper();

    testFileMetadataDTO = objectMapper.readValue(testFileMetadataJSON, FileMetadataDTO.class);

    testFileMetadataEntity = objectMapper.readValue(testFileMetadataJSON, FileMetadataEntity.class);

    newTestFileMetadataDTO = objectMapper.readValue(newTestFileMetadataJSON, FileMetadataDTO.class);

    senderAdeACKFileMetadataDTO1 = objectMapper.readValue(senderAdeACKFileMetadataJSON1,
        FileMetadataEntity.class);

    senderAdeACKFileMetadataDTO2 = objectMapper.readValue(senderAdeACKFileMetadataJSON2,
        FileMetadataEntity.class);

    List<FileMetadataEntity> senderAdeACKList = List.of(senderAdeACKFileMetadataDTO1,
        senderAdeACKFileMetadataDTO2);

    when(fileMetadataRepository.save(any(FileMetadataEntity.class)))
        .thenAnswer(invocation -> testFileMetadataEntity);

    when(fileMetadataRepository.findFirstByName("presentFilename"))
        .thenAnswer(invocation -> testFileMetadataEntity);

    when(fileMetadataRepository.removeByName("presentFilename"))
        .thenAnswer(invocation -> testFileMetadataEntity);

    when(fileMetadataRepository.findNamesBySenderAndTypeAndStatus("presentFilename", 6, 0))
        .thenAnswer(invocation -> senderAdeACKList);

  }

  @Test
  void store() {
    FileMetadataDTO stored = service.storeFileMetadata(newTestFileMetadataDTO);

    assertNotNull(stored);
    verify(fileMetadataRepository).findFirstByName(any(String.class));
    verify(fileMetadataRepository).save(any(FileMetadataEntity.class));
  }

  @Test
  void storeKoFileAlreadyPresent() {
    Exception exception = assertThrows(FilenameAlreadyPresent.class, () -> {
      FileMetadataDTO stored = service.storeFileMetadata(testFileMetadataDTO);
    });

    verify(fileMetadataRepository).findFirstByName(any(String.class));
  }

  @Test
  void retrieve() {
    FileMetadataDTO retrieved = service.retrieveFileMetadata("presentFilename");

    assertNotNull(retrieved);
    verify(fileMetadataRepository).findFirstByName(anyString());
  }

  @Test
  void retrieveKoNotPresent() {
    FileMetadataDTO retrieved = service.retrieveFileMetadata("notPresentFilename");

    assertNull(retrieved);
    verify(fileMetadataRepository).findFirstByName(anyString());
  }

  @Test
  void delete() {
    FileMetadataDTO deleted = service.deleteFileMetadata("presentFilename");

    assertNotNull(deleted);
    verify(fileMetadataRepository).removeByName(anyString());
  }

  @Test
  void deleteKo() {
    FileMetadataDTO deleted = service.deleteFileMetadata("notPresentFilename");

    assertNull(deleted);
    verify(fileMetadataRepository).removeByName(anyString());
  }

  @Test
  void update() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    FileMetadataDTO updated = service.updateFileMetadata(
        objectMapper.readValue(metadataUpdatesJSON, FileMetadataDTO.class));

    assertNotNull(updated);
    assertEquals(FileStatus.DOWNLOAD_STARTED.getOrder(), (int) updated.getStatus());
    verify(fileMetadataRepository).findFirstByName(anyString());
    verify(fileMetadataRepository).removeByName(anyString());
    verify(fileMetadataRepository).save(any(FileMetadataEntity.class));
  }

  @Test
  void updateKoNotPresent() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    FileMetadataDTO updated = service.updateFileMetadata(
        objectMapper.readValue(notPresentMetadataUpdatesJSON, FileMetadataDTO.class));

    assertNull(updated);
    verify(fileMetadataRepository).findFirstByName(anyString());
    verify(fileMetadataRepository, Mockito.times(0)).removeByName(anyString());
    verify(fileMetadataRepository, Mockito.times(0)).save(any(FileMetadataEntity.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"name\":,\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":0,\"size\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"wrong value\",\"status\":0,\"type\":0,\"application\":0,\"size\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":,\"type\":0,\"application\":0,\"size\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":\"wrong value\",\"type\":0,\"application\":0,\"size\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":,\"application\":0,\"size\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":\"wrong value\",\"application\":0,\"size\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":,\"size\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":\"wrong value\",\"size\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":0,\"size\":}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":0,\"size\":\"wrong value\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":0,\"size\":0,\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5a\"}",
  })
  void updateKoNonValidDTO(String body) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    //Assert true if Jackson cannot deserialize the JSON string
    FileMetadataDTO mapped;
    try {
      mapped = objectMapper.readValue(body, FileMetadataDTO.class);
    } catch (JsonMappingException | JsonParseException e) {
      assertTrue(true);
      return;
    }

    assertThrows(DTOViolationException.class, () -> {
      service.updateFileMetadata(mapped);
    });
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"container\": \"ade-decrypted\",\"status\":0,\"application\":0,\"size\":0,\"type\":0}",
      "{\"name\":\"\",\"container\": \"ade-decrypted\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":0,\"size\":0}",
  })
  void updateKoEmptyFilename(String body) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    FileMetadataDTO mapped = objectMapper.readValue(body, FileMetadataDTO.class);

    assertThrows(EmptyFilenameException.class, () -> {
      service.updateFileMetadata(mapped);
    });
  }

  @Test
  void getSenderAdeACK() {
    SenderAdeAckListDTO retrieved = service.getSenderAdeAckList(List.of("presentFilename"));
    assertNotNull(retrieved);
    assertEquals(2, retrieved.getFileNameList().size());
    verify(fileMetadataRepository, times(1)).findNamesBySenderAndTypeAndStatus(anyString(),
        anyInt(), anyInt());
  }

  @Test
  void getSenderAdeACKWithNullSender() {
    SenderAdeAckListDTO retrieved = service.getSenderAdeAckList(List.of());
    assertNotNull(retrieved);
    assertEquals(0, retrieved.getFileNameList().size());
    verify(fileMetadataRepository, times(0)).findNamesBySenderAndTypeAndStatus(anyString(),
        anyInt(), anyInt());
  }

  @Test
  void updateAfterSenderAdeAckDownload() {
    FileMetadataDTO result = service.updateStatus("presentFilename",
        FileStatus.DOWNLOAD_ENDED.getOrder());
    assertNotNull(result);
    assertEquals(FileStatus.DOWNLOAD_ENDED.getOrder(), result.getStatus());
  }

  @Test
  void updateAfterSenderAdeAckDownloadNullFilename() {
    int newStatus = FileStatus.DOWNLOAD_ENDED.getOrder();
    assertThrows(FilenameNotPresent.class,
        () -> service.updateStatus("missingFilename", newStatus));
  }

  @Test
  void updateSenderAdeAckWithStatusAlreadyPresent() {
    // the file status is already SUCCESS by setup
    FileMetadataDTO result = service.updateStatus("presentFilename",
        FileStatus.SUCCESS.getOrder());

    assertNotNull(result);
    assertEquals(FileStatus.SUCCESS.getOrder(), result.getStatus());
    Mockito.verify(fileMetadataRepository, times(0)).save(any());
  }

  @Nested
  class FireEventTests {

    @Test
    void whenFileIsUploadedThenFireReceivedEvent() {
      final var fileUploadEvent = mockFileEventMetadata(FileType.AGGREGATES_SOURCE,
          FileStatus.SUCCESS);

      service.storeFileMetadata(fileUploadEvent);
      Mockito.verify(fileChangedEventListener).handleFileChanged(
          new FileChanged(
              "/" + fileUploadEvent.getContainer() + "/" + fileUploadEvent.getParent(),
              fileUploadEvent.getSender(),
              fileUploadEvent.getSize(),
              fileUploadEvent.getReceiveTimestamp(),
              FileChanged.Type.RECEIVED
          )
      );
    }

    @Test
    void whenFileIsSplitThenFireDecryptedEvent() {
      final var mockedEntity = getMockedParent();
      when(fileMetadataRepository.findFirstByName(null)).thenReturn(mockedEntity);
      final var fileSplitEvent = mockFileEventMetadata(FileType.AGGREGATES_CHUNK,
          FileStatus.SUCCESS);

      service.storeFileMetadata(fileSplitEvent);
      Mockito.verify(fileChangedEventListener).handleFileChanged(
          new FileChanged(
              "/" + mockedEntity.getContainer() + "/" + fileSplitEvent.getParent(),
              fileSplitEvent.getSender(),
              fileSplitEvent.getSize(),
              fileSplitEvent.getReceiveTimestamp(),
              FileChanged.Type.DECRYPTED
          )
      );
    }

    @Test
    void givenNoParentFileWhenFileIsSplitThenFireDecryptedEvent() {
      when(fileMetadataRepository.findFirstByName(null)).thenReturn(null);
      final var fileSplitEvent = mockFileEventMetadata(FileType.AGGREGATES_CHUNK,
          FileStatus.SUCCESS);

      service.storeFileMetadata(fileSplitEvent);
      Mockito.verify(fileChangedEventListener).handleFileChanged(
          new FileChanged(
              "/" + fileSplitEvent.getContainer() + "/" + fileSplitEvent.getParent(),
              fileSplitEvent.getSender(),
              fileSplitEvent.getSize(),
              fileSplitEvent.getReceiveTimestamp(),
              FileChanged.Type.DECRYPTED
          )
      );
    }

    @Test
    void whenFileIsMovedToAdeThenFireSentToAdeEvent() {
      final var moveToAdeEvent = mockFileEventMetadata(FileType.AGGREGATES_DESTINATION,
          FileStatus.SUCCESS);

      service.storeFileMetadata(moveToAdeEvent);
      verify(fileChangedEventListener).handleFileChanged(
          new FileChanged(
              "/" + moveToAdeEvent.getContainer() + "/" + moveToAdeEvent.getParent(),
              moveToAdeEvent.getSender(),
              moveToAdeEvent.getSize(),
              moveToAdeEvent.getReceiveTimestamp(),
              FileChanged.Type.SENT_TO_ADE
          )
      );
    }

    @Test
    void whenAckIsReadyThenFireAckToDownloadEvent() {
      final var ackReadyEvent = mockFileEventMetadata(FileType.SENDER_ADE_ACK, FileStatus.SUCCESS);
      service.storeFileMetadata(ackReadyEvent);
      verify(fileChangedEventListener).handleFileChanged(
          new FileChanged(
              "/" + ackReadyEvent.getContainer() + "/" + ackReadyEvent.getParent(),
              ackReadyEvent.getSender(),
              ackReadyEvent.getSize(),
              ackReadyEvent.getReceiveTimestamp(),
              FileChanged.Type.ACK_TO_DOWNLOAD
          )
      );
    }

    @Test
    void whenAckIsImplicitAckedThenFireAckDownloadedEvent() {
      final var ackAckedEvent = mockFileEventMetadata(FileType.SENDER_ADE_ACK,
          FileStatus.DOWNLOAD_ENDED);
      when(fileMetadataRepository.findFirstByName("filename"))
          .thenReturn(modelMapper.map(ackAckedEvent, FileMetadataEntity.class));
      service.updateFileMetadata(ackAckedEvent);
      verify(fileChangedEventListener).handleFileChanged(
          new FileChanged(
              "/" + ackAckedEvent.getContainer() + "/" + ackAckedEvent.getParent(),
              ackAckedEvent.getSender(),
              ackAckedEvent.getSize(),
              ackAckedEvent.getReceiveTimestamp(),
              FileChanged.Type.ACK_DOWNLOADED
          )
      );
    }

    @Test
    void whenAckIsExplicitAckedThenFireAckDownloadEvent() {
      final var ackReadyEvent = mockFileEventMetadata(FileType.SENDER_ADE_ACK,
          FileStatus.DOWNLOAD_STARTED);
      final var ackAckedEvent = mockFileEventMetadata(FileType.SENDER_ADE_ACK,
          FileStatus.DOWNLOAD_ENDED);
      when(fileMetadataRepository.findFirstByName("filename"))
          .thenReturn(modelMapper.map(ackReadyEvent, FileMetadataEntity.class));
      service.updateStatus(ackAckedEvent.getName(), FileStatus.DOWNLOAD_ENDED.getOrder());
      verify(fileChangedEventListener).handleFileChanged(
          new FileChanged(
              "/" + ackAckedEvent.getContainer() + "/" + ackAckedEvent.getParent(),
              ackAckedEvent.getSender(),
              ackAckedEvent.getSize(),
              ackAckedEvent.getReceiveTimestamp(),
              FileChanged.Type.ACK_DOWNLOADED
          )
      );
    }

    @Test
    void whenAckIsExplicitAckedThenFireAckDownloadEventEvenIfItHasBeenAlreadyDownloaded() {
      final var ackAckedEvent = mockFileEventMetadata(FileType.SENDER_ADE_ACK,
          FileStatus.DOWNLOAD_ENDED);
      when(fileMetadataRepository.findFirstByName("filename"))
          .thenReturn(modelMapper.map(ackAckedEvent, FileMetadataEntity.class));

      service.updateStatus("filename", FileStatus.DOWNLOAD_ENDED.getOrder());

      verify(fileChangedEventListener).handleFileChanged(any());
    }

    @Test
    void whenNothingOfRelevantHappensThenNoFireEvent() {
      final var nothingEvent = mockFileEventMetadata(FileType.AGGREGATES_SOURCE, FileStatus.FAILED);
      service.storeFileMetadata(nothingEvent);
      verify(fileChangedEventListener, times(0)).handleFileChanged(any());
    }

    private FileMetadataDTO mockFileEventMetadata(FileType type, FileStatus status) {
      final var fileMetadataDTO = new FileMetadataDTO();
      fileMetadataDTO.setName("filename");
      fileMetadataDTO.setContainer("container");
      fileMetadataDTO.setType(type.getOrder());
      fileMetadataDTO.setStatus(status.getOrder());
      fileMetadataDTO.setReceiveTimestamp(LocalDateTime.now());
      fileMetadataDTO.setSender("12345");
      fileMetadataDTO.setParent("parent");
      fileMetadataDTO.setApplication(FileApplication.ADE.getOrder());
      fileMetadataDTO.setSize(1234L);
      when(fileMetadataRepository.save(any()))
          .thenReturn(modelMapper.map(fileMetadataDTO, FileMetadataEntity.class));
      return fileMetadataDTO;
    }
  }

  private FileMetadataEntity getMockedParent() {
    final var mockedEntity = new FileMetadataEntity();
    mockedEntity.setName("parentFilename");
    mockedEntity.setContainer("parentContainer");
    mockedEntity.setType(FileType.AGGREGATES_CHUNK.getOrder());
    mockedEntity.setStatus(FileStatus.SUCCESS.getOrder());
    mockedEntity.setReceiveTimestamp(LocalDateTime.now());
    mockedEntity.setSender("12345");
    mockedEntity.setParent("parent");
    mockedEntity.setApplication(FileApplication.ADE.getOrder());
    mockedEntity.setSize(1234L);
    return mockedEntity;
  }
}
