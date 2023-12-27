package com.arassec.artivact.backend.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Asset {

    private String fileName;

    private String url;

    private boolean transferable;

}
