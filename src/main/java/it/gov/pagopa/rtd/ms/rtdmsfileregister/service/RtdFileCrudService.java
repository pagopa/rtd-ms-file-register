package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataEntity;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.RtdFileRepository;
import java.util.Arrays;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RtdFileCrudService {

  ModelMapper modelMapper = new ModelMapper();

  @Autowired
  private RtdFileRepository repository;

  public boolean storeRtdFileStatus(FileMetadataDTO f){
    repository.insert(modelMapper.map(f, FileMetadataEntity.class));
    return true;
  }

  public List<FileMetadataDTO> retrieveFileMetadata(){
    return Arrays.asList(modelMapper.map(repository.findAll(), FileMetadataDTO[].class));
  }

  public boolean deleteFileMetadata(){
    repository.deleteAll();
    return true;
  }
}
