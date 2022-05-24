package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.RtdFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller to expose MicroService
 */
@Api(tags = "RTD file register Controller")
@RequestMapping("")
@Validated
public interface RestController {

  @GetMapping(value = "/")
  @ResponseStatus(HttpStatus.OK)
  void healthCheck();

  @GetMapping(value = "/file-status")
  ResponseEntity<String> getFileStatus(
      //@ApiParam(value = "${swagger.filestatus.filename}", required = true) String fileName);
      @RequestParam(value = "filename") String fileName);


  @PutMapping(value = "/file-status")
  ResponseEntity<String> updateFileStatus(@RequestParam(value = "filename") String fileName,
      @RequestBody RtdFile body);
}
