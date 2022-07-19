package it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameAlreadyPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileApplication;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileStatus;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BlobRegisterAdapter {

  String encryptedTransactionsContainer = "((ade|rtd)-transactions-[a-z0-9]{44})";
  String decryptedTransactionsContainer = "((ade|rtd)-transactions-decrypted)";
  String adeAggregatesContainer = "(ade)";
  String senderADEACKContainer = "(sender-ade-ack)";


  String acceptedContainers = encryptedTransactionsContainer + "|"
      + decryptedTransactionsContainer + "|"
      + senderADEACKContainer + "|"
      + adeAggregatesContainer;

  private static final String EVENT_NOT_OF_INTEREST_MSG = "Event not of interest: ";

  @Autowired
  FileMetadataService fileMetadataService;

  public EventGridEvent evaluateEvent(EventGridEvent event) {
    LocalDateTime eventTimeinLocal = event.getEventTime();

    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String containerName = parts[4];
    if (containerName.matches("ade")) {
      containerName = containerName + "/" + parts[5];
    }

    FileType fileType = evaluateContainer(containerName);

    if (fileType == FileType.UNKNOWN) {
      log.info(EVENT_NOT_OF_INTEREST_MSG + event.getSubject());
      return null;
    } else {
      log.info("Received event: " + event.getSubject());
    }

    String blobName = parts[6];

    FileMetadataDTO fileMetadata = new FileMetadataDTO();

    fileMetadata.setName(cleanFilename(blobName));

    fileMetadata.setReceiveTimestamp(eventTimeinLocal);

    fileMetadata.setStatus(FileStatus.SUCCESS.getOrder());

    fileMetadata.setType(fileType.getOrder());

    fileMetadata.setApplication(evaluateApplication(fileType).getOrder());

    fileMetadata.setSize(extractFileSize(event));
    if (fileMetadata.getSize() <= 0) {
      log.warn("File size is "+ fileMetadata.getSize()+ " for event: " + event.getSubject());
    }

    try {
      fileMetadataService.storeFileMetadata(fileMetadata);
    } catch (FilenameAlreadyPresent e){
      log.warn("File already present: " + fileMetadata.getName());
    }

    log.info("Evaluated event: {}", event.getSubject());
    return event;
  }

  public FileType evaluateContainer(String containerName) {
    // RTD types
    if (containerName.matches("rtd-transactions-[a-z0-9]{44}")) {
      return FileType.TRANSACTIONS_SOURCE;
    }
    if (containerName.matches("rtd-transactions-decrypted")) {
      return FileType.TRANSACTIONS_CHUNK;
    }

    // ADE types
    if (containerName.matches("ade-transactions-[a-z0-9]{44}")) {
      return FileType.AGGREGATES_SOURCE;
    }
    if (containerName.matches("ade-transactions-decrypted")) {
      return FileType.AGGREGATES_CHUNK;
    }
    if (containerName.matches("ade/in")) {
      return FileType.AGGREGATES_DESTINATION;
    }
    if (containerName.matches("ade/ack")) {
      return FileType.ADE_ACK;
    }
    if (containerName.matches("sender-ade-ack")) {
      return FileType.SENDER_ADE_ACK;
    }

    return FileType.UNKNOWN;
  }

  public FileApplication evaluateApplication(FileType fileType) {
    // RTD application
    if (fileType == FileType.TRANSACTIONS_SOURCE
        || fileType == FileType.TRANSACTIONS_CHUNK) {
      return FileApplication.RTD;
    }

    // ADE types
    if (fileType == FileType.AGGREGATES_SOURCE
        || fileType == FileType.AGGREGATES_CHUNK
        || fileType == FileType.AGGREGATES_DESTINATION
        || fileType == FileType.ADE_ACK
        || fileType == FileType.SENDER_ADE_ACK) {
      return FileApplication.ADE;
    }

    return FileApplication.UNKNOWN;
  }

  public long extractFileSize(EventGridEvent event) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode sizeNode = objectMapper.readTree(String.valueOf(event.getData()))
          .get("contentLength");
      if (sizeNode != null) {
        return sizeNode.longValue();
      } else {
        log.warn("No contentLength in event: {}", event.getSubject());
      }
    } catch (JsonProcessingException e) {
      log.warn("Failed to parse event: {}", event.getSubject());
    }
    return 0;
  }

  public String extractParent(String filename, FileType fileType) {
    if (fileType == FileType.TRANSACTIONS_SOURCE
        || fileType == FileType.AGGREGATES_SOURCE
        || fileType == FileType.ADE_ACK
    ) {
      return filename;
    }
    if (fileType == FileType.TRANSACTIONS_CHUNK || fileType == FileType.AGGREGATES_CHUNK) {
      return filename.replaceAll("\\.(\\d)+\\.decrypted", "");
    }
    if (fileType == FileType.AGGREGATES_DESTINATION) {
      return filename.replaceAll("\\.(\\d)+\\.decrypted", "")
          .replace(".gz", "");
    }
    if (fileType == FileType.SENDER_ADE_ACK) {
      return filename.replaceFirst(filename.split("\\.")[1], "")
          .replaceFirst("\\.", "");
    }
    return null;
  }

}