package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import io.swagger.annotations.Api;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  List<FileMetadataDTO> getFileStatus();


  @PutMapping(value = "/file-status")
  ResponseEntity<String> setFileStatus(@RequestBody FileMetadataDTO body);
}
