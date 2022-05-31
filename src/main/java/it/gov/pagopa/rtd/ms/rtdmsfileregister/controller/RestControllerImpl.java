package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@Slf4j
public class RestControllerImpl implements
    it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController {

  @Override
  public List<FileMetadataDTO> getFileStatus() {
    FileMetadataDTO stub = new FileMetadataDTO("test0", "abc", 5, 2, new BigDecimal(900),
        new BigDecimal(700), 5, 0);
    return List.of(stub);
  }

  @Override
  public ResponseEntity<String> setFileStatus(@NotNull @NotBlank FileMetadataDTO body) {
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<String> deleteFileMetadata() {
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
