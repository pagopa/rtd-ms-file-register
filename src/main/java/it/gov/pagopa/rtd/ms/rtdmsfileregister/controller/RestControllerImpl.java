package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@Slf4j
public class RestControllerImpl implements
    it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController {

  protected ObjectMapper objectMapper = new ObjectMapper();

  static String testFilemetadata = "{\"name\":\"presentFilename\",\"hash\":\"0c8795b2d35316c58136ec2c62056e23e9e620e3b6ec6653661db7a76abd38b5\",\"numTrx\":5,\"numAggregates\":2,\"amountAde\":900,\"amountRtd\":700,\"numChunks\":5,\"status\":0}";

  @Override
  public List<FileMetadataDTO> getFileMetadata(String filename) {
    log.info("Received GET [{}]", filename);

    FileMetadataDTO updatedTestFileMetadataDTO;

    try {
      updatedTestFileMetadataDTO = objectMapper.readValue(testFilemetadata, FileMetadataDTO.class);
    } catch (JsonProcessingException e) {
      updatedTestFileMetadataDTO = null;
    }

    return List.of(updatedTestFileMetadataDTO);
  }

  @Override
  public FileMetadataDTO putFileMetadata(FileMetadataDTO body) {
    log.info("Received PUT [{}]", body);

    return body;
  }

  @Override
  public List<FileMetadataDTO> deleteFileMetadata(String filename) {
    log.info("Received DELETE [{}]", filename);

    FileMetadataDTO updatedTestFileMetadataDTO;

    try {
      updatedTestFileMetadataDTO = objectMapper.readValue(testFilemetadata, FileMetadataDTO.class);
    } catch (JsonProcessingException e) {
      updatedTestFileMetadataDTO = null;
    }
    return List.of(updatedTestFileMetadataDTO);
  }

    @Override
    public Map<String, String> handleValidationExceptions (MethodArgumentNotValidException ex){
      {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
          String fieldName = ((FieldError) error).getField();
          String errorMessage = error.getDefaultMessage();
          errors.put(fieldName, errorMessage);
        });
        log.error(String.valueOf(errors));
        return errors;
      }
    }

  }
