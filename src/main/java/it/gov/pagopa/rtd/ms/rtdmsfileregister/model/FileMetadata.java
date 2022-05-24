package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("fileregister")
public class FileMetadata {

  private String name;

  private String hash;


  private int numTrx;


  private int numAggregates;


  private BigDecimal amountAde;


  private BigDecimal amountRtd;

  private int numChunks;

  private int status;

}
