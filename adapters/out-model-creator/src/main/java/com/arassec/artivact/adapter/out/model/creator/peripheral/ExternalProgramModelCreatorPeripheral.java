package com.arassec.artivact.adapter.out.model.creator.peripheral;

import com.arassec.artivact.application.port.out.gateway.OsGateway;
import com.arassec.artivact.application.port.out.peripheral.ModelCreatorPeripheral;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.configuration.PeripheralImplementation;
import com.arassec.artivact.domain.model.item.CreationImageSet;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import com.arassec.artivact.domain.model.peripheral.BasePeripheral;
import com.arassec.artivact.domain.model.peripheral.ModelCreationResult;
import com.arassec.artivact.domain.model.peripheral.PeripheralStatus;
import com.arassec.artivact.domain.model.peripheral.configs.ExternalProgramPeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.ModelCreatorPeripheralConfig;
import com.arassec.artivact.domain.model.peripheral.configs.PeripheralConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for model-creator adapter implementations.
 */
@Component
@Getter
@RequiredArgsConstructor
public class ExternalProgramModelCreatorPeripheral extends BasePeripheral implements ModelCreatorPeripheral {

    /**
     * The file repository.
     */
    private final FileRepository fileRepository;

    /**
     * Gateway to the operating system.
     */
    private final OsGateway osGateway;

    /**
     * Result directory template used for external model creators.
     */
    private static final String RESULT_DIR_TEMPLATE = "{projectDir}/temp/export/";

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralImplementation getSupportedImplementation() {
        return PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PeripheralStatus getStatus(PeripheralConfig peripheralConfig) {
        if (inUse.get()) {
            return PeripheralStatus.AVAILABLE;
        }

        if (osGateway.isExecutable(((ExternalProgramPeripheralConfig) peripheralConfig).getCommand())) {
            return PeripheralStatus.AVAILABLE;
        }

        return PeripheralStatus.NOT_EXECUTABLE;
    }

    @Override
    public List<PeripheralConfig> scanPeripherals() {
        List<PeripheralConfig> peripheralConfigs = new ArrayList<>();

        if (inUse.get()) {
            return peripheralConfigs;
        }

        Path home = Path.of(System.getProperty("user.home"));

        Optional<Path> optionalMeshroom = osGateway.scanForDirectory(home, 5, "Meshroom-2025");
        if (optionalMeshroom.isPresent()) {
            ModelCreatorPeripheralConfig peripheralConfig = new ModelCreatorPeripheralConfig();
            peripheralConfig.setLabel("Meshroom 2025");
            peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);
            peripheralConfig.setCommand(optionalMeshroom.get().resolve("Meshroom").toAbsolutePath().toString());
            peripheralConfig.setArguments("-i {projectDir}/temp/\n-p photogrammetry");
            peripheralConfig.setResultDir(RESULT_DIR_TEMPLATE);

            peripheralConfigs.add(peripheralConfig);

            peripheralConfig = new ModelCreatorPeripheralConfig();
            peripheralConfig.setLabel("Meshroom 2025 (Batch)");
            peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);
            peripheralConfig.setCommand(optionalMeshroom.get().resolve("meshroom_batch").toAbsolutePath().toString());
            peripheralConfig.setArguments("-i {projectDir}/temp/\n-p photogrammetry\n-o {projectDir}/temp/export/");
            peripheralConfig.setResultDir(RESULT_DIR_TEMPLATE);

            peripheralConfigs.add(peripheralConfig);
        }

        if (osGateway.isLinux()) {
            Optional<Path> optionalMetashape = osGateway.scanForDirectory(home, 5, "metashape-2.2");
            if (optionalMetashape.isPresent()) {
                ModelCreatorPeripheralConfig peripheralConfig = new ModelCreatorPeripheralConfig();
                peripheralConfig.setLabel("Metashape 2.2");
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);
                peripheralConfig.setCommand(optionalMetashape.get().resolve("metashape").toAbsolutePath().toString());
                peripheralConfig.setOpenInputDirInOs(true);
                peripheralConfig.setArguments("");
                peripheralConfig.setResultDir(RESULT_DIR_TEMPLATE);
                peripheralConfigs.add(peripheralConfig);
            }
        }

        if (osGateway.isWindows()) {
            Path metashape = Path.of("C:\\Program Files\\Agisoft\\Metashape\\metashape.exe");
            if (osGateway.isExecutable(metashape.toAbsolutePath().toString())) {
                ModelCreatorPeripheralConfig peripheralConfig = new ModelCreatorPeripheralConfig();
                peripheralConfig.setLabel("Metashape");
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);
                peripheralConfig.setCommand(metashape.toAbsolutePath().toString());
                peripheralConfig.setOpenInputDirInOs(true);
                peripheralConfig.setArguments("");
                peripheralConfig.setResultDir(RESULT_DIR_TEMPLATE);
                peripheralConfigs.add(peripheralConfig);
            }

            Path realityScan = Path.of("C:\\Program Files\\Epic Games\\RealityScan_2.0\\RealityScan.exe");
            if (osGateway.isExecutable(realityScan.toAbsolutePath().toString())) {
                ModelCreatorPeripheralConfig peripheralConfig = new ModelCreatorPeripheralConfig();
                peripheralConfig.setLabel("RealityScan 2.0");
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);
                peripheralConfig.setCommand(realityScan.toAbsolutePath().toString());
                peripheralConfig.setArguments("-addFolder {projectDir}/temp/\n-save {projectDir}/temp/MyProject.rcproj");
                peripheralConfig.setResultDir("{projectDir}/temp/export/");
                peripheralConfigs.add(peripheralConfig);

                peripheralConfig = new ModelCreatorPeripheralConfig();
                peripheralConfig.setLabel("RealityScan (Headless)");
                peripheralConfig.setPeripheralImplementation(PeripheralImplementation.EXTERNAL_PROGRAM_MODEL_CREATOR_PERIPHERAL);
                peripheralConfig.setCommand(realityScan.toAbsolutePath().toString());
                peripheralConfig.setArguments("""
                        -addFolder {projectDir}/temp/
                        -save {projectDir}/temp/MyProject.rcproj
                        -headless
                        -align
                        -setReconstructionRegionAuto
                        -calculateNormalModel
                        -simplify 200000
                        -smooth
                        -calculateTexture
                        -exportSelectedModel {projectDir}/temp/export/RealityScanExport.obj
                        -quit
                        """);
                peripheralConfig.setResultDir(RESULT_DIR_TEMPLATE);
                peripheralConfigs.add(peripheralConfig);
            }
        }

        return peripheralConfigs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelCreationResult createModel(List<Path> images) {
        Path tempDir = initParams.getWorkDir();

        fileRepository.emptyDir(tempDir);

        copyImages(images, tempDir, progressMonitor);

        ModelCreatorPeripheralConfig config = ((ModelCreatorPeripheralConfig) initParams.getConfig());
        Path resultDir = Path.of(config.getResultDir().replace("{projectDir}", initParams.getProjectRoot().toAbsolutePath().toString()));

        fileRepository.emptyDir(resultDir);
        fileRepository.createDirIfRequired(resultDir);

        if (config.isOpenInputDirInOs()) {
            fileRepository.openDirInOs(tempDir);
        }

        progressMonitor.updateLabelKey("createModelStart");

        String command = config.getCommand();
        String[] arguments = config.getArguments()
                .replace("{projectDir}", initParams.getProjectRoot().toAbsolutePath().toString())
                .replace("\n", " ")
                .split(" ");


        osGateway.execute(command, Arrays.asList(arguments));

        return new ModelCreationResult(resultDir, config.getLabel());
    }

    /**
     * Copies all images from the provided {@link CreationImageSet} to the provided destination.
     *
     * @param images          The images to copy.
     * @param destination     The destination path to copy the images to.
     * @param progressMonitor A progress monitor which is updated during copying.
     */
    protected void copyImages(List<Path> images, Path destination, ProgressMonitor progressMonitor) {
        progressMonitor.updateLabelKey("copyImages");
        var index = new AtomicInteger(1);
        images.forEach(image -> {
            progressMonitor.updateProgress(index.getAndIncrement(), images.size());
            getFileRepository().copy(image, destination.resolve(image.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        });
    }

}
