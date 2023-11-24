package com.arassec.artivact.backend.service.model.page;

import com.arassec.artivact.backend.service.model.BaseRestrictedItem;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class PageContent extends BaseRestrictedItem {

    private boolean indexPage;

    private List<Widget> widgets = new LinkedList<>();

}
