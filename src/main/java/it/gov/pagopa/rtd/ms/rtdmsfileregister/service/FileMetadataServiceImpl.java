package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.DTOViolationException;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.EmptyFilenameException;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameAlreadyPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataEntity;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.FileMetadataRepository;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileMetadataServiceImpl implements FileMetadataService {

  ModelMapper modelMapper = new ModelMapper();

  @Autowired
  private FileMetadataRepository repository;

  public FileMetadataDTO retrieveFileMetadata(String filename) {
    FileMetadataEntity retrieved = repository.findFirstByName(filename);
    if (retrieved != null) {
      return modelMapper.map(retrieved, FileMetadataDTO.class);
    }

    return null;
  }

  public FileMetadataDTO storeFileMetadata(FileMetadataDTO metadata) {
    FileMetadataEntity retrieved = repository.findFirstByName(metadata.getName());

    if (retrieved != null) {
      throw new FilenameAlreadyPresent();
    }

    modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    return modelMapper.map(repository.save(modelMapper.map(metadata, FileMetadataEntity.class)),
        FileMetadataDTO.class);
  }

  public FileMetadataDTO deleteFileMetadata(String filename) {
    FileMetadataEntity deleted = repository.removeByName(filename);
    if (deleted != null) {
      return modelMapper.map(deleted, FileMetadataDTO.class);
    }

    return null;
  }

  @Override
  public FileMetadataDTO updateFileMetadata(FileMetadataDTO metadata) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    // Blocks the request if the name is null or empty, then blocks if there are constraint violations
    // other than null. In this way only valid and non-null fields are mapped to the entity.
    Set<ConstraintViolation<FileMetadataDTO>> violations = validator.validate(metadata);
    if (!violations.isEmpty()) {
      for (ConstraintViolation<FileMetadataDTO> violation : violations) {
        if (violation.getPropertyPath().toString().equals("name")) {
          if (violation.getMessage().equals("must not be null") || violation.getMessage()
              .equals("must not be blank")) {
            throw new EmptyFilenameException();
          }
        } else if (!(violation.getMessage().equals("must not be null") || violation.getMessage()
            .equals("must not be blank"))) {
          throw new DTOViolationException();
        }
      }
    }

    FileMetadataEntity toBeUpdated = repository.findFirstByName(metadata.getName());

    if (toBeUpdated == null) {
      return null;
    }

    repository.removeByName(metadata.getName());

    //Avoid to override fields not specified in the request body
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT)
        .setPropertyCondition(Conditions.isNotNull());

    modelMapper.map(metadata, toBeUpdated, String.valueOf(FileMetadataEntity.class));

    return modelMapper.map(repository.save(toBeUpdated), FileMetadataDTO.class);
  }
}
