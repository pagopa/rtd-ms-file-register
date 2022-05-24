//package it.gov.pagopa.rtd.ms.rtdmsfileregister.repository;
//
//import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.RtdFile;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@Slf4j
//@SpringBootTest
//@RunWith(SpringRunner.class)
//class RtdFileRepositoryTest {
//
//  @Autowired
//  private RtdFileRepository repository;
//
//  void populate() throws Exception {
//    RtdFile tmp1 = new RtdFile();
//    tmp1.setStatus(0);
//    tmp1.setName("test");
//    RtdFile tmp2 = new RtdFile();
//    tmp2.setStatus(1);
//    tmp2.setName("prova");
//    RtdFile tmp3 = new RtdFile();
//    tmp3.setStatus(1);
//    tmp3.setName("file1");
//    RtdFile tmp4 = new RtdFile();
//    tmp4.setStatus(5);
//    tmp4.setName("primo");
//    RtdFile tmp5 = new RtdFile();
//    tmp5.setStatus(5);
//    tmp5.setName("secondo");
//
//    repository.save(tmp1);
//    repository.save(tmp2);
//    repository.save(tmp3);
//    repository.save(tmp4);
//    repository.save(tmp5);
//  }
//
////  @AfterEach
////  void cleanMock() {
////    restController.resetDbStub();
////  }
//
//  @Test
//  void shouldRetrieve() throws Exception {
//    populate();
//
//    // fetch all customers
//    log.info("Files found with findAll():");
//    log.info("-------------------------------");
//    for (RtdFile customer : repository.findAll()) {
//      log.info(customer.toString());
//    }
//    log.info("");
//  }
//}
