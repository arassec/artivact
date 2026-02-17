package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.IdentifiedObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Implements the {@link PersistAsJson} aspect.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PersistAsJsonAspect {

    /**
     * Minimum ID length required for JSON persistence.
     */
    private static final int MIN_ID_LENGTH = 6;

    /**
     * The JSON mapper for serialization.
     */
    private final JsonMapper jsonMapper;

    /**
     * The file repository for file operations.
     */
    private final FileRepository fileRepository;

    /**
     * Use case for accessing project directories.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Configuration property to enable or disable JSON persistence.
     */
    @Value("${artivact.json.persistence.enabled:false}")
    private boolean jsonPersistenceEnabled;

    /**
     * Persists entities as JSON files after the annotated method returns successfully.
     *
     * @param joinPoint    The aspectj {@link JoinPoint}.
     * @param returnValue  The value returned by the method.
     */
    @AfterReturning(pointcut = "@annotation(com.arassec.artivact.application.infrastructure.aspect.PersistAsJson)", returning = "returnValue")
    public void persistAsJson(JoinPoint joinPoint, Object returnValue) {
        if (!jsonPersistenceEnabled) {
            return;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            PersistAsJson annotation = signature.getMethod().getAnnotation(PersistAsJson.class);
            String entityType = annotation.value();

            // Handle different types of return values
            if (returnValue instanceof IdentifiedObject identifiedObject) {
                persistEntity(entityType, identifiedObject);
            } else if (returnValue instanceof Collection<?> collection) {
                for (Object item : collection) {
                    if (item instanceof IdentifiedObject identifiedObject) {
                        persistEntity(entityType, identifiedObject);
                    }
                }
            } else {
                // Check method parameters for entities to persist
                for (Object argument : joinPoint.getArgs()) {
                    if (argument instanceof IdentifiedObject identifiedObject) {
                        persistEntity(entityType, identifiedObject);
                    } else if (argument instanceof Collection<?> collection) {
                        for (Object item : collection) {
                            if (item instanceof IdentifiedObject identifiedObject) {
                                persistEntity(entityType, identifiedObject);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't interrupt the main flow
            log.error("Failed to persist entity as JSON", e);
        }
    }

    /**
     * Persists a single entity as JSON.
     *
     * @param entityType The type of entity (e.g., "items", "pages", "menus").
     * @param entity     The entity to persist.
     */
    private void persistEntity(String entityType, IdentifiedObject entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        String id = entity.getId();
        if (id.length() < MIN_ID_LENGTH) {
            log.debug("Skipping JSON persistence for entity with ID shorter than 6 characters: {}", id);
            return;
        }

        try {
            Path entityTypeDir = useProjectDirsUseCase.getProjectRoot().resolve(entityType);
            Path jsonFilePath = getJsonFilePath(entityTypeDir, id);

            // Create parent directories
            fileRepository.createDirIfRequired(jsonFilePath.getParent());

            // Serialize and write JSON
            String json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entity);
            Files.writeString(jsonFilePath, json);

            log.debug("Persisted entity as JSON: {}", jsonFilePath);
        } catch (IOException e) {
            log.error("Failed to write JSON file for entity ID: {}", id, e);
        }
    }

    /**
     * Deletes entity JSON files before the annotated method executes.
     *
     * @param joinPoint The aspectj {@link JoinPoint}.
     */
    @Before("@annotation(com.arassec.artivact.application.infrastructure.aspect.DeleteEntityJson)")
    public void deleteEntityJson(JoinPoint joinPoint) {
        if (!jsonPersistenceEnabled) {
            return;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            DeleteEntityJson annotation = signature.getMethod().getAnnotation(DeleteEntityJson.class);
            String entityType = annotation.value();

            // Check method parameters for entity IDs or entities to delete
            for (Object argument : joinPoint.getArgs()) {
                if (argument instanceof String entityId) {
                    deleteEntityJsonFile(entityType, entityId);
                } else if (argument instanceof IdentifiedObject identifiedObject) {
                    deleteEntityJsonFile(entityType, identifiedObject.getId());
                }
            }
        } catch (Exception e) {
            // Log error but don't interrupt the main flow
            log.error("Failed to delete entity JSON", e);
        }
    }

    /**
     * Deletes the JSON file for an entity.
     *
     * @param entityType The type of entity (e.g., "items", "pages", "menus").
     * @param entityId   The ID of the entity to delete.
     */
    private void deleteEntityJsonFile(String entityType, String entityId) {
        if (!jsonPersistenceEnabled || entityId == null || entityId.length() < MIN_ID_LENGTH) {
            return;
        }

        try {
            Path entityTypeDir = useProjectDirsUseCase.getProjectRoot().resolve(entityType);
            Path jsonFilePath = getJsonFilePath(entityTypeDir, entityId);

            if (Files.exists(jsonFilePath)) {
                Files.delete(jsonFilePath);
                log.debug("Deleted JSON file: {}", jsonFilePath);

                // Clean up empty parent directories
                Path firstParent = jsonFilePath.getParent();
                if (Files.exists(firstParent) && fileRepository.list(firstParent).isEmpty()) {
                    Files.delete(firstParent);

                    Path secondParent = firstParent.getParent();
                    if (Files.exists(secondParent) && fileRepository.list(secondParent).isEmpty()) {
                        Files.delete(secondParent);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to delete JSON file for entity ID: {}", entityId, e);
        }
    }

    /**
     * Gets the JSON file path for an entity ID.
     *
     * @param root The root directory.
     * @param id   The entity ID.
     * @return The path to the JSON file.
     */
    private Path getJsonFilePath(Path root, String id) {
        String firstSubDir = id.substring(0, 3);
        String secondSubDir = id.substring(3, 6);
        return root.resolve(firstSubDir).resolve(secondSubDir).resolve(id + ".json");
    }
}
