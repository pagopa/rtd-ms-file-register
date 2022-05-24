package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("fileregister")
public class RtdFile {

  private String name;

  private String hash;


  private int numTrx;


  private int numAggregates;


  private BigDecimal amountAde;


  private BigDecimal amountRtd;

  private int numChunks;

  private int status;

}
