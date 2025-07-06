package com.arassec.artivact.domain.misc;

import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.misc.ProgressMonitor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ProgressMonitor}.
 */
class ProgressMonitorTest {

    /**
     * Tests updating the progress.
     */
    @Test
    void testUpdateProgress() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ProgressMonitorTest.class, "suffix");
        assertThat(progressMonitor.getTargetAmount()).isZero();
        assertThat(progressMonitor.getCurrentAmount()).isZero();
        assertThat(progressMonitor.getException()).isNull();

        progressMonitor.updateProgress(25, 100);
        assertThat(progressMonitor.getTargetAmount()).isEqualTo(100);
        assertThat(progressMonitor.getCurrentAmount()).isEqualTo(25);
        assertThat(progressMonitor.getException()).isNull();

        progressMonitor.updateProgress("error", new ArtivactException("test-exception"));
        assertThat(progressMonitor.getException()).isNotNull();
        assertThat(progressMonitor.getException().getMessage()).isEqualTo("test-exception");
        assertThat(progressMonitor.getLabelKey()).isEqualTo("Progress.ProgressMonitorTest.error");
    }

    /**
     * Tests updating the label key.
     */
    @Test
    void testUpdateLabelKey() {
        ProgressMonitor progressMonitor = new ProgressMonitor(ProgressMonitorTest.class, "suffix");
        assertThat(progressMonitor.getLabelKey()).isEqualTo("Progress.ProgressMonitorTest.suffix");

        progressMonitor.updateLabelKey("new-suffix");
        assertThat(progressMonitor.getLabelKey()).isEqualTo("Progress.ProgressMonitorTest.new-suffix");
    }

}
