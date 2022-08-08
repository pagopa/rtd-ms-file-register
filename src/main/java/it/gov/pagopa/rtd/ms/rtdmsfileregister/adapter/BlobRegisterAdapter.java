package it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter;

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
    if (event.getData() != null) {
      log.info(event.getData().getUrl());
    }
    LocalDateTime eventTimeinLocal = event.getEventTime();

    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String containerName = parts[4];
    if (containerName.matches("ade") || containerName.matches("sender-ade-ack")) {
      containerName = containerName + "/" + parts[6];
    }

    FileType fileType = evaluateContainer(containerName);

    if (fileType == FileType.UNKNOWN) {
      log.info(EVENT_NOT_OF_INTEREST_MSG + event.getSubject());
      return null;
    } else {
      log.info("Received event: " + event.getSubject());
    }

    String blobName;
    if (fileType == FileType.AGGREGATES_DESTINATION || fileType == FileType.ADE_ACK
        || fileType == FileType.SENDER_ADE_ACK) {
      blobName = parts[7];
    } else {
      blobName = parts[6];
    }

    FileMetadataDTO fileMetadata = new FileMetadataDTO();

    fileMetadata.setName(blobName);

    fileMetadata.setReceiveTimestamp(eventTimeinLocal);

    fileMetadata.setStatus(FileStatus.SUCCESS.getOrder());

    fileMetadata.setType(fileType.getOrder());

    fileMetadata.setApplication(evaluateApplication(fileType).getOrder());

    fileMetadata.setSender(extractSender(blobName, fileType));

    if (event.getData() == null) {
      log.warn("No metedata found for event: " + event.getSubject());
    } else if (event.getData().getContentLength() == null) {
      log.warn("No content length found for event: " + event.getSubject());
    } else {
      fileMetadata.setSize(event.getData().getContentLength());
      if (fileMetadata.getSize() <= 0) {
        log.warn("File size is " + fileMetadata.getSize() + " for event: " + event.getSubject());
      }
    }

    fileMetadata.setParent(extractParent(blobName, fileType));

    try {
      fileMetadataService.storeFileMetadata(fileMetadata);
    } catch (FilenameAlreadyPresent e) {
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
    if (containerName.matches("sender-ade-ack/[A-Z0-9]{5}")) {
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
    try {
      if (fileType == FileType.SENDER_ADE_ACK) {
        String originalAdeAck = filename.replaceFirst(filename.split("\\.")[1], "")
            .replaceFirst(filename.split("\\.")[2], "")
            .replaceFirst("\\.", "").replaceFirst("\\.", "");
        return "CSTAR.".concat(originalAdeAck);
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      log.warn("Exception while extracting parent: " + e.getMessage());
    }
    return null;
  }

  public String extractSender(String filename, FileType fileType) {
    if (fileType == FileType.UNKNOWN) {
      return null;
    }
    if (fileType == FileType.ADE_ACK) {
      return "ADE";
    }
    return filename.split("\\.")[1];
  }
}