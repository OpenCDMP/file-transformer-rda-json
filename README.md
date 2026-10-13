# OpenCDMP RDA JSON file transformer

**file-transformer-rda-json** exports [OpenCDMP](https://github.com/OpenCDMP/OpenCDMP) plans as machine-actionable DMPs (maDMPs) in the JSON format of the [RDA DMP Common Standard](https://github.com/RDA-DMP-Common/RDA-DMP-Common-Standard) v1.2, and imports such files as new plans. It is a Spring Boot service that implements the [file-transformer-base](https://github.com/OpenCDMP/file-transformer-base) contract; its plugin id is `rda-file-transformer`. See [File transformers](https://opencdmp.github.io/optional-services/file-transformers/) in the OpenCDMP documentation.

| Operation | Plans | Assets |
| --- | --- | --- |
| Export to RDA JSON | Yes, with their assets as `dataset` entries | No |
| Import from RDA JSON | Yes, with their `dataset` entries as assets | No |

The deposit services and evaluators of OpenCDMP also use the RDA JSON this service produces.

## Features

- Values are read from the plan blueprint and asset template questions that carry `rda.*` semantic keys, with fallbacks to plan data such as the title, the contact and the references. See the [RDA JSON semantics reference](https://opencdmp.github.io/optional-services/file-transformers/rda-json-semantics/).
- Sections and questions can describe RDA objects, such as a distribution with its host and licences, so that answers spread over several questions make up one object.
- Import reads the files published with the RDA DMP Common Standard, including properties given as a single object where the schema expects a list.
- The service reports the keys it reads in a semantics manifest, so the plan blueprint and asset template editors of OpenCDMP can suggest them and check a structure.
- Organisation settings choose the reference types read as grants, funders, projects and researchers, and the repository whose DOI becomes the plan identifier.

## Requirements

- An OpenCDMP installation and its Keycloak realm. The service accepts only tokens that carry the `plugins` audience.
- To build from source: Java 21 and Maven. The service runs on Spring Boot 4.0.

## Run with Docker

The image is [`opencdmp/file-transformer-rda-json`](https://hub.docker.com/r/opencdmp/file-transformer-rda-json) on Docker Hub. It listens on port `8080`, reads `app-<PROFILE>.env` from `/config`, and writes its logs to `/logs`. The OpenCDMP [Docker example deployment](https://opencdmp.github.io/deployment/install/) runs it.

To run it on its own, create `config/app-docker.env`:

```properties
IDP_ISSUER_URI=https://opencdmp.example.org/auth/realms/OpenCDMP
IDP_CLAIMS_ROLES_PATH=plugins.roles
SECURITY_AUDIENCE=plugins
STORAGE_PATH=/storage
```

and start the container:

```bash
docker run -d --name file-transformer-rda-json \
  -e PROFILE=docker -e SERVER_PORT=8080 -p 8080:8080 \
  -v "$PWD/config:/config" -v "$PWD/logs:/logs" \
  opencdmp/file-transformer-rda-json:<tag>
```

At startup the service downloads the OpenID configuration from `IDP_ISSUER_URI`, so Keycloak must be reachable at that address. `/actuator/health` reports when the service is up.

## Build from source

```bash
mvn clean package
```

The build runs the tests and produces `web/target/file-transformer-rda-web-1.0.0-SNAPSHOT.jar`.

The tests in `core` export and re-import a set of plans, and import the examples of the RDA DMP Common Standard, against snapshots in `core/src/test/resources/snapshots/`. To add a plan captured from a running installation as a test, see [core/src/test/resources/fixtures/README.md](core/src/test/resources/fixtures/README.md).

To run the JAR, set the required variables and start it. The realm must be reachable:

```bash
export IDP_ISSUER_URI=http://opencdmp.localhost:8180/auth/realms/OpenCDMP
export IDP_CLAIMS_ROLES_PATH=plugins.roles
export SECURITY_AUDIENCE=plugins
export STORAGE_PATH=./storage
export LOGGING_PATH=./logs
java -jar web/target/file-transformer-rda-web-1.0.0-SNAPSHOT.jar
```

Without `SERVER_PORT`, the service listens on port `8086`. [Run and test a plugin locally](https://opencdmp.github.io/developers/plugins/run-locally/) shows how to get a token, call the service, and register it with a local installation.

The `Dockerfile` builds the service from source:

```bash
docker build -t file-transformer-rda-json .
```

## Configuration

Set these as environment variables or in `app-<PROFILE>.env`:

| Variable | Purpose |
| --- | --- |
| `IDP_ISSUER_URI`, `IDP_CLAIMS_ROLES_PATH`, `SECURITY_AUDIENCE` | Keycloak realm, roles path (`plugins.roles`) and audience (`plugins`) of the tokens the service accepts. Required. |
| `STORAGE_PATH`, `RDA_FILE_TRANSFORMER_USE_SHARED_STORAGE` | Folder for temporary and shared files, and whether files are exchanged with OpenCDMP through a shared folder. |
| `RDA_FILE_TRANSFORMER_*_CODE` | Codes of the reference types the service reads, when they differ from the defaults. The grants, funders, projects and researchers codes are the defaults of the organisation settings. |
| `PROFILE`, `SERVER_PORT`, `LOGGING_*` | Profile, port and logging, as for every plugin service. |

[File transformer services: RDA JSON](https://opencdmp.github.io/deployment/configuration/backend/file-transformers/#rda-json-file-transformer) and [Settings common to all plugin services](https://opencdmp.github.io/deployment/configuration/plugin-services/#settings-common-to-all-plugin-services) list every variable with its default. Organisation administrators adjust the [organisation settings](https://opencdmp.github.io/optional-services/file-transformers/rda-json-semantics/#organisation-settings) in OpenCDMP.

### Register with OpenCDMP

Register the service with its address and the plugin id `rda-file-transformer`: for the whole installation in the API service configuration, or for one organisation with **Add plugin** under **Settings > Plugins**. See [Registering plugin services](https://opencdmp.github.io/deployment/configuration/plugin-services/).

## API

The service implements these endpoints of `FileTransformerController`:

| Method | Path | Request body | Response |
| --- | --- | --- | --- |
| `POST` | `/api/file-transformer/export/plan?format=json` | `PlanModel` | `FileEnvelopeModel` with the RDA JSON file |
| `POST` | `/api/file-transformer/import/plan` | `PlanImportModel` | `PlanModel` |
| `POST` | `/api/file-transformer/preprocessing/plan` | `FileEnvelopeModel` with the RDA JSON file | `PreprocessingPlanModel`: the plan title and its datasets |
| `POST` | `/api/file-transformer/preprocessing/description` | `FileEnvelopeModel` with one RDA `dataset` object | `PreprocessingDescriptionModel`: the dataset title |
| `GET` | `/api/file-transformer/formats` | - | `FileTransformerConfiguration`, with the semantics manifest |

The `description` export and import endpoints return an error. The models come from [common-models](https://github.com/OpenCDMP/common-models) and file-transformer-base; see the [file transformer plugin guide](https://opencdmp.github.io/developers/plugins/file-transformers/).

## How plan blueprints and asset templates map to RDA objects

The export reads the plan blueprint (the plan, `rda.dmp.*`) and the asset templates (each dataset, `rda.dataset.*`) through semantic keys. Where a key is placed decides what the RDA JSON looks like. The [semantics guide](https://opencdmp.github.io/administering/content-management/templates/semantics/#objects-several-questions-one-exported-object) explains how to set keys and objects, and the [RDA JSON semantics reference](https://opencdmp.github.io/optional-services/file-transformers/rda-json-semantics/) lists every key, its fallback and how it is imported.

### Three places a key can go

| Where | Example | What it does |
|---|---|---|
| Input | `rda.dataset.distribution.title` on a text input | The answer becomes that value. |
| Question ("This question is …") | `rda.dataset.distribution.license` on a repeating question | Each answer of the question is one object. |
| Section ("This section is …") | `rda.dataset.distribution` on a section | Everything answered in the section, its subsections included, is one object. |

A question or section carries an object key (`rda.dataset.distribution`, `rda.dmp.project`, ...), never a value key. Inputs always carry value keys.

### Rules for objects

1. **A section object makes exactly one object.** Values are taken from the first answer that has them, so a repeating question inside such a section exports only its first answer.
2. **Without a section object, every answer of every question is its own object.** Values of one object spread over several questions then come out as several half-filled objects, which usually fail the RDA schema (the editor reports this as "fill … with no section or question object").
3. **The same object on a section and on a question inside it: the section wins.** You get one object; the question's key adds nothing.
4. **Objects nest.** Inside a section that is `rda.dataset.distribution`, a repeating question that is `rda.dataset.distribution.license` gives one distribution with one licence per answer. The same works for `...distribution.host`.
5. **Two separate sections are two objects, even with the same key.** A section object belongs to that section only; values are never merged across sections. The editor flags this as "same object on two sections or questions".
6. **To merge values from different sections, nest them.** Put the sections under one parent section and give the parent the object. The outermost section with the object decides the group.

```
Section "Data sharing"         ← rda.dataset.distribution        → ONE distribution
 ├─ Subsection "Repository"    ← rda.dataset.distribution.host   (optional, nested)
 │    └─ host title, host url
 └─ Subsection "Access"
      ├─ data access
      └─ Question "Licences"   ← rda.dataset.distribution.license, repeating → one licence per answer
```

| Layout | Result in the RDA JSON |
|---|---|
| Two sibling sections, both `rda.dataset.distribution` | 2 distributions |
| One section `rda.dataset.distribution` and, in another section, a question `rda.dataset.distribution` | 2 or more distributions (1 from the section, 1 per answer of the question) |
| Subsections under one parent that is `rda.dataset.distribution` | 1 distribution |

### Single objects and lists

Use a section object when the plan or dataset has one of it, and a repeating question when RDA holds a list and users may give several:

| Object | Put it on |
|---|---|
| `rda.dmp.contact`, `rda.dmp.dmp_id`, `rda.dmp.project` (one project, one funding) | Section |
| `rda.dmp.contributor`, `rda.dmp.cost`, `rda.dmp.alternate_identifier`, `rda.dmp.related_identifier` | Repeating question (section without object) |
| `rda.dataset.distribution` | Section for one distribution; repeating question for several |
| `rda.dataset.distribution.license`, `...distribution.host` | Question inside the distribution |

### Other mappings

- **Plan identity:** `dmp_id` is the DOI the plan received from the repository set in the organisation settings (Zenodo by default), else the answered identifier, else the plan's internal id. `title`, `description`, `created` and `modified` fall back to the plan's own values when not answered.
- **Reference inputs** with an object key (`rda.dmp.contributor`, `rda.dataset.distribution`, `...host`, `...license`) fill the whole object from the picked reference's own fields.
- **Answer options** of a select or radio input can stand for key values (an option "Zenodo" giving the host title and URL), or say there is nothing to export for the object.
- **Yes/No values:** `is_reused` takes a Yes/No input or a text, radio or select answer of `true`/`false` or `yes`/`no`, in any case. `personal_data`, `sensitive_data`, `ethical_issues_exist` and `support_versioning` take `yes`, `no` or `unknown`.
- **Plan-level objects in asset templates:** an asset template may also ask for `rda.dmp.*` objects (costs, contributors, projects). They are gathered from the plan blueprint and every asset into the one `dmp`.

## Related resources

- [File transformers overview](https://opencdmp.github.io/optional-services/file-transformers/)
- [RDA JSON semantics reference](https://opencdmp.github.io/optional-services/file-transformers/rda-json-semantics/)
- [Semantics guide](https://opencdmp.github.io/administering/content-management/templates/semantics/)
- [File transformer services configuration](https://opencdmp.github.io/deployment/configuration/backend/file-transformers/#rda-json-file-transformer)
- [Registering plugin services](https://opencdmp.github.io/deployment/configuration/plugin-services/)
- [File transformer plugin guide](https://opencdmp.github.io/developers/plugins/file-transformers/)

## Contributing

Report bugs and suggest changes in [GitHub Issues](https://github.com/OpenCDMP/file-transformer-rda-json/issues). See [Contributing](https://opencdmp.github.io/developers/contributing/) for how to propose a change.

## License

file-transformer-rda-json is licensed under the [European Union Public Licence (EUPL) v1.2](LICENSE).

## Contact

Questions: email opencdmp at cite.gr.
