package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType;

public interface NamingConventionPolicy {
  String extractParentFileName(String filename, FileType fileType);
}
