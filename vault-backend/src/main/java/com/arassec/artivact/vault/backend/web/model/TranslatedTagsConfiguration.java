package com.arassec.artivact.vault.backend.web.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class TranslatedTagsConfiguration {

    List<TranslatedTag> tags = new LinkedList<>();

}
