package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.RtdFile;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.RtdFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RtdFileCrudService {

  @Autowired
  private RtdFileRepository repository;

  public boolean storeRtdFileStatus(RtdFile f){
    repository.insert(f);
    return true;
  }
}
