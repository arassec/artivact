package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.IdentifiedObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Aspect that persists entities as JSON files in the project root whenever they are changed.
 * JSON files are placed under subdirectories matching the entity type using the ID-based subdirectory structure.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PersistEntityAsJsonAspect {

    /**
     * Minimum length of an entity ID to be persisted.
     */
    private static final int MIN_ID_LENGTH = 6;

    /**
     * The JSON mapper.
     */
    private final JsonMapper jsonMapper;

    /**
     * The application's {@link FileRepository}.
     */
    private final FileRepository fileRepository;

    /**
     * Use case for project directory handling.
     */
    private final UseProjectDirsUseCase useProjectDirsUseCase;

    /**
     * Whether JSON persistence is enabled.
     */
    @Value("${artivact.json-persistence.enabled:false}")
    private boolean enabled;

    /**
     * Intercepts methods annotated with {@link PersistEntityAsJson} and persists or deletes the entity JSON file.
     *
     * @param joinPoint The aspectj {@link JoinPoint}.
     */
    @AfterReturning("@annotation(com.arassec.artivact.application.infrastructure.aspect.PersistEntityAsJson)")
    public void persistEntityAsJson(JoinPoint joinPoint) {
        if (!enabled) {
            return;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            PersistEntityAsJson annotation = signature.getMethod().getAnnotation(PersistEntityAsJson.class);

            String entityDir = annotation.entityDir();
            Class<?> entityType = annotation.entityType();
            boolean isDelete = annotation.delete();

            if (isDelete) {
                handleDelete(joinPoint.getArgs(), entityDir);
            } else {
                handlePersist(joinPoint.getArgs(), entityDir, entityType);
            }
        } catch (Exception e) {
            log.warn("Failed to persist entity as JSON: {}", e.getMessage());
        }
    }

    /**
     * Handles persisting entity parameters as JSON files.
     *
     * @param args       The method arguments.
     * @param entityDir  The entity type directory.
     * @param entityType The entity class to look for in the arguments.
     */
    private void handlePersist(Object[] args, String entityDir, Class<?> entityType) {
        for (Object arg : args) {
            if (arg instanceof Collection<?> collection) {
                collection.stream()
                        .filter(entityType::isInstance)
                        .filter(IdentifiedObject.class::isInstance)
                        .map(IdentifiedObject.class::cast)
                        .forEach(entity -> writeJsonFile(entity, entityDir));
            } else if (entityType.isInstance(arg) && arg instanceof IdentifiedObject entity) {
                writeJsonFile(entity, entityDir);
            }
        }
    }

    /**
     * Handles deleting entity JSON files.
     *
     * @param args      The method arguments.
     * @param entityDir The entity type directory.
     */
    private void handleDelete(Object[] args, String entityDir) {
        for (Object arg : args) {
            if (arg instanceof String entityId) {
                deleteJsonFile(entityId, entityDir);
            }
        }
    }

    /**
     * Writes the entity as a JSON file.
     *
     * @param entity    The entity to serialize.
     * @param entityDir The entity type directory.
     */
    private void writeJsonFile(IdentifiedObject entity, String entityDir) {
        String id = entity.getId();
        if (id == null || id.length() < MIN_ID_LENGTH) {
            return;
        }

        Path jsonFilePath = resolveJsonFilePath(id, entityDir);

        fileRepository.createDirIfRequired(jsonFilePath.getParent());
        byte[] jsonBytes = jsonMapper.writeValueAsBytes(entity);
        fileRepository.write(jsonFilePath, jsonBytes);

        log.debug("Persisted entity {} as JSON: {}", id, jsonFilePath);
    }

    /**
     * Deletes the JSON file for the given entity ID.
     *
     * @param entityId  The entity's ID.
     * @param entityDir The entity type directory.
     */
    private void deleteJsonFile(String entityId, String entityDir) {
        if (entityId == null || entityId.length() < MIN_ID_LENGTH) {
            return;
        }

        Path jsonFilePath = resolveJsonFilePath(entityId, entityDir);

        if (fileRepository.exists(jsonFilePath)) {
            fileRepository.delete(jsonFilePath);
            cleanEmptyParentDirs(jsonFilePath);
        }
    }

    /**
     * Resolves the JSON file path for the given entity ID and entity directory.
     *
     * @param entityId  The entity's ID.
     * @param entityDir The entity type directory.
     * @return The path to the JSON file.
     */
    private Path resolveJsonFilePath(String entityId, String entityDir) {
        Path root = useProjectDirsUseCase.getProjectRoot().resolve(entityDir);
        String firstSubDir = fileRepository.getSubDir(entityId, 0);
        String secondSubDir = fileRepository.getSubDir(entityId, 1);
        return root.resolve(firstSubDir).resolve(secondSubDir).resolve(entityId + ".json");
    }

    /**
     * Removes empty parent directories up to two levels after file deletion.
     *
     * @param jsonFilePath The path of the deleted JSON file.
     */
    private void cleanEmptyParentDirs(Path jsonFilePath) {
        Path secondSubDir = jsonFilePath.getParent();
        Path firstSubDir = secondSubDir != null ? secondSubDir.getParent() : null;

        if (secondSubDir != null && fileRepository.exists(secondSubDir) && isDirEmpty(secondSubDir)) {
            fileRepository.delete(secondSubDir);
        }
        if (firstSubDir != null && fileRepository.exists(firstSubDir) && isDirEmpty(firstSubDir)) {
            fileRepository.delete(firstSubDir);
        }
    }

    /**
     * Checks if a directory is empty.
     *
     * @param dir The directory to check.
     * @return {@code true} if the directory is empty.
     */
    private boolean isDirEmpty(Path dir) {
        return fileRepository.isDir(dir) && fileRepository.list(dir).isEmpty();
    }

}
