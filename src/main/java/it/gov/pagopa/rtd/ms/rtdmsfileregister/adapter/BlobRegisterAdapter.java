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

    DECRYPTIONERROR {
      @Override
      public int getOrder() {
        return -1;
      }
    },

    MALFORMEDTRX {
      @Override
      public int getOrder() {
        return -2;
      }
    },

    RECEIVED {
      @Override
      public int getOrder() {
        return 0;
      }
    },

    DECRYPTEDANDSPLIT {
      @Override
      public int getOrder() {
        return 1;
      }
    },

    PROCESSED {
      @Override
      public int getOrder() {
        return 10;
      }
    },

    DEPOSITED {
      @Override
      public int getOrder() {
        return 20;
      }
    },

    ADEACKPRODUCED {
      @Override
      public int getOrder() {
        return 21;
      }
    },

    SENDERADEACKPRODUCED {
      @Override
      public int getOrder() {
        return 22;
      }
    },

    SENDERACKCONSUMED {
      @Override
      public int getOrder() {
        return 23;
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

  @Autowired
  FileMetadataService fileMetadataService;

  public boolean validateContainer(EventGridEvent event) {
    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String containerName = parts[4];

    return containerName.matches(acceptedContainers);
  }

  public EventGridEvent evaluateEvent(EventGridEvent event) {
    LocalDateTime eventTimeinLocal = event.getEventTime();

    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String blobName = parts[6];

    FileMetadataDTO fileMetadata = new FileMetadataDTO();

    fileMetadata.setName(cleanFilename(blobName));

    fileMetadata.setReceiveTimestamp(eventTimeinLocal);
    fileMetadata.setLastTransitionTimestamp(eventTimeinLocal);

    fileMetadata.setStatus(0);

    fileMetadataService.storeFileMetadata(fileMetadata);

    log.info("Evaluated event: {}", event.getSubject());
    return event;
  }

  public String cleanFilename (String filename) {
    return filename
        .replace(".csv", "")
        .replaceAll("\\.(\\d)+\\.decrypted", "")
        .replace(".pgp", "")
        .replace(".gpg", "");

  }

}
