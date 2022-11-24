package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileChanged {

  private final String fileName;
  private final String sender;
  private final Long size;
  private final LocalDateTime receiveTimestamp;
  private final Type status;

  public enum Type {
    RECEIVED,
    DECRYPTED,
    SENT_TO_ADE,
    ACK_TO_DOWNLOAD,
    ACK_DOWNLOADED
  }
}
