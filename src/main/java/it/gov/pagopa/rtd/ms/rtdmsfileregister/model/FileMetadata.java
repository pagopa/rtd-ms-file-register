package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Getter
@Setter
public class FileMetadata {

  //Common File Metadata

  @NotNull
  @NotBlank
  private String name;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime receiveTimestamp;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime lastTransitionTimestamp;

  @NotNull
  private Integer status;

  //CSTAR File Metadata

  @Pattern(regexp = "[a-zA-Z0-9]{5}",
      message = "Sender code must be 5 alphanumeric char")
  private String sender;

  //ADEACK File Metadata

  private List<String> originalFilesList;

  //RTD File Metadata

  @Pattern(regexp = "[a-zA-Z0-9]{64}",
      message = "Hash length must be 64 alphanumeric char")
  private String hash;

  private String application;

  @Min(value = 0, message = "Number of chunks total value must be positive")
  private Integer chunksTotal;

  @Min(value = 0, message = "Number of chunks left value must be positive")
  private Integer chunksLeft;

  //AppRTD File Metadata

  @Min(value = 0, message = "numTransactions value must be positive")
  private Integer numTransactions;

  @Min(value = 0, message = "amountTransactions value must be positive")
  private BigDecimal amountTransactions;

  //AppADE File Metadata

  @Min(value = 0, message = "numAggregates value must be positive")
  private Integer numAggregates;

  @Min(value = 0, message = "amountAggregates value must be positive")
  private BigDecimal amountAggregates;

  private List<String> senderAdeAckFilesList;

}
