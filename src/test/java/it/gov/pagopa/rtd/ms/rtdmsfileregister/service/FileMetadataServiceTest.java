package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.RtdMsFileRegisterApplication;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.DTOViolationException;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.EmptyFilenameException;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameAlreadyPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataEntity;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.FileMetadataRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@DataMongoTest
@ContextConfiguration(classes = {FileMetadataServiceImpl.class, RtdMsFileRegisterApplication.class})
@EntityScan("it.gov.pagopa.rtd.ms.rtdmsfileregister.model")
class FileMetadataServiceTest {

  @MockBean
  private FileMetadataRepository fileMetadataRepository;

  @Autowired
  private FileMetadataServiceImpl service;

  private FileMetadataDTO testFileMetadataDTO;
  private FileMetadataEntity testFileMetadataEntity;
  private FileMetadataDTO newTestFileMetadataDTO;

  static String testFileMetadataJSON = "{\"name\":\"presentFilename\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTransactions\":5,\"numAggregates\":2,\"amountAggregates\":900,\"amountTransactions\":700,\"chunksTotal\":5,\"status\":0}";

  static String newTestFileMetadataJSON = "{\"name\":\"newFilename\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T12:19:16.500\",\"hash\":\"090ed8c1103eb1dc4bae0ac2aa608fa5c085648438b7d38cfc238b9a98eba545\",\"numTransactions\":15,\"numAggregates\":12,\"amountAggregates\":1900,\"amountTransactions\":1700,\"chunksTotal\":15,\"status\":1}";

  private String metadataUpdatesJSON = "{\"name\":\"presentFilename\", \"status\":1}";

  private String notPresentMetadataUpdatesJSON = "{\"name\":\"notPresentFilename\", \"status\":1}";

  @BeforeEach
  public void setUpTest() throws JsonProcessingException {
    reset(fileMetadataRepository);

    ObjectMapper objectMapper = new ObjectMapper();

    testFileMetadataDTO = objectMapper.readValue(testFileMetadataJSON, FileMetadataDTO.class);

    testFileMetadataEntity = objectMapper.readValue(testFileMetadataJSON, FileMetadataEntity.class);

    newTestFileMetadataDTO = objectMapper.readValue(newTestFileMetadataJSON, FileMetadataDTO.class);

    when(fileMetadataRepository.save(any(FileMetadataEntity.class))).thenAnswer(invocation -> {
      return testFileMetadataEntity;
    });

    when(fileMetadataRepository.findFirstByName("presentFilename")).thenAnswer(invocation -> {
      return testFileMetadataEntity;
    });

    when(fileMetadataRepository.removeByName("presentFilename")).thenAnswer(invocation -> {
      return testFileMetadataEntity;
    });

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
    assertEquals(1, (int) updated.getStatus());
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
      "{\"name\":\"test0\",\"receiveTimestamp\":\"wrong value\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"wrong value\",\"status\":0}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":\"wrong value\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"sender\":\"1234\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"sender\":\"123456\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5a\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"chunksTotal\":\"wrong value\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"chunksTotal\":-1}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"chunksLeft\":\"wrong value\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"chunksLeft\":-1}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"numTransactions\":\"wrong value\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"numTransactions\":-1}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"amountTransactions\":\"wrong value\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"amountTransactions\":-1}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"numAggregates\":\"wrong value\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"numAggregates\":-1}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"amountAggregates\":\"wrong value\"}",
      "{\"name\":\"test0\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"lastTransitionTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"amountAggregates\":-1}",
  })
  void updateKoNonValidDTO(String body) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    //Assert true if Jackson cannot deserialize the JSON string
    FileMetadataDTO mapped;
    try {
      mapped = objectMapper.readValue(body, FileMetadataDTO.class);
    } catch (JsonMappingException e) {
      Assertions.assertTrue(true);
      return;
    }

    assertThrows(DTOViolationException.class, () -> {
      service.updateFileMetadata(mapped);
    });
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTransactions\":\"1\",\"numAggregates\":2,\"amountAggregates\":900,\"amountTransactions\":700,\"chunksTotal\":5,\"status\":0}",
      "{\"name\":\"\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTransactions\":\"1\",\"numAggregates\":2,\"amountAggregates\":900,\"amountTransactions\":700,\"chunksTotal\":5,\"status\":0}",
  })
  void updateKoEmptyFilename(String body) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    FileMetadataDTO mapped = objectMapper.readValue(body, FileMetadataDTO.class);

    assertThrows(EmptyFilenameException.class, () -> {
      service.updateFileMetadata(mapped);
    });
  }
}
