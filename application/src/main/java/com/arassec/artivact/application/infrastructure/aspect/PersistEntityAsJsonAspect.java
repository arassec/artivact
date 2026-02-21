package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.IdentifiedObject;
import com.arassec.artivact.domain.model.configuration.ConfigurationTypeProvider;
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
            String filename = annotation.filename();

            if (isDelete) {
                handleDelete(joinPoint.getArgs(), entityDir, filename);
            } else {
                handlePersist(joinPoint.getArgs(), entityDir, entityType, filename);
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
     * @param filename   The name of the JSON file.
     */
    private void handlePersist(Object[] args, String entityDir, Class<?> entityType, String filename) {
        for (Object arg : args) {
            if (entityType.isInstance(arg) && arg instanceof IdentifiedObject entity) {
                String id = entity.getId();
                if (id == null || id.length() < MIN_ID_LENGTH) {
                    return;
                }

                Path jsonFilePath = resolveJsonFilePath(id, entityDir, filename);

                fileRepository.createDirIfRequired(jsonFilePath.getParent());
                byte[] jsonBytes = jsonMapper.writeValueAsBytes(entity);
                fileRepository.write(jsonFilePath, jsonBytes);

                log.debug("Persisted entity {} as JSON: {}", id, jsonFilePath);
            } else if (entityType.isInstance(arg) && arg instanceof ConfigurationTypeProvider config) {
                String type = config.getConfigurationType().toString().toLowerCase();

                Path jsonFilePath = useProjectDirsUseCase.getProjectRoot().resolve(entityDir).resolve(type + filename);
                fileRepository.createDirIfRequired(jsonFilePath.getParent());
                byte[] jsonBytes = jsonMapper.writeValueAsBytes(config);
                fileRepository.write(jsonFilePath, jsonBytes);

                log.debug("Persisted config {} as JSON: {}", type, jsonFilePath);
            }
        }
    }

    /**
     * Handles deleting entity JSON files.
     *
     * @param args      The method arguments.
     * @param entityDir The entity type directory.
     * @param filename  The name of the JSON file.
     */
    private void handleDelete(Object[] args, String entityDir, String filename) {
        for (Object arg : args) {
            if (arg instanceof String entityId) {
                if (entityId.length() < MIN_ID_LENGTH) {
                    return;
                }
                fileRepository.deleteAndPruneEmptyParents(resolveJsonFilePath(entityId, entityDir, filename));
            }
        }
    }

    /**
     * Resolves the JSON file path for the given entity ID and entity directory.
     *
     * @param entityId  The entity's ID.
     * @param entityDir The entity type directory.
     * @param filename  The name of the JSON file (without path).
     * @return The path to the JSON file.
     */
    private Path resolveJsonFilePath(String entityId, String entityDir, String filename) {
        Path root = useProjectDirsUseCase.getProjectRoot().resolve(entityDir);
        return fileRepository.getSubdirFilePath(root, entityId, filename);
    }

}
