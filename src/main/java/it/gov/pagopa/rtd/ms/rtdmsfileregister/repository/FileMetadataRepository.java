package it.gov.pagopa.rtd.ms.rtdmsfileregister.repository;

import com.mongodb.lang.NonNull;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataEntity;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Data Access Object to manage all CRUD operations to the database This kind of repository
 * leverages object relational mapping (ORM), so it can be used only with relational DB.
 */
public interface FileMetadataRepository extends MongoRepository<FileMetadataEntity, String> {

  @NonNull
  @Override
  FileMetadataEntity save(@NonNull FileMetadataEntity metadata);

  FileMetadataEntity findFirstByName(String filename);

  FileMetadataEntity removeByName(String filename);

  List<FileMetadataEntity> findNamesBySenderAndTypeAndStatus(String sender, int type, int status);

  List<FileMetadataEntity> findAllByNameAndType(String sender, int type);
}
