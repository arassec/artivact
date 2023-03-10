package com.arassec.artivact.creator.standalone.core;

import com.arassec.artivact.common.ArtivactCommonConfiguration;
import com.arassec.artivact.common.util.FileUtil;
import com.arassec.artivact.creator.standalone.core.adapter.image.background.BackgroundRemovalAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.image.background.FallbackBackgroundRemovalAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.image.camera.CameraAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.image.camera.FallbackCameraAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.image.turntable.FallbackTurntableAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.image.turntable.TurntableAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.model.creator.FallbackModelCreatorAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.model.creator.ModelCreatorAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.model.editor.FallbackModelEditorAdapter;
import com.arassec.artivact.creator.standalone.core.adapter.model.editor.ModelEditorAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
@Import(ArtivactCommonConfiguration.class)
public class ArtivactCreatorConfiguration {

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/artivact-creator-labels");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public TurntableAdapter fallbackTurntableAdapter() {
        return new FallbackTurntableAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public CameraAdapter fallbackCameraAdapter(FileUtil fileUtil) {
        return new FallbackCameraAdapter(fileUtil);
    }

    @Bean
    @ConditionalOnMissingBean
    public BackgroundRemovalAdapter fallbackBackgroundRemovalAdapter() {
        return new FallbackBackgroundRemovalAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelCreatorAdapter fallbackModelCreatorAdapter() {
        return new FallbackModelCreatorAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelEditorAdapter fallbackModelEditorAdapter() {
        return new FallbackModelEditorAdapter();
    }

}
