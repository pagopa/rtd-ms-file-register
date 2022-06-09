package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FileMetadata {
  @NotNull
  @NotBlank
  private String name;

  @NotNull
  @NotBlank
  @Pattern(regexp = "[a-zA-Z0-9]{64}",
      message = "Hash length must be 64 alphanumeric char")
  private String hash;

  @Min(value = 0, message = "numTrx value must be positive")
  private Integer numTrx;

  @Min(value = 0, message = "numAggregates value must be positive")
  private Integer numAggregates;

  @Min(value = 0, message = "amountAde value must be positive")
  private BigDecimal amountAde;

  @Min(value = 0, message = "amountRtd value must be positive")
  private BigDecimal amountRtd;

  @Min(value = 0, message = "numChunks value must be positive")
  private Integer numChunks;

  @NotNull
  private Integer status;
}
