package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

//@RunWith(SpringRunner.class)
@WebMvcTest(RestControllerImpl.class)
@Slf4j
class RestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @SpyBean
  RestControllerImpl restController;

  static String BASE_URI = "http://localhost:8080";

  static String METADATA_ENDPOINT = "/file-status";

  static String HEALTHCHECK_ENDPOINT = "/";

  void insertDummy() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
        .put(BASE_URI + METADATA_ENDPOINT)
        .param("filename", "prova")
        .content("{\"status\":5}")
        .accept(MediaType.TEXT_PLAIN));
  }

  @Test
  void shouldGetHealthcheck() throws Exception {
    //insertDummy();
    mockMvc.perform(MockMvcRequestBuilders
            .get(BASE_URI+HEALTHCHECK_ENDPOINT)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void shouldGetMetadata() throws Exception {
    //insertDummy();
    mockMvc.perform(MockMvcRequestBuilders
            .get(BASE_URI + METADATA_ENDPOINT)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void shouldPut() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}")
            .accept(MediaType.TEXT_PLAIN))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void shouldNotPut() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .put(BASE_URI + METADATA_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"test0\",\"hash\":\"abc\",\"numTrx\":\"test\",\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}")
            .accept(MediaType.TEXT_PLAIN))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldDelete() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .delete(BASE_URI + METADATA_ENDPOINT)
            .accept(MediaType.TEXT_PLAIN))
        .andDo(print())
        .andExpect(status().isOk());
  }

//  @Test
//  void shouldFailGetNotFound() throws Exception {
//    mockMvc.perform(MockMvcRequestBuilders
//            .get(BASE_URI + ENDPOINT)
//            .param("filename", "prova")
//            .accept(MediaType.TEXT_PLAIN))
//        .andDo(print())
//        .andExpect(status().isNotFound())
//        .andExpect(header().string("Error", "file not found"));
//  }
//
//  @Test
//  void shouldFailGetNoParam() throws Exception {
//    mockMvc.perform(MockMvcRequestBuilders
//            .get(BASE_URI + ENDPOINT)
//            .accept(MediaType.TEXT_PLAIN))
//        .andDo(print())
//        .andExpect(status().isBadRequest());
//  }
//
//  @ParameterizedTest
//  @ValueSource(strings = {
//      "{\"status\":1}",
//      "{\"status\":2}"
//  })
//  void shouldPutStatus(String body) throws Exception {
//    mockMvc.perform(MockMvcRequestBuilders
//            .put(BASE_URI + ENDPOINT)
//            .param("filename", "prova")
//            .content(body)
//            .accept(MediaType.TEXT_PLAIN)
//            .accept(MediaType.APPLICATION_JSON))
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(header().string("current-status", body.split(":")[1].replace("}", "")));
//  }
}
