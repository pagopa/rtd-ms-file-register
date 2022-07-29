package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
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

  @NotNull
  @NotBlank
  private String name;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime receiveTimestamp;

  @NotNull
  private Integer status;

  @NotNull
  private Integer type;

  @NotNull
  private Integer application;

  @Pattern(regexp = "([a-zA-Z0-9]{5})|ADE",
      message = "Sender code must be 5 alphanumeric char")
  private String sender;

  @NotNull
  private Long size;

  @Pattern(regexp = "[a-zA-Z0-9]{64}",
      message = "Hash length must be 64 alphanumeric char")
  private String hash;

  private String parent;
}
