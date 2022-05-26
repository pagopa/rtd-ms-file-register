package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDTO {

  private String name;

  private String hash;

  private int numTrx;

  private int numAggregates;


  private BigDecimal amountAde;


  private BigDecimal amountRtd;

  private int numChunks;

  private int status;
}
