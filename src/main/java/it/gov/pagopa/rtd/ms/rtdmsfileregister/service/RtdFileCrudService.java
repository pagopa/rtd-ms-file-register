package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.RtdFileRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RtdFileCrudService {

  @Autowired
  private RtdFileRepository repository;

  public boolean storeRtdFileStatus(FileMetadata f){
    repository.insert(f);
    return true;
  }

  public List<FileMetadata> retrieveFileMetadata(){
    return repository.findAll();
  }
}
