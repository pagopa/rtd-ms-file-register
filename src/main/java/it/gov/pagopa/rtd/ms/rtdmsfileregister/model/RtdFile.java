package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RtdFile implements Serializable {

  @Id
  private String name;

  @Pattern(regexp = "[a-zA-Z0-9]{64}",
      message = "Hash length must be 64 alphanumeric char")
  private String hash;

  @Min(value = 0, message = "The value must be positive")
  private int numTrx;

  @Min(value = 0, message = "The value must be positive")
  private int numAggregates;

  @Min(value = 0L, message = "The value must be positive")
  private BigDecimal amountAde;

  @Min(value = 0L, message = "The value must be positive")
  private BigDecimal amountRtd;

  @Min(value = 0, message = "The value must be positive")
  private int numChunks;

  @NotNull
  @NotBlank
  private int status;

}
