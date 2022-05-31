package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(RestControllerImpl.class)
@Slf4j
class RestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @SpyBean
  RestControllerImpl restController;

  static String BASE_URI = "http://localhost:8080";

  static String METADATA_ENDPOINT = "/file-status";

  @Test
  void shouldGetMetadata() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .get(BASE_URI + METADATA_ENDPOINT)
            .param("filename", "test")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void shouldNotGetNullFilename() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .get(BASE_URI + METADATA_ENDPOINT)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldPut() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}")
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":\"wrong value\",\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":5,\"numAggregates\":\"wrong value\",\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":\"wrong value\",\"amountRtd\":700,\"numChunks\":5,\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":\"wrong value\",\"numChunks\":5,\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":\"wrong value\",\"status\":0}",
      "{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":\"wrong value\"}",
  })
  void shouldNotPut(String body) throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(body)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldDelete() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .delete(BASE_URI + METADATA_ENDPOINT)
            .param("filename", "test")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void shouldNotDeleteNullFilename() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .delete(BASE_URI + METADATA_ENDPOINT)
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

}
