package it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameAlreadyPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.NamingConventionPolicy;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileApplication;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileStatus;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
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

  private final FileMetadataService fileMetadataService;
  private final NamingConventionPolicy namingConventionPolicy;

  public void evaluateEvent(EventGridEvent event) {
    LocalDateTime eventTimeinLocal = event.getEventTime();

    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String containerName = extractContainer(parts[4], parts[6]);

    FileType fileType = evaluateContainer(containerName);

    if (fileType == FileType.UNKNOWN) {
      log.info(EVENT_NOT_OF_INTEREST_MSG + event.getSubject());
      return;
    }

    log.info("Received event: " + event.getSubject());

    String blobName;
    if (fileType == FileType.AGGREGATES_DESTINATION
        || fileType == FileType.ADE_ACK
        || fileType == FileType.SENDER_ADE_ACK) {
      if (parts.length < 8) {
        log.info(EVENT_NOT_OF_INTEREST_MSG + event.getSubject());
        return;
      }
      blobName = parts[7];
    } else {
      blobName = parts[6];
    }

    FileMetadataDTO fileMetadata = new FileMetadataDTO();
    fileMetadata.setName(blobName);
    fileMetadata.setContainer(containerName);
    fileMetadata.setReceiveTimestamp(eventTimeinLocal);
    fileMetadata.setStatus(FileStatus.SUCCESS.getOrder());
    fileMetadata.setType(fileType.getOrder());
    fileMetadata.setApplication(evaluateApplication(fileType).getOrder());
    fileMetadata.setSender(extractSender(blobName, fileType));

    if (event.getData() == null) {
      log.warn("No metadata found for event: " + event.getSubject());
    } else if (event.getData().getContentLength() == null) {
      log.warn("No content length found for event: " + event.getSubject());
    } else {
      fileMetadata.setSize(event.getData().getContentLength());
      if (fileMetadata.getSize() <= 0 && fileType != FileType.ADE_ACK) {
        log.warn("File size is " + fileMetadata.getSize() + " for event: " + event.getSubject());
      }
    }

    fileMetadata.setParent(extractParent(blobName, fileType));

    try {
      fileMetadataService.storeFileMetadata(fileMetadata);
    } catch (FilenameAlreadyPresent e) {
      if (fileType != FileType.ADE_ACK) {
        log.error("File already present: " + fileMetadata.getName());
      }
    }

    log.info("Evaluated event: {}", event.getSubject());
  }

  public String extractContainer(String containerName, String subContainerName) {
    if (containerName.matches("ade") || containerName.matches("sender-ade-ack")) {
      return containerName + "/" + subContainerName;
    }
    return containerName;
  }

  public FileType evaluateContainer(String containerName) {
    return namingConventionPolicy.extractFileTypeFromContainer(containerName);
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
    return namingConventionPolicy.extractParentFileName(filename, fileType);
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
