package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


/**
 * Properties of an event published to an Event Grid topic. https://docs.microsoft.com/en-us/azure/event-grid/event-schema
 */
@NoArgsConstructor
@Getter
@Setter
public class EventGridEvent {

  /**
   * A unique identifier for the event.
   */
  @JsonProperty(value = "id", required = true)
  private String id;

  /**
   * The resource path of the event source.
   */
  @JsonProperty(value = "topic")
  private String topic;

  /**
   * A resource path relative to the topic path.
   */
  @JsonProperty(value = "subject", required = true)
  private String subject;

  /**
   * Event data specific to the event type.
   */
  @JsonProperty(value = "data", required = true)
  private Object data;

  /**
   * The type of the event that occurred.
   */
  @JsonProperty(value = "eventType", required = true)
  private String eventType;

  /**
   * The time (in UTC) the event was generated.
   */
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonProperty(value = "eventTime", required = true)
  private LocalDateTime eventTime;

  /**
   * The schema version of the event metadata.
   */
  @JsonProperty(value = "metadataVersion", access = JsonProperty.Access.WRITE_ONLY)
  private String metadataVersion;

  /**
   * The schema version of the data object.
   */
  @JsonProperty(value = "dataVersion", required = true)
  private String dataVersion;
}
