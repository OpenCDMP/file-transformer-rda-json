package org.opencdmp.filetransformer.rda.service.rdafiletransformer;

import org.opencdmp.filetransformer.rda.convention.ConventionService;
import org.opencdmp.filetransformer.rda.service.descriptiontemplatesearcher.TemplateFieldSearcherService;
import org.opencdmp.filetransformer.rda.service.json.JsonHandlingService;
import org.opencdmp.filetransformer.rda.service.planblueprintsearcher.PlanBlueprintFieldSearcherService;

/** The collaborators every mapper needs. */
public record RdaMapperContext(JsonHandlingService jsonHandlingService,
                               RdaFileTransformerServiceConfiguration configuration,
                               TemplateFieldSearcherService templateFieldSearcherService,
                               PlanBlueprintFieldSearcherService blueprintFieldSearcherService,
                               ConventionService conventionService) {
}
