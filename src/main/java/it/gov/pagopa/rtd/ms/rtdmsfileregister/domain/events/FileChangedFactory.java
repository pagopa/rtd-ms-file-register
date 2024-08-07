package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;

import java.util.Optional;

/**
 * Factory to create a file changed event starting from a file
 */
public interface FileChangedFactory {

  static FileChangedFactory typeStatusBased(DecryptedEventCommand command) {
    return new TypeStatusFileChangedFactory(command);
  }

  Optional<FileChanged> createFrom(FileMetadata fileMetadata);
}
