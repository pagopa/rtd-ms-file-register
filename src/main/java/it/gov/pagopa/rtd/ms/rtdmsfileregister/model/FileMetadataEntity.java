package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("fileregister")
public class FileMetadataEntity {

  @Id
  @NotNull
  @NotBlank
  private String name;

  @NotNull
  @NotBlank
  @Pattern(regexp = "[a-zA-Z0-9]{64}",
      message = "Hash length must be 64 alphanumeric char")
  private String hash;

  @Min(value = 0, message = "The value must be positive")
  private Integer numTrx;

  @Min(value = 0, message = "The value must be positive")
  private Integer numAggregates;

  @Min(value = 0, message = "The value must be positive")
  private BigDecimal amountAde;

  @Min(value = 0, message = "The value must be positive")
  private BigDecimal amountRtd;

  @Min(value = 0, message = "The value must be positive")
  private Integer numChunks;

  @NotNull
  private Integer status;

}
