package it.gov.pagopa.rtd.ms.rtdmsfileregister.controller;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileStatus;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.SenderAdeAckListDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@Slf4j
class RestControllerImpl implements
    it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController {

  @Autowired
  FileMetadataService fileMetadataService;

  @Override
  public FileMetadataDTO getFileMetadata(@Valid @NotNull @NotBlank String filename) {
    log.info("Received GET [{}]", filename);

    FileMetadataDTO retrieved = fileMetadataService.retrieveFileMetadata(filename);
    if (retrieved == null) {
      throw new FilenameNotPresent();
    }

    return retrieved;
  }

  @Override
  public FileMetadataDTO setFileMetadata(FileMetadataDTO body) {
    log.info("Received POST [{}]", body);

    return fileMetadataService.storeFileMetadata(body);
  }

  @Override
  public FileMetadataDTO deleteFileMetadata(String filename) {
    log.info("Received DELETE [{}]", filename);

    FileMetadataDTO deleted = fileMetadataService.deleteFileMetadata(filename);
    if (deleted == null) {
      throw new FilenameNotPresent();
    }

    return deleted;
  }

  @Override
  public FileMetadataDTO updateFileMetadata(FileMetadataDTO metadata) {
    log.info("Received PUT [{}]", metadata);

    FileMetadataDTO deleted = fileMetadataService.updateFileMetadata(metadata);
    if (deleted == null) {
      throw new FilenameNotPresent();
    }

    return deleted;
  }

  @Override
  public SenderAdeAckListDTO senderAdeACKList(@NotNull List<String> senders) {
    log.info("Received GET sender AdE ACK List for sender {}", senders);

    return fileMetadataService.getSenderAdeAckList(senders);
  }

  @Override
  public FileMetadataDTO setDownloadedSenderAdeAck(String filename) {
    log.info("Received PUT set downloaded SenderAdeAck for file {}", filename);

    FileMetadataDTO updated = fileMetadataService.updateStatus(filename,
        FileStatus.DOWNLOAD_ENDED.getOrder());

    if (updated.getStatus() != FileStatus.DOWNLOAD_ENDED.getOrder()) {
      throw new FileNotUpdated();
    }
    return updated;
  }

}
