package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameNotPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.StatusAlreadySet;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataEntity;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileStatus;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.SenderAdeAckListDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(RestControllerImpl.class)
@Slf4j
class RestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FileMetadataService fileMetadataService;

  @SpyBean
  RestControllerImpl restController;

  private final ModelMapper modelMapper = new ModelMapper();

  protected ObjectMapper objectMapper = new ObjectMapper();

  static String BASE_URI = "http://localhost:8080";

  static String METADATA_ENDPOINT = "/file-status";

  static String SENDER_ADE_ACK_ENDPOINT = "/sender-ade-ack";

  static String TEST_FILE_METADATA = "{\"name\":\"presentFilename\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"application\":0,\"size\":0,\"type\":0}";

  static String UPDATED_TEST_FILE_METADATA = "{\"name\":\"presentFilename\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"status\":1}";

  static String ACKED_TEST_FILE_METADATA = "{\"name\":\"presentFilename\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"status\":1}";

  static String UPDATE_FILE_METADATA = "{\"name\":\"presentFilename\",\"status\":5}";

  static String MALFORMED_UPDATE_FILE_METADATA = "{\"name\":\"\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"status\":1}";

  static String senderAdeAckFileName1 = "ADEACK.99999.12345.2022-09-13.709f29ed-2a34-4c73-9a23-397e2e768ecf.csv";

  static String senderAdeAckFileName2 = "ADEACK.55555.12345.2022-09-13.709f29ed-2a34-4c73-9a23-397e2e768ecf.csv";

  static String senderAdeACKFileMetadataJSON1 = "{\"name\":\"" + senderAdeAckFileName1
      + "\",\"receiveTimestamp\":\"2020-08-06T12:19:16.500\",\"status\":0,\"application\":1,\"size\":0,\"type\":6,\"sender\":99999}";

  static String senderAdeACKFileMetadataJSON2 = "{\"name\":\"" + senderAdeAckFileName2
      + "\",\"receiveTimestamp\":\"2020-08-06T12:19:16.500\",\"status\":0,\"application\":1,\"size\":0,\"type\":6,\"sender\":55555}";

  static FileMetadataDTO testFileMetadataDTO;

  static FileMetadataDTO updateFileMetadataDTO;
  static FileMetadataDTO updatedTestFileMetadataDTO;
  static FileMetadataDTO ackedTestFileMetadataDTO;
  static FileMetadataDTO malformedUpdateTestFileMetadataDTO;

  private FileMetadataEntity senderAdeACKFileMetadataEntity1;

  private FileMetadataEntity senderAdeACKFileMetadataEntity2;

  @PostConstruct
  public void configureTest() throws JsonProcessingException {

    testFileMetadataDTO = objectMapper.readValue(TEST_FILE_METADATA, FileMetadataDTO.class);

    updateFileMetadataDTO = objectMapper.readValue(UPDATE_FILE_METADATA, FileMetadataDTO.class);

    updatedTestFileMetadataDTO = objectMapper.readValue(UPDATED_TEST_FILE_METADATA,
        FileMetadataDTO.class);

    ackedTestFileMetadataDTO = objectMapper.readValue(ACKED_TEST_FILE_METADATA,
        FileMetadataDTO.class);

    malformedUpdateTestFileMetadataDTO = objectMapper.readValue(MALFORMED_UPDATE_FILE_METADATA,
        FileMetadataDTO.class);

    senderAdeACKFileMetadataEntity1 = objectMapper.readValue(senderAdeACKFileMetadataJSON1,
        FileMetadataEntity.class);

    senderAdeACKFileMetadataEntity2 = objectMapper.readValue(senderAdeACKFileMetadataJSON2,
        FileMetadataEntity.class);

    SenderAdeAckListDTO senderAdeACKList = new SenderAdeAckListDTO(
        List.of(senderAdeACKFileMetadataEntity1.getName(),
            senderAdeACKFileMetadataEntity2.getName()));

    BDDMockito.doReturn(testFileMetadataDTO).when(fileMetadataService)
        .retrieveFileMetadata("presentFilename");

    BDDMockito.doReturn(testFileMetadataDTO).when(fileMetadataService)
        .storeFileMetadata(testFileMetadataDTO);

    BDDMockito.doReturn(testFileMetadataDTO).when(fileMetadataService)
        .storeFileMetadata(Mockito.any(FileMetadataDTO.class));

    BDDMockito.doReturn(testFileMetadataDTO).when(fileMetadataService)
        .deleteFileMetadata("presentFilename");

    BDDMockito.doReturn(testFileMetadataDTO).when(fileMetadataService)
        .deleteFileMetadata("presentFilename");

    BDDMockito.doReturn(null).when(fileMetadataService)
        .updateFileMetadata(malformedUpdateTestFileMetadataDTO);

    BDDMockito.doReturn(updatedTestFileMetadataDTO).when(fileMetadataService)
        .updateFileMetadata(updateFileMetadataDTO);

    BDDMockito.doReturn(senderAdeACKList).when(fileMetadataService)
        .getSenderAdeAckList(Arrays.asList("99999", "11111"));

    BDDMockito.doReturn(ackedTestFileMetadataDTO).when(fileMetadataService)
        .updateStatus("presentFilename", 1);

    BDDMockito.doThrow(FilenameNotPresent.class).when(fileMetadataService)
        .updateStatus("notPresentFilename", 1);

    BDDMockito.doThrow(StatusAlreadySet.class).when(fileMetadataService)
        .updateStatus("alreadyDownloadedFilename", 1);

    BDDMockito.doReturn(modelMapper.map(senderAdeACKFileMetadataEntity1, FileMetadataDTO.class))
        .when(fileMetadataService)
        .updateStatus("notUpdatedFilename", 1);
  }

  @Test
  void shouldGetMetadata() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .get(BASE_URI + METADATA_ENDPOINT)
            .param("filename", "presentFilename")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    FileMetadataDTO justPut = objectMapper.readValue(result.getResponse().getContentAsString(),
        FileMetadataDTO.class);

    assertEquals(testFileMetadataDTO, justPut);
    BDDMockito.verify(fileMetadataService).retrieveFileMetadata(Mockito.any(String.class));
  }

  @Test
  void shouldNotGetMetadataFileNotPresent() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .get(BASE_URI + METADATA_ENDPOINT)
            .param("filename", "notPresentFileName")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andReturn();

    BDDMockito.verify(fileMetadataService, Mockito.times(1))
        .retrieveFileMetadata(Mockito.any(String.class));
  }

  @Test
  void shouldNotGetNullFilename() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .get(BASE_URI + METADATA_ENDPOINT)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andReturn();

    assertEquals("", result.getResponse().getContentAsString());
    BDDMockito.verify(fileMetadataService, Mockito.times(0))
        .storeFileMetadata(Mockito.any(FileMetadataDTO.class));
  }

  @Test
  void shouldPost() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .post(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TEST_FILE_METADATA)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    FileMetadataDTO justPut = objectMapper.readValue(result.getResponse().getContentAsString(),
        FileMetadataDTO.class);

    assertEquals(testFileMetadataDTO, justPut);
    BDDMockito.verify(fileMetadataService).storeFileMetadata(Mockito.any(FileMetadataDTO.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "{\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":0,\"size\":0}",
      "{\"name\":,\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":0,\"size\":0}",
      "{\"name\":\"\",\"receiveTimestamp\":\"2020-08-06T11:19:16.500\",\"status\":0,\"type\":0,\"application\":0,\"size\":0}",
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
  void shouldNotPostWrongValue(String body) throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .post(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(body)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldDelete() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .delete(BASE_URI + METADATA_ENDPOINT)
            .param("filename", "presentFilename")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    FileMetadataDTO justDeleted = objectMapper.readValue(result.getResponse().getContentAsString(),
        FileMetadataDTO.class);

    assertEquals(testFileMetadataDTO, justDeleted);
    BDDMockito.verify(fileMetadataService).deleteFileMetadata(Mockito.any(String.class));
  }

  @Test
  void shouldNotDeleteFileNotPresent() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .delete(BASE_URI + METADATA_ENDPOINT)
            .param("filename", "notPresentFileName")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andReturn();

    assertEquals("", result.getResponse().getContentAsString());
    BDDMockito.verify(fileMetadataService, Mockito.times(1))
        .deleteFileMetadata(Mockito.any(String.class));
  }

  @Test
  void shouldNotDeleteNullFilename() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .delete(BASE_URI + METADATA_ENDPOINT)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest());

    BDDMockito.verify(fileMetadataService, Mockito.times(0))
        .deleteFileMetadata(Mockito.any(String.class));
  }

  @Test
  void shouldUpdate() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + METADATA_ENDPOINT)
            .content(objectMapper.writeValueAsString(updateFileMetadataDTO))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    FileMetadataDTO justUpdated = objectMapper.readValue(result.getResponse().getContentAsString(),
        FileMetadataDTO.class);

    assertEquals(updatedTestFileMetadataDTO, justUpdated);
    BDDMockito.verify(fileMetadataService)
        .updateFileMetadata(Mockito.any(FileMetadataDTO.class));
  }

  @Test
  void shouldNotUpdateMissingContentType() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + METADATA_ENDPOINT)
            .content(objectMapper.writeValueAsString(updateFileMetadataDTO))
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void shouldNotUpdateMissingBody() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldNotUpdateMissingFilename() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(malformedUpdateTestFileMetadataDTO))
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldGetSenderAdeAck() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .get(BASE_URI + SENDER_ADE_ACK_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param("senders", "99999", "11111")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    assertEquals(
        "{\"fileNameList\":[\"" + senderAdeAckFileName1 + "\",\"" + senderAdeAckFileName2 + "\"]}",
        result.getResponse().getContentAsString());

    BDDMockito.verify(fileMetadataService, Mockito.times(1))
        .getSenderAdeAckList(Mockito.any(List.class));
  }

  @Test
  void shouldAckSenderAdEAck() throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + SENDER_ADE_ACK_ENDPOINT)
            .param("filename", "presentFilename")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    FileMetadataDTO acked = objectMapper.readValue(result.getResponse().getContentAsString(),
        FileMetadataDTO.class);

    assertEquals(FileStatus.DOWNLOADED.getOrder(), acked.getStatus());
    assertEquals(ackedTestFileMetadataDTO, acked);
    BDDMockito.verify(fileMetadataService)
        .updateStatus(Mockito.any(String.class), Mockito.any(Integer.class));
  }

  @Test
  void shouldNotAckSenderAdEAckMissingFilename() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + SENDER_ADE_ACK_ENDPOINT)
            .param("filename", "notPresentFilename")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldNotAckSenderAdEAckStatusAlreadyDownloaded() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + SENDER_ADE_ACK_ENDPOINT)
            .param("filename", "notUpdatedFilename")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().is5xxServerError());
  }

}
