package com.arassec.artivact.creator.standalone.core.service;

import com.arassec.artivact.creator.standalone.core.adapter.export.ExportAdapter;
import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExportAdapter exportAdapter;

    public void export(CreatorArtivact creatorArtivact, ProgressMonitor progressMonitor) {
        exportAdapter.export(creatorArtivact, creatorArtivact.getProjectRoot().resolve("Export"), progressMonitor);
    }

}
