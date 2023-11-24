package com.arassec.artivact.backend.api.model;

import com.arassec.artivact.backend.service.model.TranslatableString;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemCardData {

    private String itemId;

    private TranslatableString title;

    private String imageUrl;

    private boolean hasModel;

}
