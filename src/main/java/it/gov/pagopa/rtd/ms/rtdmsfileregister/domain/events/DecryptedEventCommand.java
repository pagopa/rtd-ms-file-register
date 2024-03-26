package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.NamingConventionPolicy;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.FileMetadataRepository;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DecryptedEventCommand implements Function<FileMetadata, FileChanged> {

  private final FileMetadataRepository repository;
  private final NamingConventionPolicy namingConventionPolicy;

  @Override
  public FileChanged apply(FileMetadata fileMetadata) {

    // retrieve pgp container from parent
    var parentFile = repository.findFirstByName(
        namingConventionPolicy.extractParentFileName(fileMetadata.getName(),
            fileMetadata.getContainer()));

    // todo if absent then raise error?

    return new FileChanged("/" + parentFile.getContainer() + "/" + fileMetadata.getParent(),
        fileMetadata.getSender(), fileMetadata.getSize(), fileMetadata.getReceiveTimestamp(),
        StatusMapper.getFileChangedTypeFromFile(fileMetadata));
  }
}
