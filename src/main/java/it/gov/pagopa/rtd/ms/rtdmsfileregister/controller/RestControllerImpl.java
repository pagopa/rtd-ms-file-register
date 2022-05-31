package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@Slf4j
public class RestControllerImpl implements
    it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController {

  @Override
  public List<FileMetadataDTO> getFileMetadata(String filename) {
    log.info("Received GET [{}]", filename);

    FileMetadataDTO stub = new FileMetadataDTO(filename, "abc", 5, 2, new BigDecimal(900),
        new BigDecimal(700), 5, 0);

    return List.of(stub);
  }

  @Override
  public FileMetadataDTO putFileMetadata(FileMetadataDTO body) {
    log.info("Received PUT [{}]", body);

    return body;
  }

  @Override
  public List<FileMetadataDTO> deleteFileMetadata(String filename) {
    log.info("Received DELETE [{}]", filename);

    FileMetadataDTO stub = new FileMetadataDTO(filename, "abc", 5, 2, new BigDecimal(900),
        new BigDecimal(700), 5, 0);

    return List.of(stub);
  }

}
