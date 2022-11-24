package it.gov.pagopa.rtd.ms.rtdmsfileregister.domain;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.runners.Parameterized;

import java.util.stream.Stream;

import static it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileType.*;
import static org.assertj.core.api.Assertions.assertThat;

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
}