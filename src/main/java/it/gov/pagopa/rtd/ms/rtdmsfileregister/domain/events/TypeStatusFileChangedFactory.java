package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is a factory which produce file changed events based on
 * file metadata's type and status
 */
class TypeStatusFileChangedFactory implements FileChangedFactory {

  private final Map<String, FileChanged.Type> statusConversionMap;

  public TypeStatusFileChangedFactory() {
    statusConversionMap = new HashMap<>();
    statusConversionMap.put("2_0", FileChanged.Type.RECEIVED);
    statusConversionMap.put("3_0", FileChanged.Type.DECRYPTED);
    statusConversionMap.put("4_0", FileChanged.Type.SENT_TO_ADE);
    statusConversionMap.put("6_0", FileChanged.Type.ACK_TO_DOWNLOAD);
    statusConversionMap.put("6_2", FileChanged.Type.ACK_DOWNLOADED);
  }

  @Override
  public Optional<FileChanged> createFrom(FileMetadata file) {
    return Optional.ofNullable(statusConversionMap.get(lookupKey(file)))
            .map(type -> new FileChanged(file.getParent(), file.getSender(), file.getSize(), file.getReceiveTimestamp(), type));
  }

  private String lookupKey(FileMetadata fileMetadata) {
    return String.format("%d_%d", fileMetadata.getType(), fileMetadata.getStatus());
  }
}
