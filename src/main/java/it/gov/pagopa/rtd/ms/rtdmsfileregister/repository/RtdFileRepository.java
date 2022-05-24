package it.gov.pagopa.rtd.ms.rtdmsfileregister.repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.RtdFile;

// import org.springframework.data.repository.CrudRepository;
// import org.springframework.stereotype.Repository;

/**
 * Data Access Object to manage all CRUD operations to the database This kind of repository
 * leverages object relational mappong (ORM), so it can be used only with relational DB.
 * 
 */
// @Repository
// public interface RtdFileRepository extends CrudRepository<RtdFile, String> {

//   RtdFile findByName(String name);

//   List<RtdFile> findByStatus(int status);
// }



public interface RtdFileRepository extends MongoRepository<RtdFile, String> {

  RtdFile insert(RtdFile f);
}
