package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

public enum FileStatus {

  FAILED {
    @Override
    public int getOrder() {
      return -1;
    }
  },

  SUCCESS {
    @Override
    public int getOrder() {
      return 0;
    }
  },

  DOWNLOAD_STARTED {
    @Override
    public int getOrder() {
      return 1;
    }
  },

  DOWNLOAD_ENDED {
    @Override
    public int getOrder() {
      return 2;
    }
  };

  public abstract int getOrder();
}
