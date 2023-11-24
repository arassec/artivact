package com.arassec.artivact.backend.service.model.page;

import lombok.Data;

@Data
public class Page {

    private String id;

    private Integer version;

    private PageContent pageContent;

}
