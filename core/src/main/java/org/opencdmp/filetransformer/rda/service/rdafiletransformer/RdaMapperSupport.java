package org.opencdmp.filetransformer.rda.service.rdafiletransformer;

import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.RadioBoxDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SelectDataModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.ExternalIdentifierModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetModel;
import org.opencdmp.commonmodels.models.referencetype.ReferenceTypeFieldModel;
import org.opencdmp.filetransformer.rda.convention.ConventionService;
import org.opencdmp.filetransformer.rda.service.planblueprintsearcher.PlanBlueprintFieldSearcherService;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.enums.*;
import org.opencdmp.commonmodels.models.*;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.SectionModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commonmodels.models.reference.ReferenceFieldModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.filetransformer.rda.model.rda.*;
import org.opencdmp.filetransformer.rda.service.descriptiontemplatesearcher.TemplateFieldSearcherService;
import org.opencdmp.filetransformer.rda.service.json.JsonHandlingService;
import org.opencdmp.filetransformerbase.models.misc.*;
import org.slf4j.LoggerFactory;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Collaborators, constants and value helpers shared by the export and import mappers.
 */
public abstract class RdaMapperSupport {
    protected static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(RdaFileTransformerService.class));

    protected final JsonHandlingService jsonHandlingService;
    protected final RdaFileTransformerServiceConfiguration configuration;
    protected final TemplateFieldSearcherService templateFieldSearcherService;
    protected final PlanBlueprintFieldSearcherService blueprintFieldSearcherService;
    protected final ConventionService conventionService;

    protected static final String DATASET_SYSTEM_ALTERNATE_IDENTIFIER_PREFIX = "opencdmp_";
    protected static final String REPOSITORY_ZENODO = "Zenodo";
    protected static final String PREFIX_ORCID = "orcid";
    protected static final String ORCID_URL = "https://orcid.org/";
    /** Identifier types the export writes itself, in the lower case RDA's examples and other tools use. */
    protected static final String DMP_TYPE_DOI = "doi";
    protected static final String DMP_TYPE_OTHER = "other";
    protected static final String DMP_TYPE_ORCID = "orcid";
    protected static final String DMP_TYPE_FUNDREF = "fundref";
    protected static final String DMP_TYPE_ROR = "ror";
    protected static final String DMP_TYPE_OPENCDMP = "OPENCDMP";
    /** RDA 1.3 only allows "url" or "other" as a metadata standard identifier type. */
    protected static final String METADATA_STANDARD_TYPE_OTHER = "other";

    protected RdaMapperSupport(RdaMapperContext context) {
        this.jsonHandlingService = context.jsonHandlingService();
        this.configuration = context.configuration();
        this.templateFieldSearcherService = context.templateFieldSearcherService();
        this.blueprintFieldSearcherService = context.blueprintFieldSearcherService();
        this.conventionService = context.conventionService();
    }

    /**
     * A whole number of bytes. Accepts what number inputs produce ("1024.0", "2.5E9") as long as there is no
     * fraction; throws otherwise.
     */
    protected Long parseByteSize(String value) {
        return new java.math.BigDecimal(value.trim()).longValueExact();
    }

    /**
     * A value for an RDA boolean (is_reused): "true" or "yes" is true, "false" or "no" is false, in any case, so a
     * text, radio or select answer of Yes/No works like a Yes/No input. Null for anything else.
     */
    protected static Boolean parseBoolean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("yes")) return true;
        if (trimmed.equalsIgnoreCase("false") || trimmed.equalsIgnoreCase("no")) return false;
        return null;
    }

    /** An ISO instant ("2026-03-04T05:06:07Z"), or a date on its own ("2026-03-04", what date inputs give) read as midnight UTC. */
    protected Instant parseDateTime(String value) {
        try {
            return Instant.parse(value.trim());
        } catch (java.time.format.DateTimeParseException e) {
            return LocalDate.parse(value.trim()).atStartOfDay(ZoneOffset.UTC).toInstant();
        }
    }

    /**
     * A plain identifier key (rda.dmp.project.funding.funder_id, ...grant_id): a picked reference on a reference
     * input, the identifier as text on any other input.
     */
    protected org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel writeIdentifier(org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel, FieldModel templateFieldModel, String identifier) {
        if (templateFieldModel.getData() instanceof ReferenceTypeDataModel referenceTypeDataModel) {
            ReferenceModel referenceModel = new ReferenceModel();
            referenceModel.setReference(identifier);
            referenceModel.setType(referenceTypeDataModel.getReferenceType());
            fieldModel.setReferences(new ArrayList<>(List.of(referenceModel)));
            return fieldModel;
        }
        return this.buildFieldModel(fieldModel, templateFieldModel, identifier, null, null, null);
    }

    /** Does any input of the question carry one of the keys? */
    protected boolean asksForAny(FieldSetModel question, List<String> semantics) {
        if (question == null || question.getFields() == null) return false;
        return question.getFields().stream().anyMatch(field -> field.getSemantics() != null && field.getSemantics().stream().anyMatch(semantics::contains));
    }

    /** Plan-level objects an RDA file holds once, but that a blueprint and every template may each have questions for. */
    protected static final String OBJECT_COST = "rda.dmp.cost";
    protected static final String OBJECT_CONTRIBUTOR = "rda.dmp.contributor";
    protected static final String OBJECT_PROJECT = "rda.dmp.project";
    protected static final String OBJECT_ALTERNATE_IDENTIFIER = "rda.dmp.alternate_identifier";
    protected static final String OBJECT_RELATED_IDENTIFIER = "rda.dmp.related_identifier";
    protected static final String OBJECT_AFFILIATION = "rda.dmp.affiliation";

    /**
     * Imports a plan-level object only where it is first asked for. The export gathers these objects from the
     * blueprint and from every description, so writing the same contributors into each of them would multiply
     * them on the next export.
     */
    protected void placeOnce(Set<String> placed, String object, Map<String, PropertyDefinitionFieldSetModel> answers,
                             java.util.function.Supplier<Map<String, PropertyDefinitionFieldSetModel>> build) {
        if (placed.contains(object)) return;
        Map<String, PropertyDefinitionFieldSetModel> built = build.get();
        boolean wrote = built.values().stream().anyMatch(x -> x != null && x.getItems() != null && !x.getItems().isEmpty());
        if (wrote) placed.add(object);
        this.mergeAnswers(answers, built);
    }

    /**
     * Adds one import builder's answers to those gathered so far. Answers to the same question are merged by
     * position, so builders filling different inputs of a question answered once still give one answer; an input
     * keeps the value it was given first. A builder that found nothing for a question leaves it alone.
     */
    protected void mergeAnswers(Map<String, PropertyDefinitionFieldSetModel> answers, Map<String, PropertyDefinitionFieldSetModel> addition) {
        for (Map.Entry<String, PropertyDefinitionFieldSetModel> entry : addition.entrySet()) {
            PropertyDefinitionFieldSetModel added = entry.getValue();
            if (added == null || added.getItems() == null || added.getItems().isEmpty()) continue;
            PropertyDefinitionFieldSetModel existing = answers.get(entry.getKey());
            if (existing == null || existing.getItems() == null || existing.getItems().isEmpty()) {
                answers.put(entry.getKey(), added);
                continue;
            }
            for (int i = 0; i < added.getItems().size(); i++) {
                PropertyDefinitionFieldSetItemModel item = added.getItems().get(i);
                if (i < existing.getItems().size()) {
                    PropertyDefinitionFieldSetItemModel target = existing.getItems().get(i);
                    if (target.getFields() == null) target.setFields(new HashMap<>());
                    if (item.getFields() != null) item.getFields().forEach(target.getFields()::putIfAbsent);
                } else {
                    item.setOrdinal(existing.getItems().size());
                    existing.getItems().add(item);
                }
            }
        }
    }

    private static final java.util.regex.Pattern ORCID = java.util.regex.Pattern.compile("(?:https?://(?:www\\.)?orcid\\.org/)?\\d{4}-\\d{4}-\\d{4}-\\d{3}[\\dX]/?");

    /**
     * The type of a person identifier that was given without one (RDA requires it): an ORCID, bare or as an
     * orcid.org URL, is "orcid", any other URL "url", anything else "other".
     */
    protected static String inferIdentifierType(String identifier) {
        if (identifier == null) return DMP_TYPE_OTHER;
        String value = identifier.trim();
        if (ORCID.matcher(value).matches()) return DMP_TYPE_ORCID;
        if (value.startsWith("http://") || value.startsWith("https://")) return "url";
        return DMP_TYPE_OTHER;
    }

    private static final java.util.regex.Pattern DOI = java.util.regex.Pattern.compile("(?:https?://(?:dx\\.)?doi\\.org/|doi:)?10\\.\\d{4,9}/\\S+", java.util.regex.Pattern.CASE_INSENSITIVE);

    /**
     * The type of a plan or dataset identifier that was given without one: a DOI (bare, doi: or doi.org) is "doi",
     * any other URL "url", anything else "other".
     */
    protected static String inferPersistentIdType(String identifier) {
        if (identifier == null) return DMP_TYPE_OTHER;
        String value = identifier.trim();
        if (DOI.matcher(value).matches()) return DMP_TYPE_DOI;
        if (value.startsWith("http://") || value.startsWith("https://")) return "url";
        return DMP_TYPE_OTHER;
    }

    private static final java.util.regex.Pattern ROR = java.util.regex.Pattern.compile("(?:https?://ror\\.org/)?0[a-hj-km-np-tv-z0-9]{6}\\d{2}/?");

    /**
     * The type of a funder identifier that was given without one: a ROR id, bare or as a ror.org URL, is "ror", which
     * RDA 1.3 recommends; anything else "fundref".
     */
    protected static String inferFunderIdType(String identifier) {
        if (identifier != null && ROR.matcher(identifier.trim()).matches()) return DMP_TYPE_ROR;
        return DMP_TYPE_FUNDREF;
    }

    private static final java.util.regex.Pattern RE3DATA = java.util.regex.Pattern.compile("(?:https?://(?:dx\\.)?doi\\.org/|doi:)?10\\.17616/R3\\w+/?|https?://(?:www\\.)?re3data\\.org/repository/\\S+", java.util.regex.Pattern.CASE_INSENSITIVE);

    /** The type of a host identifier that was given without one: a re3data DOI or record URL is "re3data", anything else as for a plan or dataset identifier. */
    protected static String inferHostIdType(String identifier) {
        if (identifier != null && RE3DATA.matcher(identifier.trim()).matches()) return "re3data";
        return inferPersistentIdType(identifier);
    }

    protected Language buildRdaLanguage(String language){
        if (language == null) return null;
        String rdaLanguage = this.configuration.getRdaFileTransformerServiceProperties().getLanguageMap().getOrDefault(language, null);

        return rdaLanguage != null ? Language.fromValue(rdaLanguage) : null;
    }

    /**
     * The answers that describe one RDA object each, for the given keys. The questions read are those with an input
     * carrying one of the keys (in template order), then those whose answer options stand for one.
     * <ul>
     *     <li>A question inside a section describing an object that the keys belong to (e.g. a section whose
     *     semantics hold {@code rda.dataset.distribution}): the answers of all such questions of that section are one
     *     group, so separate questions make up one object. The outermost such section counts.</li>
     *     <li>Any other question: each answer is its own group, as before objects existed.</li>
     * </ul>
     * A group whose chosen answer option exports nothing is left out.
     */
    protected List<AnswerGroup> groups(DescriptionTemplateModel template, Map<String, PropertyDefinitionFieldSetModel> answers, List<String> semantics) {
        List<? extends SectionModel> sections = template == null || template.getDefinition() == null ? null : template.getDefinition().getSections();
        return this.groups(this.templateFieldSearcherService.searchFieldSetsBySemantics(template, semantics), sections, answers, semantics);
    }

    protected List<AnswerGroup> groups(PlanBlueprintModel blueprint, Map<String, PropertyDefinitionFieldSetModel> answers, List<String> semantics) {
        List<? extends SectionModel> sections = blueprint == null || blueprint.getDefinition() == null ? null : blueprint.getDefinition().getSections();
        return this.groups(this.blueprintFieldSearcherService.searchFieldSetsBySemantics(blueprint, semantics), sections, answers, semantics);
    }

    /** A question with the objects the sections around it describe, outermost first, each tagged with its section. */
    private record Placed(FieldSetModel question, List<String> sectionObjects) {
    }

    private List<AnswerGroup> groups(List<FieldSetModel> byInputs, List<? extends SectionModel> sections, Map<String, PropertyDefinitionFieldSetModel> answers, List<String> semantics) {
        Map<String, Placed> placed = new LinkedHashMap<>();
        this.place(sections, new ArrayList<>(), placed);

        List<FieldSetModel> questions = new ArrayList<>(byInputs);
        Set<String> listed = new HashSet<>();
        byInputs.forEach(x -> listed.add(x.getId()));
        for (Placed question : placed.values()) {
            if (!listed.contains(question.question().getId()) && new AnswerGroup(question.question(), new org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel()).asksForAnyOf(semantics)) {
                questions.add(question.question());
            }
        }

        List<Object> slots = new ArrayList<>();
        Map<String, List<AnswerGroup.Member>> bySection = new HashMap<>();
        for (FieldSetModel question : questions) {
            Placed where = placed.get(question.getId());
            String sectionObject = where == null ? null : where.sectionObjects().stream().filter(x -> covers(x.substring(x.indexOf('|') + 1), semantics)).findFirst().orElse(null);
            for (org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel answer : this.findFieldSetValue(question, answers)) {
                if (sectionObject == null) {
                    slots.add(new AnswerGroup(question, answer));
                } else {
                    bySection.computeIfAbsent(sectionObject, k -> {
                        slots.add(k);
                        return new ArrayList<>();
                    }).add(new AnswerGroup.Member(question, answer));
                }
            }
        }
        List<AnswerGroup> groups = new ArrayList<>();
        for (Object slot : slots) {
            AnswerGroup group = slot instanceof AnswerGroup answerGroup ? answerGroup : new AnswerGroup(bySection.get((String) slot));
            if (!group.exportsNothing()) groups.add(group);
        }
        return groups;
    }

    // ---- import side of objects and answer options ----

    /** The questions given, then every other question of the definition with an answer option standing for a key under {@code object}. */
    protected List<FieldSetModel> withAnswerOptionsFor(List<? extends SectionModel> sections, List<FieldSetModel> questions, String object) {
        Map<String, Placed> placed = new LinkedHashMap<>();
        this.place(sections, new ArrayList<>(), placed);
        List<FieldSetModel> result = new ArrayList<>(questions);
        Set<String> listed = new HashSet<>();
        questions.forEach(x -> listed.add(x.getId()));
        for (Placed question : placed.values()) {
            if (listed.contains(question.question().getId())) continue;
            boolean stands = question.question().getAllField() != null && question.question().getAllField().stream()
                    .flatMap(field -> answerOptions(field).stream())
                    .anyMatch(option -> option.semantics() != null && option.semantics().stream().anyMatch(x -> x.getKey() != null && covers(object, List.of(x.getKey()))));
            if (stands) result.add(question.question());
        }
        return result;
    }

    /** A select or radio option of a definition input, with what it stands for. */
    protected record AnswerOption(String value, List<org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SemanticValueModel> semantics, boolean exportsNothing) {
    }

    protected static List<AnswerOption> answerOptions(org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel field) {
        List<AnswerOption> options = new ArrayList<>();
        if (field.getData() instanceof SelectDataModel select && select.getOptions() != null) {
            select.getOptions().forEach(x -> options.add(new AnswerOption(x.getValue(), x.getSemantics(), x.isExportsNothing())));
        } else if (field.getData() instanceof RadioBoxDataModel radio && radio.getOptions() != null) {
            radio.getOptions().forEach(x -> options.add(new AnswerOption(x.getValue(), x.getSemantics(), x.isExportsNothing())));
        }
        return options;
    }

    /**
     * The option of the input whose key values all match the imported object's (see {@link #flatten}); when several
     * match, the one standing for the most keys. Null when none does. Keys in {@code answeredElsewhere} are left out
     * of the comparison: an input carrying them wins over the option on export, so the file may hold another value.
     */
    protected static String chooseOption(org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel field, Map<String, String> values, Set<String> answeredElsewhere) {
        String best = null;
        int bestScore = 0;
        for (AnswerOption option : answerOptions(field)) {
            if (option.semantics() == null || option.semantics().isEmpty()) continue;
            List<org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SemanticValueModel> compared = option.semantics().stream()
                    .filter(x -> x.getKey() != null && !answeredElsewhere.contains(x.getKey())).toList();
            if (compared.isEmpty()) continue;
            boolean all = compared.stream().allMatch(x -> x.getValue() != null && x.getValue().equalsIgnoreCase(values.get(x.getKey())));
            if (all && compared.size() > bestScore) {
                best = option.value();
                bestScore = compared.size();
            }
        }
        return best;
    }

    /** Every key some input of the definition carries. */
    protected static Set<String> keysOnInputs(List<? extends SectionModel> sections) {
        Set<String> keys = new HashSet<>();
        if (sections == null) return keys;
        for (SectionModel section : sections) {
            if (section.getFieldSets() != null) {
                for (FieldSetModel question : section.getFieldSets()) {
                    if (question.getAllField() == null) continue;
                    question.getAllField().forEach(field -> { if (field.getSemantics() != null) keys.addAll(field.getSemantics()); });
                }
            }
            keys.addAll(keysOnInputs(section.getSections()));
        }
        return keys;
    }

    /**
     * An imported RDA object as semantic key values, e.g. a distribution as {@code rda.dataset.distribution.title},
     * {@code rda.dataset.distribution.host.url}, ...; of a list, the first element counts.
     */
    protected Map<String, String> flatten(Object rdaObject, String key) {
        Map<String, String> values = new HashMap<>();
        if (rdaObject == null) return values;
        String json = this.jsonHandlingService.toJsonSafe(rdaObject);
        if (json == null) return values;
        this.flatten(this.jsonHandlingService.fromJsonSafe(Object.class, json), key, values);
        return values;
    }

    private void flatten(Object node, String key, Map<String, String> values) {
        if (node instanceof Map<?, ?> map) {
            map.forEach((k, v) -> this.flatten(v, key + "." + k, values));
        } else if (node instanceof List<?> list) {
            if (!list.isEmpty()) this.flatten(list.getFirst(), key, values);
        } else if (node != null) {
            values.putIfAbsent(key, node.toString());
        }
    }

    /**
     * For an object the imported file does not have (a dataset without distributions): the answer options saying
     * "nothing to export" in the questions about it, chosen.
     */
    protected Map<String, PropertyDefinitionFieldSetModel> chooseNothingToExport(List<? extends SectionModel> sections, String object) {
        Map<String, Placed> placed = new LinkedHashMap<>();
        this.place(sections, new ArrayList<>(), placed);
        Map<String, PropertyDefinitionFieldSetModel> answers = new HashMap<>();
        for (Placed question : placed.values()) {
            boolean about = question.sectionObjects().stream().anyMatch(x -> covers(object, List.of(x.substring(x.indexOf('|') + 1))))
                    || (question.question().getSemantics() != null && question.question().getSemantics().stream().anyMatch(x -> covers(object, List.of(x))));
            if (!about || question.question().getAllField() == null) continue;
            Map<String, org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel> fields = new HashMap<>();
            for (org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldModel field : question.question().getAllField()) {
                answerOptions(field).stream().filter(AnswerOption::exportsNothing).findFirst().ifPresent(option -> {
                    org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel value = new org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel();
                    value.setId(field.getId());
                    fields.put(field.getId(), this.buildFieldModel(value, field, option.value(), null, null, null));
                });
            }
            if (fields.isEmpty()) continue;
            org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel item = new org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel();
            item.setOrdinal(0);
            item.setFields(fields);
            PropertyDefinitionFieldSetModel fieldSet = new PropertyDefinitionFieldSetModel();
            fieldSet.setItems(new ArrayList<>(List.of(item)));
            answers.put(question.question().getId(), fieldSet);
        }
        return answers;
    }

    /** Never more answers than a question allows: one for a question answered once, at most its maximum otherwise. */
    protected void trimToMultiplicity(List<FieldSetModel> questions, Map<String, PropertyDefinitionFieldSetModel> answers) {
        if (questions == null) return;
        for (FieldSetModel question : questions) {
            PropertyDefinitionFieldSetModel answered = answers.get(question.getId());
            if (answered == null || answered.getItems() == null) continue;
            int max = !question.isHasMultiplicity() ? 1
                    : question.getMultiplicity() != null && question.getMultiplicity().getMax() != null && question.getMultiplicity().getMax() > 0 ? question.getMultiplicity().getMax()
                    : Integer.MAX_VALUE;
            if (answered.getItems().size() > max) answered.setItems(new ArrayList<>(answered.getItems().subList(0, max)));
        }
    }

    /** Walks the sections in order, remembering for each question the objects its sections describe ("sectionId|key"). */
    private void place(List<? extends SectionModel> sections, List<String> around, Map<String, Placed> placed) {
        if (sections == null) return;
        for (SectionModel section : sections.stream().sorted(Comparator.comparing(SectionModel::getOrdinal, Comparator.nullsLast(Comparator.naturalOrder()))).toList()) {
            List<String> here = new ArrayList<>(around);
            if (section.getSemantics() != null) section.getSemantics().stream().filter(x -> x != null && !x.isBlank()).forEach(x -> here.add(section.getId() + "|" + x));
            if (section.getFieldSets() != null) {
                for (FieldSetModel question : section.getFieldSets().stream().sorted(Comparator.comparing(FieldSetModel::getOrdinal, Comparator.nullsLast(Comparator.naturalOrder()))).toList()) {
                    placed.put(question.getId(), new Placed(question, here));
                }
            }
            this.place(section.getSections(), here, placed);
        }
    }

    /** Does an object key cover one of the keys, i.e. is it one of them or a parent of one? */
    private static boolean covers(String object, List<String> semantics) {
        return semantics.stream().anyMatch(x -> x.equals(object) || x.startsWith(object + "."));
    }

    protected List<org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel> findFieldSetValue(FieldSetModel fieldSetModel, Map<String, PropertyDefinitionFieldSetModel> fieldSets){
        List<org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel> items = new ArrayList<>();
        if (fieldSetModel == null || fieldSets == null) return items;
        PropertyDefinitionFieldSetModel propertyDefinitionFieldSetModel =  fieldSets.getOrDefault(fieldSetModel.getId(), null);
        if (propertyDefinitionFieldSetModel != null && propertyDefinitionFieldSetModel.getItems() != null) return propertyDefinitionFieldSetModel.getItems();
        return items;
    }

    protected String getReferenceFieldValueFromSemantic(ReferenceModel referenceModel, String semantic) {
        if (referenceModel == null || referenceModel.getDefinition() == null ||referenceModel.getDefinition().getFields() == null || referenceModel.getDefinition().getFields().isEmpty()
                || referenceModel.getType() == null || referenceModel.getType().getDefinition().getFields() == null || referenceModel.getType().getDefinition().getFields().isEmpty()
                || semantic == null) return null;

        org.opencdmp.commonmodels.models.referencetype.ReferenceTypeFieldModel referenceTypeFieldModel = referenceModel.getType().getDefinition().getFields().stream().filter(x -> x.getSemantics() != null && !x.getSemantics().isEmpty() && x.getSemantics().contains(semantic)).findFirst().orElse(null);
        if (referenceTypeFieldModel == null || referenceTypeFieldModel.getCode() == null) return null;

        ReferenceFieldModel referenceFieldModel = referenceModel.getDefinition().getFields().stream().filter(x -> x.getCode() != null && x.getCode().equals(referenceTypeFieldModel.getCode())).findFirst().orElse(null);
        if (referenceFieldModel == null) return null;

        return referenceFieldModel.getValue() != null && !referenceFieldModel.getValue().isBlank() ? referenceFieldModel.getValue(): null;
    }

    protected org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel buildFieldModel(org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel, FieldModel templateFieldModel, String textValue, Date dateValue, Boolean booleanValue, List<String> textListValue){
        return this.buildFieldModel(fieldModel, templateFieldModel, textValue, dateValue, booleanValue, textListValue, null);
    }

    protected org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel buildFieldModel(org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel fieldModel, FieldModel templateFieldModel, String textValue, Date dateValue, Boolean booleanValue, List<String> textListValue, Double numberValue){
        if (templateFieldModel == null || templateFieldModel.getData() == null) return fieldModel;

        switch (templateFieldModel.getData().getFieldType()) {
            case FREE_TEXT, TEXT_AREA, RICH_TEXT_AREA -> {
                if (textValue != null && !textValue.isBlank()) fieldModel.setTextValue(textValue);
                else if (booleanValue != null) fieldModel.setTextValue(booleanValue.toString());
                else if (dateValue != null) fieldModel.setTextValue(dateValue.toString());
                else if (textListValue != null && !textListValue.isEmpty()) fieldModel.setTextValue(textListValue.getFirst());
                else if (numberValue != null) fieldModel.setTextValue(String.valueOf(numberValue));
            }
            case BOOLEAN_DECISION, CHECK_BOX -> {
                if (booleanValue != null) fieldModel.setBooleanValue(booleanValue);
                if (textValue != null && !textValue.isBlank()) {
                    // "yes"/"no" come from keys that take yes, no or unknown; "unknown" leaves the input unanswered
                    if (textValue.equalsIgnoreCase("true") || textValue.equalsIgnoreCase("yes")) fieldModel.setBooleanValue(true);
                    else if (textValue.equalsIgnoreCase("false") || textValue.equalsIgnoreCase("no")) fieldModel.setBooleanValue(false);
                }
            }
            case NUMBER -> {
                if (numberValue != null) fieldModel.setNumberValue(numberValue);
                if (textValue != null && !textValue.isBlank()) {
                    try {
                        fieldModel.setNumberValue(Double.valueOf(textValue));
                    } catch (Exception e) {
                        logger.warn("invalid double parse from value: " + textValue);
                    }
                }
                if (textListValue != null && !textListValue.isEmpty()) {
                    try {
                        fieldModel.setNumberValue(Double.valueOf(textListValue.getFirst()));
                    } catch (Exception e) {
                        logger.warn("invalid double parse from value: " + textListValue.getFirst());
                    }
                }
            }
            case DATE_PICKER -> {
                if (dateValue != null) fieldModel.setDateValue(dateValue.toInstant());
                else if (textValue != null && !textValue.isBlank()) {
                    try {
                        fieldModel.setDateValue(Instant.parse(textValue));
                    } catch (Exception e) {
                        try {
                            fieldModel.setDateValue(LocalDate.parse(textValue, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(ZoneOffset.UTC).toInstant());
                        } catch (Exception eχ) {
                            logger.warn("invalid instant parse from value: " + textValue);
                        }
                    }

                }
            }
            case DATASET_IDENTIFIER, VALIDATION -> {
                if (textValue != null && !textValue.isBlank()) {
                    ExternalIdentifierModel externalIdentifierModel = new ExternalIdentifierModel();
                    externalIdentifierModel.setIdentifier(textValue);
                    externalIdentifierModel.setType(textValue);
                    fieldModel.setExternalIdentifier(externalIdentifierModel);
                }
            }
            case TAGS -> {
                if (textListValue != null && !textListValue.isEmpty()) {
                    fieldModel.setTextListValue(textListValue);
                } else if (textValue != null && !textValue.isBlank()) {
                    fieldModel.setTextListValue(List.of(textValue));
                }
            }
            case SELECT -> {
                SelectDataModel selectDataModel = (SelectDataModel) templateFieldModel.getData();
                if (selectDataModel != null && selectDataModel.getOptions() != null && !selectDataModel.getOptions().isEmpty()){
                    if (textListValue != null && !textListValue.isEmpty()) {
                        fieldModel.setTextListValue(textListValue);
                    } else if (textValue != null && !textValue.isBlank()) {
                        fieldModel.setTextListValue(List.of(textValue));
                    }
                }

            }
            case RADIO_BOX -> {
                if (textValue != null && !textValue.isBlank()) {
                    RadioBoxDataModel radioBoxModel = (RadioBoxDataModel)templateFieldModel.getData();
                    if (radioBoxModel != null && radioBoxModel.getOptions() != null && !radioBoxModel.getOptions().isEmpty()){
                        for (RadioBoxDataModel.RadioBoxOptionModel option : radioBoxModel.getOptions()){
                            if (option.getValue() != null && option.getValue().equalsIgnoreCase(textValue)) {
                                fieldModel.setTextValue(option.getValue());
                                break;
                            } else if (option.getLabel() != null && option.getLabel().equalsIgnoreCase(textValue)) {
                                fieldModel.setTextValue(option.getValue());
                                break;
                            }
                        }
                    }
                }
            }
            case REFERENCE_TYPES -> {
                if (textValue != null && !textValue.isBlank()) {
                    ReferenceModel referenceModel = new ReferenceModel();
                    ReferenceTypeDataModel referenceTypeDataModel = (ReferenceTypeDataModel) templateFieldModel.getData();
                    if (referenceTypeDataModel != null) referenceModel.setType(referenceTypeDataModel.getReferenceType());
                    referenceModel.setLabel(textValue);

                    fieldModel.setReferences(List.of(referenceModel));
                }
                else if (textListValue != null && !textListValue.isEmpty()) {
                    fieldModel.setReferences(new ArrayList<>());
                    for (String value: textListValue) {
                        ReferenceModel referenceModel = new ReferenceModel();
                        ReferenceTypeDataModel referenceTypeDataModel = (ReferenceTypeDataModel) templateFieldModel.getData();
                        if (referenceTypeDataModel != null) referenceModel.setType(referenceTypeDataModel.getReferenceType());
                        referenceModel.setLabel(value);

                        fieldModel.getReferences().add(referenceModel);
                    }
                }
            }
        }


        return fieldModel;
    }

    protected String formatTextAsDate(String fieldValue) {
        if (fieldValue == null || fieldValue.isEmpty()) return null;
        try {
            String formattedDate;

            if (fieldValue.contains("T")) {
                LocalDateTime ldt =
                        LocalDateTime.ofInstant(
                                Instant.parse(fieldValue),
                                ZoneOffset.UTC
                        );

                formattedDate = ldt.format(DateTimeFormatter.ISO_LOCAL_DATE);

            } else {
                LocalDate ld = LocalDate.parse(fieldValue);
                formattedDate = ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
            }

            return formattedDate;

        } catch (Exception e) {
            logger.warn("invalid parse date for value: {}", fieldValue);
            return null;
        }
    }
}
