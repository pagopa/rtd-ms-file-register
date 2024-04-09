package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType;
import org.springframework.data.util.Pair;

import java.util.Optional;

public class DefaultNamingPolicy implements NamingConventionPolicy {
  @Override
  public String extractParentFileName(String filename, FileType fileType) {
    if (fileType == FileType.TRANSACTIONS_SOURCE
            || fileType == FileType.AGGREGATES_SOURCE
            || fileType == FileType.ADE_ACK
            || fileType == FileType.SENDER_ADE_ACK
    ) {
      return filename;
    }
    if (fileType == FileType.AGGREGATES_CHUNK) {
      final var parts = filename.split("\\.");
      final var batchChunk = extractBatchServiceChunk(parts).orElse("");
      return "ADE." + parts[1] + ".TRNLOG." + parts[2] + "." + parts[3] + "." + parts[4]
              + batchChunk
              + ".csv.pgp";
    }
    if (fileType == FileType.TRANSACTIONS_CHUNK) {
      return filename.replaceAll("\\.(\\d)+\\.decrypted", "");
    }
    if (fileType == FileType.AGGREGATES_DESTINATION) {
      String[] parts = filename.replace(".gz", "").split("\\.");
      final var batchChunk = extractBatchServiceChunk(parts).orElse("");
      return "ADE." + parts[1] + ".TRNLOG." + parts[2] + "." + parts[3] + "." + parts[4]
              + batchChunk
              + ".csv.pgp";
    }
    return null;
  }

  @Override
  public String extractParentFileName(String filename, String container) {
    return extractParentFileName(filename, extractFileTypeFromContainer(container));
  }

  @Override
  public FileType extractFileTypeFromContainer(String containerName) {
    // RTD types
    if (containerName.matches("rtd-transactions-[a-z0-9]{44}")) {
      return FileType.TRANSACTIONS_SOURCE;
    }
    if (containerName.matches("rtd-transactions-decrypted")) {
      return FileType.TRANSACTIONS_CHUNK;
    }

    // ADE types
    if (containerName.matches("ade-transactions-[a-z0-9]{44}")) {
      return FileType.AGGREGATES_SOURCE;
    }
    if (containerName.matches("ade-transactions-decrypted")) {
      return FileType.AGGREGATES_CHUNK;
    }
    if (containerName.matches("ade/in")) {
      return FileType.AGGREGATES_DESTINATION;
    }
    if (containerName.matches("ade/ack")) {
      return FileType.ADE_ACK;
    }
    if (containerName.matches("sender-ade-ack/[A-Z0-9]{5}")) {
      return FileType.SENDER_ADE_ACK;
    }

    return FileType.UNKNOWN;
  }

  private Optional<String> extractBatchServiceChunk(String[] parts) {
    final var chunkInfo = extractChunkInfo(parts);
    return chunkInfo.getFirst().equals("00")
            ? Optional.empty() : Optional.of(String.format(".%s", chunkInfo.getFirst()));
  }

  private Pair<String, String> extractChunkInfo(String[] parts) {
    // verify if contains chunk info
    if (parts.length >= 5 && parts[5].matches("\\d{5}")) {
      return Pair.of(parts[5].substring(0, 2), parts[5].substring(2, 5));
    }
    return Pair.of("00", "000");
  }
}
