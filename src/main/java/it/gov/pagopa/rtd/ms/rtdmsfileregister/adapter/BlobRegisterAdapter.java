package it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
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
    if (containerName.matches("ade")){
      containerName = containerName + "/" + parts[5];
    }

    FileType fileType = evaluateContainer(containerName);

    if (fileType == FileType.UNKNOWN) {
      log.info(EVENT_NOT_OF_INTEREST_MSG + event.getSubject());
      return null;
    }

    String blobName = parts[6];

    FileMetadataDTO fileMetadata = new FileMetadataDTO();

    fileMetadata.setName(cleanFilename(blobName));

    fileMetadata.setLastTransitionTimestamp(eventTimeinLocal);

    fileMetadata.setStatus(FileStatus.SUCCESS.getOrder());

    fileMetadata.setType(evaluateContainer(containerName).getOrder());

    fileMetadata.setReceiveTimestamp(eventTimeinLocal);
    fileMetadataService.storeFileMetadata(fileMetadata);

    log.info("Evaluated event: {}", event.getSubject());
    return event;
  }

  public String cleanFilename(String filename) {
    return filename
        .replace(".csv", "")
        .replaceAll("\\.(\\d)+\\.decrypted", "")
        .replace(".pgp", "")
        .replace(".gpg", "");

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

}