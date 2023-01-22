package com.arassec.artivact.vault.backend.service.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class MediaContent {

    private List<String> images = new LinkedList<>();

    private List<String> models = new LinkedList<>();

}
