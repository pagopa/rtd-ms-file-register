package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.RtdFileCrudService;
import java.util.HashMap;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@Slf4j
public class RestControllerImpl implements
    it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController {

  @Autowired
  RtdFileCrudService service;

  private HashMap<String, FileMetadata> dbStub = new HashMap<>();


  ObjectMapper mapper = new ObjectMapper();

  @Override
  public void healthCheck() {
    //Return OK if the service is reachable
  }

  @Override
  public ResponseEntity<String> getFileStatus(@NotNull @NotBlank String fileName) {
    HttpHeaders headers = new HttpHeaders();
//
//    //if (repository.findByName(fileName) == null) {
//    if (dbStub.get(fileName) == null) {
//      headers.add("Error", "file not found");
//      return new ResponseEntity<>(headers, HttpStatus.NOT_FOUND);
//    }
//
//    //int tmpStaus = repository.findByName(fileName).getStatus();
//    int tmpStaus = dbStub.get(fileName).getStatus();
//
//    headers.add("current-status", String.valueOf(tmpStaus));
    return new ResponseEntity<>(headers, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<String> updateFileStatus(@NotNull @NotBlank String fileName,
      @NotNull @NotBlank FileMetadata body) {

    HttpHeaders headers = new HttpHeaders();
//    RtdFile tmpFile = null;
//    RtdFile presentFile;
//
//    //Lock the entry for filename
//
//    //if (repository.findByName(fileName) != null) {
//    if (dbStub.get(fileName) != null) {
//      presentFile = dbStub.get(fileName);
//      log.info("Ho trovato " + presentFile);
//    } else {
//      presentFile = new RtdFile();
//      log.info("Lo creo nuovo");
//    }
//
//    try {
//      tmpFile = mapper.readValue(body, RtdFile.class);
//      presentFile.setName(fileName);
//    } catch (NumberFormatException | InvalidFormatException e) {
//      log.error(e.toString());
//      return new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
//    } catch (NullPointerException | JsonProcessingException e) {
//      log.error(e.toString());
//      return new ResponseEntity<>(headers, HttpStatus.UNPROCESSABLE_ENTITY);
//    }
//

//    presentFile.setStatus(tmpFile.getStatus());
//
    service.storeRtdFileStatus(body);
//
////    repository.save(presentFile);
////    System.err.println("Ho salvato "+ repository.findByName(fileName));
//    dbStub.put(fileName, presentFile);
//
//    System.err.println("Ho salvato " + dbStub.get(fileName));
//    headers.add("current-status", String.valueOf(tmpFile.getStatus()));
    return new ResponseEntity<>(headers, HttpStatus.OK);
  }

//  public void resetDbStub() {
////    repository.deleteAll();
////    this.dbStub = new HashMap<>();
//  }
}
