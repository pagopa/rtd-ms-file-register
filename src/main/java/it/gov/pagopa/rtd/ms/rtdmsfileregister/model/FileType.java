package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

public enum FileType {

  UNKNOWN {
    @Override
    public int getOrder() {
      return -1;
    }
  },

  TRANSACTIONS_SOURCE {
    @Override
    public int getOrder() {
      return 0;
    }
  },

  TRANSACTIONS_CHUNK {
    @Override
    public int getOrder() {
      return 1;
    }
  },

  AGGREGATES_SOURCE {
    @Override
    public int getOrder() {
      return 2;
    }
  },

  AGGREGATES_CHUNK {
    @Override
    public int getOrder() {
      return 3;
    }
  },

  AGGREGATES_DESTINATION {
    @Override
    public int getOrder() {
      return 4;
    }
  },

  ADE_ACK {
    @Override
    public int getOrder() {
      return 5;
    }
  },

  SENDER_ADE_ACK {
    @Override
    public int getOrder() {
      return 6;
    }
  };

  public abstract int getOrder();
}
