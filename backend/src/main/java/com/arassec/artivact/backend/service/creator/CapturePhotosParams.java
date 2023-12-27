package com.arassec.artivact.backend.service.creator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapturePhotosParams {

    private int numPhotos;

    private boolean useTurnTable;

    private int turnTableDelay;

    private boolean removeBackgrounds;

}
