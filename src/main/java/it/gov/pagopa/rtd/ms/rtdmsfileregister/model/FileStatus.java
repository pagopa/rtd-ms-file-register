package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

public enum FileStatus {
  SUCCESS {
    @Override
    public int getOrder() {
      return 0;
    }
  },

  FAILED {
    @Override
    public int getOrder() {
      return -1;
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
