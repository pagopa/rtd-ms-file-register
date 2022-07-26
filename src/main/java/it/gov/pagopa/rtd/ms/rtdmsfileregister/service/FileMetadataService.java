package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.SenderAdeAckListDTO;
import org.springframework.stereotype.Service;

@Service
public interface FileMetadataService {

  FileMetadataDTO storeFileMetadata(FileMetadataDTO f);

  FileMetadataDTO retrieveFileMetadata(String filename);

  FileMetadataDTO deleteFileMetadata(String filename);

  FileMetadataDTO updateFileMetadata(FileMetadataDTO metadata);

  SenderAdeAckListDTO getSenderAdeAckList(String sender);

}
