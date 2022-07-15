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

  DOWNLOADED {
    @Override
    public int getOrder() {
      return 1;
    }
  };

  public abstract int getOrder();
}
