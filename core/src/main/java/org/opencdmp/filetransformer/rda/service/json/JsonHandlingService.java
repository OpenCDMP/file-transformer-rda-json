package org.opencdmp.filetransformer.rda.service.json;

import tools.jackson.core.JacksonException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JsonHandlingService {
    private final JsonMapper objectMapper;

    public JsonHandlingService() {
        this.objectMapper = JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                // RDA 1.3 lets contact_id, contributor_id, creator_id and metadata_standard_id be one object or a list;
                // files from other tools often use the single object, which the list-typed model must still read
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .build();
    }

    public String toJson(Object item) throws JacksonException {
        if (item == null) return null;
        return objectMapper.writeValueAsString(item);
    }

    public String toJsonSafe(Object item) {
        if (item == null) return null;
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception ex) {
            return null;
        }
    }

    public <T> T fromJson(Class<T> type, String json) throws JacksonException {
        if (json == null) return null;
        return objectMapper.readValue(json, type);
    }

    public HashMap<String, String> mapFromJson(String json) throws JacksonException {
        ObjectReader reader = objectMapper.readerFor(Map.class);
        return reader.readValue(json);
    }

    public <T> T fromJsonSafe(Class<T> type, String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            return null;
        }
    }
}
