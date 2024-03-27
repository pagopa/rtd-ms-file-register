package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events;

import static it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events.StatusMapper.getFileChangedTypeFromFile;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events.FileChanged.Type;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * This is a factory which produce file changed events based on file metadata's type and status
 */
@RequiredArgsConstructor
class TypeStatusFileChangedFactory implements FileChangedFactory {

  private final DecryptedEventCommand decryptedEventCommand;

  @Override
  public Optional<FileChanged> createFrom(FileMetadata file) {
    return Optional.ofNullable(getFileChangedTypeFromFile(file))
        .map(type -> {
          if (type == Type.DECRYPTED) {
            return decryptedEventCommand.apply(file);
          } else {
            return new FileChanged("/" + file.getContainer() + "/" + file.getParent(),
                file.getSender(), file.getSize(), file.getReceiveTimestamp(), type);
          }
        });
  }
}
