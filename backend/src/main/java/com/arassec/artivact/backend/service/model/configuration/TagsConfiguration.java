package com.arassec.artivact.backend.service.model.configuration;

import com.arassec.artivact.backend.service.model.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagsConfiguration {

    private List<Tag> tags = new LinkedList<>();

}
