package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.time.*;
import java.util.*;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds the dataset {@code security_and_privacy} list. */
public class SecurityAndPrivacyExporter extends RdaMapperSupport {

    public SecurityAndPrivacyExporter(RdaMapperContext context) {
        super(context);
    }

    List<SecurityAndPrivacy> buildRdaDatasetSecurityAndPrivacy(DescriptionModel model) {
        List<SecurityAndPrivacy> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE, SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION))) {
            boolean valueFound = false;
            SecurityAndPrivacy item = new SecurityAndPrivacy();

            String fieldValue = answers.value(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_TITLE);
            if (fieldValue != null) {
                item.setTitle(fieldValue);
                valueFound = true;
            }

            fieldValue = answers.value(SEMANTIC_DATASET_SECURITY_AND_PRIVACY_DESCRIPTION);
            if (fieldValue != null) {
                item.setDescription(fieldValue);
                valueFound = true;
            }

            if (valueFound) values.add(item);
        }
        return values.isEmpty() ? null : values;
    }
}
