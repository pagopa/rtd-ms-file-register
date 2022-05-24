//package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;
//
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestControllerImpl;
//import java.io.IOException;
//import java.net.URISyntaxException;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//@RunWith(SpringRunner.class)
////@SpringBootTest
//@WebMvcTest(RestControllerImpl.class)
//@Slf4j
////@DataJpaTest
//class RestControllerImplTest {
//
//  @Autowired
//  private MockMvc mockMvc;
//
////  @Autowired
////  private static TestRestTemplate restTemplate;
//
////  @MockBean
////  RtdFileRepository repository;
//
//  @SpyBean
//  RestControllerImpl restController;
//
//  static String BASE_URI = "http://localhost:8080";
//
//  static String ENDPOINT = "/file-status";
//
//  void insertDummy() throws Exception {
//    mockMvc.perform(MockMvcRequestBuilders
//        .put(BASE_URI + ENDPOINT)
//        .param("filename", "prova")
//        .content("{\"status\":5}")
//        .accept(MediaType.TEXT_PLAIN));
//  }
//
//  @AfterEach
//  void cleanMock() throws URISyntaxException, IOException {
//    restController.resetDbStub();
//  }
//
//  @Test
//  void shouldGet() throws Exception {
//    insertDummy();
//    mockMvc.perform(MockMvcRequestBuilders
//            .get(BASE_URI + ENDPOINT)
//            .param("filename", "prova")
//            .accept(MediaType.TEXT_PLAIN))
//        .andDo(print())
//        .andExpect(status().isOk())
//        .andExpect(header().string("current-status", "5"));
//  }
//
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
//
//  @ParameterizedTest
//  @ValueSource(strings = {
//      "{\"status\":1}",
//      "{\"status\":2}"
//  })
//  void shouldPutAlreadyExists(String body) throws Exception {
//    insertDummy();
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
//
//  @ParameterizedTest
//  @ValueSource(strings = {
//      "\"status\":5",
//      "{\"status\":5"
//  })
//  void shouldFailMalformedJson(String body) throws Exception {
//    insertDummy();
//    mockMvc.perform(MockMvcRequestBuilders
//            .put(BASE_URI + ENDPOINT)
//            .param("filename", "prova")
//            .content(body)
//            .accept(MediaType.TEXT_PLAIN))
//        //.andDo(print())
//        .andExpect(status().isUnprocessableEntity());
//  }
//
//  @ParameterizedTest
//  @ValueSource(strings = {
//      "",
//      "{\"status\":\"test\"}",
//  })
//  void shouldFailBadRequest(String body) throws Exception {
//    insertDummy();
//    mockMvc.perform(MockMvcRequestBuilders
//            .put(BASE_URI + ENDPOINT)
//            .param("filename", "prova")
//            .content(body)
//            .accept(MediaType.TEXT_PLAIN))
//        .andDo(print())
//        .andExpect(status().isBadRequest());
//  }
//
//}
