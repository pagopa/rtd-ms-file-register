package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import io.swagger.annotations.Api;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller to expose RTD File Register MicroService's endpoints
 */
@Api(tags = "RTD file register Controller")
@RequestMapping("")
@Validated
public interface RestController {

  @GetMapping(value = "/file-status", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  List<FileMetadataDTO> getFileMetadata(@RequestParam @Valid @NotNull @NotBlank String filename);

  @PutMapping(value = "/file-status", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  FileMetadataDTO putFileMetadata(@NotNull @NotBlank @RequestBody FileMetadataDTO body);

  @DeleteMapping(value = "/file-status", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  List<FileMetadataDTO> deleteFileMetadata(@NotNull @NotBlank @RequestParam String filename);
}