package com.arassec.artivact.vault.backend.service;

import com.arassec.artivact.vault.backend.persistence.model.ArtivactEntity;
import com.arassec.artivact.vault.backend.persistence.repository.ArtivactEntityRepository;
import com.arassec.artivact.vault.backend.service.model.Tag;
import com.arassec.artivact.vault.backend.service.model.configuration.TagsConfiguration;
import com.arassec.artivact.vault.backend.service.model.search.ArtivactSearchData;
import com.arassec.artivact.vault.backend.service.model.search.ArtivactSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private static final String TITLE_PREFIX = "title:";

    private static final String DESCRIPTION_PREFIX = "description:";

    private static final String PROPERTIES_PREFIX = "properties:";

    private static final String TAGS_PREFIX = "tags:";

    private final ArtivactEntityRepository artivactEntityRepository;

    private final ConfigurationService configurationService;

    public ArtivactSearchResult search(List<String> roles, String searchTerm, int pageNumber, int pageSize) {
        List<ArtivactSearchData> result = new LinkedList<>();

        Arrays.stream(searchTerm.split(" ")).forEach(searchTermPart -> {
            if (searchTermPart.startsWith(TITLE_PREFIX)) {
                result.addAll(searchTitle(roles, searchTermPart.replace(TITLE_PREFIX, "")));
            } else if (searchTermPart.startsWith(DESCRIPTION_PREFIX)) {
                result.addAll(searchDescription(roles, searchTermPart.replace(DESCRIPTION_PREFIX, "")));
            } else if (searchTermPart.startsWith(PROPERTIES_PREFIX)) {
                result.addAll(searchProperties(roles, searchTermPart.replace(PROPERTIES_PREFIX, "")));
            } else if (searchTermPart.startsWith(TAGS_PREFIX)) {
                result.addAll(searchProperties(roles, searchTermPart.replace(TAGS_PREFIX, "")));
            } else {
                result.addAll(searchTitle(roles, searchTermPart));
                result.addAll(searchDescription(roles, searchTermPart));
                result.addAll(searchProperties(roles, searchTermPart));
                result.addAll(searchTags(roles, searchTermPart));
            }
        });

        return getSearchResult(result, pageNumber, pageSize);
    }

    private List<ArtivactSearchData> searchTitle(List<String> roles, String searchTerm) {
        return searchByField(roles, () -> artivactEntityRepository.findByTitleJsonContainingIgnoreCase(searchTerm));
    }

    private List<ArtivactSearchData> searchDescription(List<String> roles, String searchTerm) {
        return searchByField(roles, () -> artivactEntityRepository.findByDescriptionJsonContainingIgnoreCase(searchTerm));
    }

    private List<ArtivactSearchData> searchProperties(List<String> roles, String searchTerm) {
        return searchByField(roles, () -> artivactEntityRepository.findByPropertiesJsonContainingIgnoreCase(searchTerm));
    }

    private List<ArtivactSearchData> searchTags(List<String> roles, String searchTerm) {
        TagsConfiguration tagsConfiguration = configurationService.loadTagsConfiguration(roles);

        List<Tag> matchingTags = tagsConfiguration.getTags().stream()
                .filter(tag -> tag.getValue().toLowerCase().contains(searchTerm.toLowerCase())
                        || tag.getTranslations().values().stream()
                        .anyMatch(translationValue -> translationValue.toLowerCase().contains(searchTerm.toLowerCase())))
                .toList();

        List<ArtivactSearchData> result = new LinkedList<>();

        matchingTags.forEach(matchingTag -> result.addAll(
                searchByField(roles, () -> artivactEntityRepository.findByTagsJsonContainingIgnoreCase(matchingTag.getId())))
        );

        return result;
    }

    private List<ArtivactSearchData> searchByField(List<String> roles, ResultProvider resultProvider) {
        return resultProvider.searchResult().stream()
                .filter(artivactEntity -> isAllowed(artivactEntity.getRestrictions(), roles))
                .map(artivactEntity -> ArtivactSearchData.builder()
                        .id(artivactEntity.getId())
                        .build())
                .toList();
    }

    private ArtivactSearchResult getSearchResult(List<ArtivactSearchData> all, int pageNumber, int pageSize) {
        if (all != null && !all.isEmpty() && pageSize > 0) {
            long totalPages = all.size() / pageSize;
            if (all.size() % pageSize > 0) {
                totalPages++;
            }
            if (totalPages > 0 && totalPages > pageNumber) {
                int startIndex = pageNumber * pageSize;
                int endIndex = startIndex + pageSize;
                if (endIndex >= all.size()) {
                    endIndex = all.size();
                }
                return ArtivactSearchResult.builder()
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalPages(totalPages)
                        .data(all.subList(startIndex, endIndex))
                        .build();
            }
        }
        return ArtivactSearchResult.builder()
                .pageNumber(pageNumber)
                .pageSize(0)
                .totalPages(0)
                .data(List.of())
                .build();
    }

    private boolean isAllowed(String restrictions, List<String> roles) {
        if (!StringUtils.hasText(restrictions)) {
            return true;
        }
        return Arrays.stream(restrictions.split(",")).anyMatch(roles::contains);
    }

    private interface ResultProvider {
        List<ArtivactEntity> searchResult();
    }

}
