package com.arassec.artivact.creator.standalone.ui.model;

import com.arassec.artivact.creator.standalone.core.model.ArtivactModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExportModelUserData {

    private ArtivactModel model;

    private String filename;

}
