package org.opencdmp.filetransformer.rda.service.planblueprintsearcher;

import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;

import java.util.List;

public interface PlanBlueprintFieldSearcherService {
	FieldModel findFieldBySemantic(FieldSetModel fieldSetModel, String semantic);

	List<FieldSetModel> searchFieldSetsBySemantics(PlanBlueprintModel blueprint, List<String> Semantics);

	List<FieldModel> searchFieldsBySemantics(PlanBlueprintModel blueprint, String value);
}
