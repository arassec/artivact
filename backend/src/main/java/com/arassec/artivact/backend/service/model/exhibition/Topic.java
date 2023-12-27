package com.arassec.artivact.backend.service.model.exhibition;

import com.arassec.artivact.backend.service.model.TranslatableString;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Topic {

    private TranslatableString title;

    private List<Tool> tools = new LinkedList<>();

}
