package it.gov.pagopa.rtd.ms.rtdmsfileregister.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class EventGridData {

  /**
   * The operation that triggered the event.
   */
  @JsonProperty(value = "api", required = true)
  private String api;

  /**
   * A client-provided request ID for the storage API operation.
   */
  @JsonProperty(value = "clientRequestId")
  private String clientRequestId;

  /**
   * Service-generated request ID for the storage API operation.
   */
  @JsonProperty(value = "requestId", required = true)
  private String requestId;

  /**
   * The value that you can use to run operations conditionally.
   */
  @JsonProperty(value = "eTag", required = true)
  private String eTag;

  /**
   * The content type specified for the blob.
   */
  @JsonProperty(value = "contentType", required = true)
  private String contentType;

  /**
   * The size of the blob in bytes.
   */
  @JsonProperty(value = "contentLength", required = true)
  private int contentLength;

  /**
   * The type of blob. Valid values are either "BlockBlob" or "PageBlob".
   */
  @JsonProperty(value = "contentType", required = true)
  private String blobType;

  /**
   * 	The offset in bytes of a write operation taken at the point where the event-triggering application completed writing to the file.
   */
  @JsonProperty(value = "contentOffset", required = true)
  private String contentOffset;

  /**
   * The url of the file that will exist after the operation completes.
   */
  @JsonProperty(value = "destinationUrl", required = true)
  private String destinationUrl	;

  /**
   * The url of the file that exists before the operation is done.
   */
  @JsonProperty(value = "sourceUrl", required = true)
  private String sourceUrl;

  /**
   * The path to the blob.
   * If the client uses a Blob REST API, then the url has this structure: <storage-account-name>.blob.core.windows.net\<container-name>\<file-name>..
   */
  @JsonProperty(value = "url", required = true)
  private String url;

  /**
   * True to run the operation on all child directories; otherwise False.
   */
  @JsonProperty(value = "recursive", required = true)
  private String recursive;

  /**
   * An opaque string value representing the logical sequence of events for any particular blob name.
   */
  @JsonProperty(value = "sequencer", required = true)
  private String sequencer;

  /**
   * A string value representing the identity associated with the event.
   */
  @JsonProperty(value = "identity", required = true)
  private String identity	;

  /**
   * Diagnostic data occasionally included by the Azure Storage service..
   */
  @JsonProperty(value = "storageDiagnostics", required = true)
  private Object storageDiagnostics;


}
