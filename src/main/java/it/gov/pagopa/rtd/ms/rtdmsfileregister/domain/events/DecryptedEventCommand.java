package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.NamingConventionPolicy;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.FileMetadataRepository;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DecryptedEventCommand implements Function<FileMetadata, FileChanged> {

  private final FileMetadataRepository repository;
  private final NamingConventionPolicy namingConventionPolicy;

  @Override
  public FileChanged apply(FileMetadata fileMetadata) {

    // retrieve pgp container from parent
    var parentFile = repository.findFirstByName(
        namingConventionPolicy.extractParentFileName(fileMetadata.getName(),
            fileMetadata.getContainer()));

    if (parentFile == null) {
      log.info("Cannot find parent file from DB!");
      // return container of the actual file, not the parent's
      return new FileChanged("/" + fileMetadata.getContainer() + "/" + fileMetadata.getParent(),
          fileMetadata.getSender(), fileMetadata.getSize(), fileMetadata.getReceiveTimestamp(),
          StatusMapper.getFileChangedTypeFromFile(fileMetadata));
    }

    // return container of the parent pgp file
    return new FileChanged("/" + parentFile.getContainer() + "/" + fileMetadata.getParent(),
        fileMetadata.getSender(), fileMetadata.getSize(), fileMetadata.getReceiveTimestamp(),
        StatusMapper.getFileChangedTypeFromFile(fileMetadata));
  }
}
