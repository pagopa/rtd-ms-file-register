package it.gov.pagopa.rtd.ms.rtdmsfileregister;

import static org.junit.jupiter.api.Assertions.assertTrue;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestControllerImpl;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.RtdFileRepository;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.RtdFileCrudService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class RtdMsFileRegisterApplicationTests {

  @MockBean
  RtdFileRepository repository;

	@InjectMocks
	RtdFileCrudService service;

	@Autowired
	RestControllerImpl controller;

	@Test
	void contextLoads() {
		assertTrue(true);
	}

}
