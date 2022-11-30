package it.gov.pagopa.rtd.ms.rtdmsfileregister.service;

import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.DTOViolationException;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.EmptyFilenameException;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.StatusAlreadySet;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameAlreadyPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.controller.RestController.FilenameNotPresent;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.domain.events.FileChangedFactory;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadata;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.FileMetadataEntity;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.model.SenderAdeAckListDTO;
import it.gov.pagopa.rtd.ms.rtdmsfileregister.repository.FileMetadataRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileMetadataServiceImpl implements FileMetadataService {

  private final FileMetadataRepository repository;
  private final FileChangedFactory eventFactory;
  private final ApplicationEventPublisher eventPublisher;

  private final ModelMapper modelMapper = new ModelMapper();


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
    return modelMapper.map(saveAsEntity(metadata), FileMetadataDTO.class);
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
    factory.close();

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
        } else {
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

    return modelMapper.map(updateEntity(toBeUpdated), FileMetadataDTO.class);
  }

  @Override
  public SenderAdeAckListDTO getSenderAdeAckList(List<String> senders) {
    List<String> fileNameList = new ArrayList<>();

    for (String sender : senders) {
      if (sender != null) {
        List<FileMetadataEntity> retrieved = repository.findNamesBySenderAndTypeAndStatus(sender, 6,
            0);
        for (FileMetadataEntity f : retrieved) {
          fileNameList.add(f.getName());
        }
      }
    }

    return new SenderAdeAckListDTO(fileNameList);
  }

  @Override
  public FileMetadataDTO updateStatus(String filename, int status) {
    FileMetadataEntity toBeUpdated = repository.findFirstByName(filename);

    if (toBeUpdated == null) {
      throw new FilenameNotPresent();
    }

    if (toBeUpdated.getStatus() == status) {
      throw new StatusAlreadySet();
    }

    toBeUpdated.setStatus(status);

    repository.removeByName(filename);

    return modelMapper.map(updateEntity(toBeUpdated), FileMetadataDTO.class);
  }


  private FileMetadataEntity saveAsEntity(FileMetadataDTO fileMetadataDto) {
    return updateEntity(modelMapper.map(fileMetadataDto, FileMetadataEntity.class));
  }

  /**
   * Move save to repository to a single point.
   * This allows to fires event properly without looking for "right place" where "fire events".
   * The right place is after saving entity to db.
   */
  private FileMetadataEntity updateEntity(FileMetadataEntity entity) {
    final var saveEntity = repository.save(entity);
    fireEvents(saveEntity);
    return saveEntity;
  }

  private void fireEvents(FileMetadata fileMetadata) {
    final var event = eventFactory.createFrom(fileMetadata);
    if (event.isPresent()) {
      eventPublisher.publishEvent(event.get());
    } else {
      log.warn("No event created for {}", fileMetadata);
    }
  }
}
