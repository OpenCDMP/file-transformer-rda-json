package org.opencdmp.filetransformer.rda.support;

import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The answers an import produced, without the blueprint and templates it echoes back (they are the ones passed in). */
public final class ImportedAnswers {

    private ImportedAnswers() {
    }

    public static Map<String, Object> of(PlanModel imported) {
        Map<String, Object> answers = new LinkedHashMap<>();
        answers.put("label", imported.getLabel());
        answers.put("plan", imported.getProperties() == null ? null : imported.getProperties().getFieldSets());
        List<Object> descriptions = new ArrayList<>();
        if (imported.getDescriptions() != null) {
            for (DescriptionModel description : imported.getDescriptions()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("label", description.getLabel());
                entry.put("answers", description.getProperties() == null ? null : description.getProperties().getFieldSets());
                descriptions.add(entry);
            }
        }
        answers.put("descriptions", descriptions);
        return answers;
    }
}
