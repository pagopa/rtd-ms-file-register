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
      return 1;
    }
  },

  TRANSACTIONS_CHUNK {
    @Override
    public int getOrder() {
      return 2;
    }
  },

  AGGREGATES_SOURCE {
    @Override
    public int getOrder() {
      return 3;
    }
  },

  AGGREGATES_CHUNK {
    @Override
    public int getOrder() {
      return 4;
    }
  },

  AGGREGATES_DESTINATION {
    @Override
    public int getOrder() {
      return 5;
    }
  },

  ADE_ACK {
    @Override
    public int getOrder() {
      return 6;
    }
  },

  SENDER_ADE_ACK {
    @Override
    public int getOrder() {
      return 7;
    }
  };

  public abstract int getOrder();
}
