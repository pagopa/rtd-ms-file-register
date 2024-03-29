package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain;

import static it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType.AGGREGATES_CHUNK;
import static it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType.AGGREGATES_DESTINATION;
import static it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType.AGGREGATES_SOURCE;
import static it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType.SENDER_ADE_ACK;
import static it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class DefaultNamingPolicyTest {

  private NamingConventionPolicy defaultPolicy;

  @BeforeEach
  public void setUp() {
    defaultPolicy = new DefaultNamingPolicy();
  }

  static class FilenameWithBatchChunkProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
      return Stream.of(
              Arguments.of("ADE.99999.TRNLOG.20220503.172038.467.01.csv.pgp", AGGREGATES_SOURCE, "ADE.99999.TRNLOG.20220503.172038.467.01.csv.pgp"),
              Arguments.of("AGGADE.99999.20220503.172038.467.01000", AGGREGATES_CHUNK, "ADE.99999.TRNLOG.20220503.172038.467.01.csv.pgp"),
              Arguments.of("AGGADE.99999.20220503.172038.467.01000.gz", AGGREGATES_DESTINATION, "ADE.99999.TRNLOG.20220503.172038.467.01.csv.pgp"),
              Arguments.of("AGGADE.99999.20220503.172038.467.02010", AGGREGATES_CHUNK, "ADE.99999.TRNLOG.20220503.172038.467.02.csv.pgp"),
              Arguments.of("AGGADE.99999.20220503.172038.467.02005.gz", AGGREGATES_DESTINATION, "ADE.99999.TRNLOG.20220503.172038.467.02.csv.pgp")
      );
    }
  }

  // input, type, expected output
  @ParameterizedTest
  @ArgumentsSource(FilenameWithBatchChunkProvider.class)
  void givenFilenameWithBatchChunkThenExtractRightParentFile(String filename, FileType fileType, String expectedParent) {
    final var parentFilename = defaultPolicy.extractParentFileName(filename, fileType);
    assertThat(parentFilename).isEqualTo(expectedParent);
  }

  static class FilenameWithNoBatchChunkProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
      return Stream.of(
              Arguments.of("ADE.99999.TRNLOG.20220503.172038.467.csv.pgp", AGGREGATES_SOURCE, "ADE.99999.TRNLOG.20220503.172038.467.csv.pgp"),
              Arguments.of("AGGADE.99999.20220503.172038.467.00000", AGGREGATES_CHUNK, "ADE.99999.TRNLOG.20220503.172038.467.csv.pgp"),
              Arguments.of("AGGADE.99999.20220503.172038.467.00020", AGGREGATES_CHUNK, "ADE.99999.TRNLOG.20220503.172038.467.csv.pgp"),
              Arguments.of("AGGADE.99999.20220503.172038.467.00000.gz", AGGREGATES_DESTINATION, "ADE.99999.TRNLOG.20220503.172038.467.csv.pgp"),
              Arguments.of("AGGADE.99999.20220503.172038.467.00010.gz", AGGREGATES_DESTINATION, "ADE.99999.TRNLOG.20220503.172038.467.csv.pgp")
      );
    }
  }


  @ParameterizedTest
  @ArgumentsSource(FilenameWithNoBatchChunkProvider.class)
  void givenFilenameWithNoBatchChunkThenExtractRightParentFile(String filename, FileType fileType, String expectedParent) {
    final var parentFilename = defaultPolicy.extractParentFileName(filename, fileType);
    assertThat(parentFilename).isEqualTo(expectedParent);
  }

  @Test
  void givenSenderAdeAckFilenameThenParentIsItself() {
    final var expectedParent = "ADEACK.03599.03599.2022-10-12.8034e5d9-0e92-45d2-9e81-bc84ce3be282.csv";
    assertThat(defaultPolicy.extractParentFileName(expectedParent, SENDER_ADE_ACK)).isEqualTo(expectedParent);
  }

  @Test
  void givenUnknownFileTypeThenParentIsNull() {
    assertThat(defaultPolicy.extractParentFileName("no_matter", UNKNOWN)).isNull();
  }
}
