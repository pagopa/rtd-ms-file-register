package it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BlobRegisterAdapter {

  public enum STATUS {

    SUCCESS {
      @Override
      public int getOrder() {
        return 0;
      }
    },

    FAILED {
      @Override
      public int getOrder() {
        return -2;
      }
    },

    DOWNLOADED {
      @Override
      public int getOrder() {
        return 0;
      }
    };

    public abstract int getOrder();
  }

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

  public boolean validateContainer(EventGridEvent event) {
    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String containerName = parts[4];

    boolean isEventOfInterest = containerName.matches(acceptedContainers);

    if (!isEventOfInterest) log.info(EVENT_NOT_OF_INTEREST_MSG + event.getSubject());

    return isEventOfInterest;
  }

  public EventGridEvent evaluateEvent(EventGridEvent event) {
    LocalDateTime eventTimeinLocal = event.getEventTime();

    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String containerName = parts[4];
    String blobName = parts[6];

    FileMetadataDTO fileMetadata = new FileMetadataDTO();

    fileMetadata.setName(cleanFilename(blobName));

    fileMetadata.setLastTransitionTimestamp(eventTimeinLocal);

    STATUS newStatus = evaluateContainer(containerName);

    fileMetadata.setStatus(evaluateContainer(containerName).getOrder());

    if(newStatus.equals(STATUS.RECEIVED) || newStatus.equals(STATUS.SENDERADEACKPRODUCED)){
      fileMetadata.setReceiveTimestamp(eventTimeinLocal);
      fileMetadataService.storeFileMetadata(fileMetadata);
    }
    else {
      fileMetadataService.updateFileMetadata(fileMetadata);
    }

    log.info("Evaluated event: {}", event.getSubject());
    return event;
  }

  public STATUS evaluateContainer(String containerName) {
    if (containerName.matches("(ade|rtd)-transactions-decrypted")) {
      return STATUS.DECRYPTEDANDSPLIT;
    }
    if (containerName.matches("sender-ade-ack")) {
      return STATUS.SENDERADEACKPRODUCED;
    }
    if (containerName.matches("ade")) {
      return STATUS.DEPOSITED;
    }

    // Default is the same as containerName.matches("(ade|rtd)-transactions-[a-z0-9]{44}"))
    return STATUS.RECEIVED;
  }


  public String cleanFilename(String filename) {
    return filename
        .replace(".csv", "")
        .replaceAll("\\.(\\d)+\\.decrypted", "")
        .replace(".pgp", "")
        .replace(".gpg", "");

  }

}