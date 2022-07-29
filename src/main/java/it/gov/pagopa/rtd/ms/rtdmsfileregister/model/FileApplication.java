package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

public enum FileApplication {

  UNKNOWN {
    @Override
    public int getOrder() {
      return -1;
    }
  },

  RTD {
    @Override
    public int getOrder() {
      return 0;
    }
  },

  ADE {
    @Override
    public int getOrder() {
      return 1;
    }
  };

  public abstract int getOrder();
}
