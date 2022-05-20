package it.gov.pagopa.rtd.ms.rtdmsfileregister.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.RtdFile;

/**
 * Data Access Object to manage all CRUD operations to the database
 */
@Repository
public interface RtdFileRepository extends CrudRepository<RtdFile, String> {

  RtdFile findByName(String name);

  List<RtdFile> findByStatus(int status);
}
