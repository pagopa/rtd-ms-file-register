package it.gov.pagopa.rtd.ms.rtdmsfileregister.adapter;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.EventGridEvent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.service.FileMetadataService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BlobRegisterAdapter {
  String timeZone = "Europe/Rome";

  @Autowired
  FileMetadataService fileMetadataService;

  public boolean evaluateContainer(EventGridEvent event) {
    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String containerName = parts[4];

    return containerName.matches("(ade|rtd)-transactions-[a-z0-9]{44}")
        ||
        containerName.matches("(ade|rtd)-transactions-decrypted")
        ||
        containerName.matches("sender-ade-ack");
  }

  public EventGridEvent evaluateEvent(EventGridEvent event) {
    LocalDateTime eventTimeinLocal = event.getEventTime();
    ZonedDateTime zoned = eventTimeinLocal.atZone(ZoneId.of(timeZone));
    OffsetDateTime eventTimeinOffset = zoned.toOffsetDateTime();

    String uri = event.getSubject();
    String[] parts = uri.split("/");
    String blobName = parts[6];

    FileMetadataDTO fileMetadata = new FileMetadataDTO();
    fileMetadata.setName(blobName);
    fileMetadata.setReceiveTimestamp(eventTimeinOffset);
    fileMetadataService.storeFileMetadata(fileMetadata);

    log.info("Evaluated event: {}", event.getSubject());
    return event;
  }

}
