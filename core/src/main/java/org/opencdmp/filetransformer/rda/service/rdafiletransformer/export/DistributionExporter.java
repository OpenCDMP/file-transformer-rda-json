package org.opencdmp.filetransformer.rda.service.rdafiletransformer.export;

import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformerbase.models.misc.*;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.AnswerGroup;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperContext;
import org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaMapperSupport;
import static org.opencdmp.filetransformer.rda.service.rdafiletransformer.RdaSemantics.*;

/** Builds the dataset {@code distribution} list with each distribution's {@code host} and {@code license} list. */
public class DistributionExporter extends RdaMapperSupport {

    public DistributionExporter(RdaMapperContext context) {
        super(context);
    }

    List<Distribution> buildRdaDistribution(DescriptionModel model){
        List<Distribution> values = new ArrayList<>();
        for (AnswerGroup answers : this.groups(model.getDescriptionTemplate(), model.getProperties().getFieldSets(), List.of(
                SEMANTIC_DATASET_DISTRIBUTION,
                SEMANTIC_DATASET_DISTRIBUTION_LICENCE,SEMANTIC_DATASET_DISTRIBUTION_ISSUED, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE,
                SEMANTIC_DATASET_DISTRIBUTION_TITLE, SEMANTIC_DATASET_DISTRIBUTION_FORMAT,
                SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL, SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS,
                SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE, SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL, SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL,
                SEMANTIC_DATASET_DISTRIBUTION_HOST,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE, SEMANTIC_DATASET_DISTRIBUTION_HOST_URL, SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE, SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION, SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION, SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM,
                SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE, SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING,SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_IDENTIFIER, SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_TYPE))) {
            // several picked distributions or repositories: one distribution each, sharing the answer's other values
            for (List<ReferenceModel> distributionReferences : oneEach(answers.input(SEMANTIC_DATASET_DISTRIBUTION))) {
                for (List<ReferenceModel> hostReferences : oneEach(answers.input(SEMANTIC_DATASET_DISTRIBUTION_HOST))) {
                    Distribution distribution = this.buildRdaDistribution(answers, distributionReferences, hostReferences);
                    if (distribution != null) values.add(distribution);
                }
            }
        }
        return values.isEmpty() ? null : values;
    }

    /** Each picked reference on its own, or one empty list when the input picks none. */
    private static List<List<ReferenceModel>> oneEach(org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel input) {
        if (input == null || input.getReferences() == null || input.getReferences().isEmpty()) return List.of(List.of());
        return input.getReferences().stream().map(List::of).toList();
    }

    /** One distribution from an answer, for one picked distribution reference and one picked repository (each may be absent); null when there is nothing in it. */
    Distribution buildRdaDistribution(AnswerGroup answers, List<ReferenceModel> distributionReferences, List<ReferenceModel> hostReferences) {
        boolean valueFound = false;
        Distribution item = new Distribution();

        if (!distributionReferences.isEmpty()) {
            item = this.buildDistributionFromReferences(distributionReferences);
            valueFound = true;
        }
        String fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_TITLE);
        if (fieldValue != null) {
            item.setTitle(fieldValue);
            valueFound = true;
        }

        List<String> fieldValues = answers.values(SEMANTIC_DATASET_DISTRIBUTION_FORMAT);
        if (item.getFormat() == null) item.setFormat(new ArrayList<>());
        if (fieldValues != null && !fieldValues.isEmpty()) {
            item.getFormat().addAll(fieldValues);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL);
        if (fieldValue != null) {
            try {
                item.setDownloadUrl(URI.create(fieldValue));
                valueFound = true;
            } catch (Exception ex) {
                logger.warn("invalid dataset distribution download url value: " + fieldValue);
            }
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION);
        if (fieldValue != null) {
            item.setDescription(fieldValue);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS);
        if (fieldValue != null) {
            try {
                item.setDataAccess(Distribution.DataAccess.fromValue(fieldValue));
                valueFound = true;
            } catch (Exception ex) {
                logger.warn("invalid distribution data access type: " + fieldValue);
            }
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE);
        if (fieldValue != null) {
            try {
                item.setByteSize(this.parseByteSize(fieldValue));
                valueFound = true;
            } catch (Exception ex) {
                logger.warn("invalid distribution byte size value: " + fieldValue);
            }
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL);
        if (fieldValue != null) {
            try{
                Date date = Date.from(LocalDate.parse(fieldValue, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(ZoneOffset.UTC).toInstant());
                item.setAvailableUntil(date);
                valueFound = true;
            } catch (Exception ex){
                logger.warn("invalid distribution available until date value: " + fieldValue);
            }
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL);
        if (fieldValue != null) {
            try{
                item.setAccessUrl(URI.create(fieldValue));
                valueFound = true;
            }catch (Exception ex){
                logger.warn("invalid distribution access url value: " + fieldValue);
            }

        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_ISSUED);
        if (fieldValue != null) {
            try {
                Date date = Date.from(LocalDate.parse(fieldValue, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(ZoneOffset.UTC).toInstant());
                item.setIssued(date);
                valueFound = true;
            } catch (Exception ex){
                logger.warn("invalid distribution issued date value: " + fieldValue);
            }
        }

        if (valueFound && item.getLicense() != null) item.getLicense().addAll(this.buildRdaDistributionLicense(answers));
        else item.setLicense(this.buildRdaDistributionLicense(answers));

        item.setHost(this.buildRdaDistributionHost(answers, item.getHost(), hostReferences));
        // RDA requires a title: a distribution with none is named after its repository
        if (item.getTitle() == null && item.getHost() != null && item.getHost().getTitle() != null) item.setTitle(item.getHost().getTitle());
        if (valueFound || !item.getLicense().isEmpty() || item.getHost() != null) return item;
        return null;
    }

    /** The licences of one distribution: one per answer of a question describing a licence, else those of the whole group. */
    List<License> buildRdaDistributionLicense(AnswerGroup answers) {
        List<License> licenses = new ArrayList<>();
        for (AnswerGroup part : answers.parts(SEMANTIC_DATASET_DISTRIBUTION_LICENCE)) {
            org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel = part.input(SEMANTIC_DATASET_DISTRIBUTION_LICENCE);
            if (fieldModel != null && fieldModel.getReferences() != null  && !fieldModel.getReferences().isEmpty()) {
                List<License> licenseList = this.buildRdaDistributionLicenseFromReferences(fieldModel.getReferences());
                if (!licenseList.isEmpty()) licenses.addAll(licenseList);
            }

            License license = new License();

            String fieldValue = part.value(SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF);
            if (fieldValue != null) {
                try {
                    license.setLicenseRef(URI.create(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid distribution license ref uri value: " + fieldValue);
                }
            }

            fieldValue = part.value(SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE);
            if (fieldValue != null) {
                license.setStartDate(fieldValue);
            }

            if (license.getLicenseRef() != null || license.getStartDate() != null) {
                licenses.add(license);
            }
        }
        return licenses;
    }

    /** The host from the answers and the picked repository, on top of {@code base} (the host of a distribution picked as a reference), or null when there is none. */
    Host buildRdaDistributionHost(AnswerGroup answers, Host base, List<ReferenceModel> hostReferences) {
        boolean valueFound = base != null;
        Host item = base != null ? base : new Host();

        if (!hostReferences.isEmpty()) {
            item = this.buildRdaDistributionHostFromReferences(hostReferences);
            valueFound = true;
        }

        String fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE);
        if (fieldValue != null) {
            item.setTitle(fieldValue);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_URL);
        if (fieldValue != null) {
            try {
                item.setUrl(URI.create(fieldValue));
                valueFound = true;
            } catch (Exception ex) {
                logger.warn("invalid distribution host url value: " + fieldValue);
            }
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION);
        if (fieldValue != null) {
            item.setDescription(fieldValue);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE);
        if (fieldValue != null) {
            item.setStorageType(fieldValue);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE);
        if (fieldValue != null) {
            item.setBackupType(fieldValue);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY);
        if (fieldValue != null) {
            item.setAvailability(fieldValue);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY);
        if (fieldValue != null) {
            item.setBackupFrequency(fieldValue);
            valueFound = true;
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION);
        if (fieldValue != null) {
            try {
                item.setGeoLocation(Host.GeoLocation.fromValue(fieldValue));
                valueFound = true;
            } catch (Exception ex) {
                logger.warn("invalid distribution host geo location value: " + fieldValue);
            }
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING);
        if (fieldValue != null) {
            try {
                item.setSupportVersioning(Host.SupportVersioning.fromValue(fieldValue));
                valueFound = true;
            } catch (Exception ex) {
                logger.warn("invalid host support versioning value: " + fieldValue);
            }
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH);
        if (fieldValue != null) {
            try {
                item.setCertifiedWith(Host.CertifiedWith.fromValue(fieldValue));
                valueFound = true;
            } catch (Exception ex) {
                logger.warn("invalid host certified value: " + fieldValue);
            }
        }
        
        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM);
        if (item.getPidSystem() == null) item.setPidSystem(new ArrayList<>());
        if (fieldValue != null) {
            try {
                item.getPidSystem().add(PidSystem.fromValue(fieldValue));
                valueFound = true;
            } catch (Exception ex) {
                logger.warn("invalid host pid system value: " + fieldValue);
            }
        }

        if (item.getHostId() == null) item.setHostId(new ArrayList<>());
        item.getHostId().addAll(this.buildRdaDistributionHostId(answers));

        if (valueFound || (item.getHostId() != null && !item.getHostId().isEmpty())) return item;
        return null;
    }

    List<HostId> buildRdaDistributionHostId(AnswerGroup answers) {
        List<HostId> hostIds = new ArrayList<>();
        HostId hostId = new HostId();
        String fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_IDENTIFIER);
        if (fieldValue != null) {
                hostId.setIdentifier(fieldValue);
        }

        fieldValue = answers.value(SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_TYPE);
        if (fieldValue != null) {
            hostId.setType(fieldValue);
        }

        if (hostId.getIdentifier() != null && hostId.getType() == null) hostId.setType(inferPersistentIdType(hostId.getIdentifier()));
        if (hostId.getIdentifier() != null || hostId.getType() != null) {
            hostIds.add(hostId);
        }
        return hostIds;
    }

    Distribution buildDistributionFromReferences(List<ReferenceModel> referenceModels){
        Distribution item = new Distribution();

        if (referenceModels == null) return item;

        for (ReferenceModel referenceModel : referenceModels) {
            String fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_TITLE);
            if (fieldValue != null) {
                item.setTitle(fieldValue);
            } else {
                item.setTitle(referenceModel.getLabel());
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_FORMAT);
            if (fieldValue != null) {
                item.setFormat(new ArrayList<>());
                item.getFormat().add(fieldValue);
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_DOWNLOAD_URL);
            if (fieldValue != null) {
                try {
                    item.setDownloadUrl(URI.create(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid dataset distribution download url value: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_DESCRIPTION);
            if (fieldValue != null) {
                item.setDescription(fieldValue);
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_DATA_ACCESS);
            if (fieldValue != null) {
                try {
                    item.setDataAccess(Distribution.DataAccess.fromValue(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid distribution data access type: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_BYTE_SIZE);
            if (fieldValue != null) {
                try {
                    item.setByteSize(this.parseByteSize(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid distribution byte size value: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_AVAILABLE_UTIL);
            if (fieldValue != null) {
                try{
                    Date date = Date.from(LocalDate.parse(fieldValue, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(ZoneOffset.UTC).toInstant());
                    item.setAvailableUntil(date);
                } catch (Exception ex){
                    logger.warn("invalid distribution available until date value: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_ACCESS_URL);
            if (fieldValue != null) {
                try{
                    item.setAccessUrl(URI.create(fieldValue));
                }catch (Exception ex){
                    logger.warn("invalid distribution access url value: " + fieldValue);
                }

            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_ISSUED);
            if (fieldValue != null) {
                try {
                    Date date = Date.from(LocalDate.parse(fieldValue, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(ZoneOffset.UTC).toInstant());
                    item.setIssued(date);
                } catch (Exception ex){
                    logger.warn("invalid distribution issued date value: " + fieldValue);
                }
            }

            item.setHost(this.buildRdaDistributionHostFromReferences(List.of(referenceModel)));
            item.setLicense(this.buildRdaDistributionLicenseFromReferences(List.of(referenceModel)));

        }

        return item;
    }

    Host buildRdaDistributionHostFromReferences(List<ReferenceModel> referenceModels) {
        Host item = new Host();

        if (referenceModels == null) return item;

        for (ReferenceModel referenceModel : referenceModels){
            String fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_TITLE);
            if (fieldValue != null) {
                item.setTitle(fieldValue);
            } else {
                item.setTitle(referenceModel.getLabel());
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_URL);
            if (fieldValue != null) {
                try {
                    item.setUrl(URI.create(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid distribution host url value: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_DESCRIPTION);
            if (fieldValue != null) {
                item.setDescription(fieldValue);
            } else {
                item.setDescription(referenceModel.getDescription());
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_STORAGE_TYPE);
            if (fieldValue != null) {
                item.setStorageType(fieldValue);
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_TYPE);
            if (fieldValue != null) {
                item.setBackupType(fieldValue);
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_AVAILABILITY);
            if (fieldValue != null) {
                item.setAvailability(fieldValue);
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_BACKUP_FREQUENCY);
            if (fieldValue != null) {
                item.setBackupFrequency(fieldValue);
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_GEO_LOCATION);
            if (fieldValue != null) {
                try {
                    item.setGeoLocation(Host.GeoLocation.fromValue(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid distribution host geo location value: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_SUPPORT_VERSIONING);
            if (fieldValue != null) {
                try {
                    item.setSupportVersioning(Host.SupportVersioning.fromValue(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid host support versioning value: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_CERTIFIED_WITH);
            if (fieldValue != null) {
                try {
                    item.setCertifiedWith(Host.CertifiedWith.fromValue(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid host certified value: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_PID_SYSTEM);
            item.setPidSystem(new ArrayList<>());
            if (fieldValue != null) {
                try {
                    item.getPidSystem().add(PidSystem.fromValue(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid host pid system value: " + fieldValue);
                }
            }

            List<HostId> hostIds = new ArrayList<>();
            HostId hostId = new HostId();

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_IDENTIFIER);
            if (fieldValue != null) {
                hostId.setIdentifier(fieldValue);
            } else {
                hostId.setIdentifier(referenceModel.getReference());
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_HOST_ID_TYPE);
            if (fieldValue != null) {
                hostId.setType(fieldValue);
            }

            if (hostId.getIdentifier() != null && hostId.getType() == null) hostId.setType(inferPersistentIdType(hostId.getIdentifier()));
            if (hostId.getIdentifier() != null || hostId.getType() != null) {
                hostIds.add(hostId);
            }

            item.setHostId(hostIds);
        }

        return item;
    }

    List<License> buildRdaDistributionLicenseFromReferences(List<ReferenceModel> referenceModels) {
        List<License> licenses = new ArrayList<>();

        if (referenceModels == null) return licenses;

        for (ReferenceModel referenceModel : referenceModels){
            License license = new License();
            String fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_LICENSE_REF);
            if (fieldValue != null) {
                try {
                    license.setLicenseRef(URI.create(fieldValue));
                } catch (Exception ex) {
                    logger.warn("invalid distribution license ref uri value: " + fieldValue);
                }
            }

            fieldValue = this.getReferenceFieldValueFromSemantic(referenceModel, SEMANTIC_DATASET_DISTRIBUTION_LICENCE_START_DATE);
            if (fieldValue != null) {
                license.setStartDate(fieldValue);
            }

            if (license.getLicenseRef() != null || license.getStartDate() != null) {
                licenses.add(license);
            }
        }

        return licenses;
    }
}
