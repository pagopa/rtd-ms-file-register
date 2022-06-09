package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

  static String TEST_FILE_METADATA = "{\"name\":\"presentFilename\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}";

  static String UPDATED_TEST_FILE_METADATA = "{\"name\":\"presentFilename\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":1}";

  static String UPDATE_FILE_METADATA = "{\"name\":\"presentFilename\",\"status\":5}";

  static String MALFORMED_UPDATE_FILE_METADATA = "{\"name\":\"\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":1}";

  static FileMetadataDTO testFileMetadataDTO;

  static FileMetadataDTO updateFileMetadataDTO;
  static FileMetadataDTO updatedTestFileMetadataDTO;
  static FileMetadataDTO malformedUpdateTestFileMetadataDTO;

  @PostConstruct
  public void configureTest() throws JsonProcessingException {

    testFileMetadataDTO = objectMapper.readValue(TEST_FILE_METADATA, FileMetadataDTO.class);

    updateFileMetadataDTO = objectMapper.readValue(UPDATE_FILE_METADATA, FileMetadataDTO.class);

    updatedTestFileMetadataDTO = objectMapper.readValue(UPDATED_TEST_FILE_METADATA,
        FileMetadataDTO.class);

    malformedUpdateTestFileMetadataDTO = objectMapper.readValue(MALFORMED_UPDATE_FILE_METADATA,
        FileMetadataDTO.class);

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
      "{\"name\":\"test0\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":\"wrong value\",\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":\"wrong value\",\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":\"wrong value\",\"amountRtd\":700,\"numChunks\":5,\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":\"wrong value\",\"numChunks\":5,\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":\"wrong value\",\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":\"wrong value\"}",
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

  // The following tests are in the form:
  // body
  // + expected response
  @ParameterizedTest
  @CsvSource(value = {
  "{}; "
      + "\\{\"name\":\"must not be (null|blank)\",\"hash\":\"must not be (null|blank)\",\"status\":\"must not be (null|blank)\"\\}",

  "{\"name\":\"\"};"
      + " \\{\"name\":\"must not be (null|blank)\",\"hash\":\"must not be (null|blank)\",\"status\":\"must not be (null|blank)\"\\}",

  "{\"name\":\"test\"};"
      + "\\{\"hash\":\"must not be (null|blank)\",\"status\":\"must not be (null|blank)\"\\}",

  "{\"name\":\"test\",\"hash\":\"\"}; "
      + "\\{\"hash\":\"(Hash length must be 64 alphanumeric char|must not be (null|blank))\",\"status\":\"must not be (null|blank)\"\\}",

  "{\"name\":\"test\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\"}; "
      + "\\{\"status\":\"must not be (null|blank)\"\\}",

  "{\"name\":\"test\",\"hash\":\"abc\",\"status\":0};"
      + "\\{\"hash\":\"Hash length must be 64 alphanumeric char\"\\}",

  "{\"name\":\"test\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\": -1,\"status\":0};"
      + "\\{\"numTrx\":\"numTrx value must be positive\"\\}",

  "{\"name\":\"test\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numAggregates\": -1,\"status\":0};"
      + "\\{\"numAggregates\":\"numAggregates value must be positive\"\\}",

  "{\"name\":\"test\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"amountAde\": -1,\"status\":0};"
      + "\\{\"amountAde\":\"amountAde value must be positive\"\\}",

  "{\"name\":\"test\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"amountRtd\": -1,\"status\":0};"
      + "\\{\"amountRtd\":\"amountRtd value must be positive\"\\}",

  "{\"name\":\"test\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numChunks\": -1,\"status\":0};"
      + "\\{\"numChunks\":\"numChunks value must be positive\"\\}",
  }
  , delimiter = ';')
  void shouldNotPostConstraintViolation(String requestBody, String expectedResponseBody) throws Exception {
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders
            .post(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(requestBody)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andReturn();

    org.junit.jupiter.api.Assertions.assertTrue(matchesPattern(expectedResponseBody).matches(result.getResponse().getContentAsString()));
    BDDMockito.verify(fileMetadataService, Mockito.times(0)).updateFileMetadata(Mockito.any(FileMetadataDTO.class));
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

}
