package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events.FileChanged.Type;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;
import jakarta.annotation.Nullable;
import java.util.Map;

public class StatusMapper {

  private static final Map<String, Type> statusConversionMap = Map.of(
      "2_0", FileChanged.Type.RECEIVED,
      "3_0", FileChanged.Type.DECRYPTED,
      "4_0", FileChanged.Type.SENT_TO_ADE,
      "6_0", FileChanged.Type.ACK_TO_DOWNLOAD,
      "6_2", FileChanged.Type.ACK_DOWNLOADED
  );

  private StatusMapper() {
  }

  @Nullable
  public static FileChanged.Type getFileChangedTypeFromFile(FileMetadata fileMetadata) {
    return statusConversionMap.get(String.format("%d_%d", fileMetadata.getType(), fileMetadata.getStatus()));
  }
}
