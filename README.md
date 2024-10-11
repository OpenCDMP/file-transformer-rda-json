# File Transformer RDA JSON for OpenCDMP

**file-transformer-rda-json** is an implementation of the `file-transformer-base` package designed to handle both the **import** and **export** of JSON files based on the **[RDA DMP Common Standard](https://github.com/RDA-DMP-Common/RDA-DMP-Common-Standard)** for machine-actionable Data Management Plans (maDMPs). This microservice is built using **Spring Boot** and can be easily integrated with the OpenCDMP platform as an import/export option for RDA-compliant JSON files.

## Overview

This microservice allows users to import and export machine-actionable DMPs (maDMPs) using the **[RDA DMP Common Standard](https://github.com/RDA-DMP-Common/RDA-DMP-Common-Standard)**. It ensures that the data is structured and exchanged in a standardized JSON format that complies with the RDA (Research Data Alliance) recommendations for DMP interoperability.

- **Exports**: Supported for RDA-compliant JSON format.
- **Imports**: Supported for RDA-compliant JSON format.

## Features

- **JSON Export**: Export OpenCDMP plans and descriptions to RDA-compliant JSON format.
- **JSON Import**: Import RDA-compliant JSON files into OpenCDMP as plans and descriptions.
- **Spring Boot Microservice**: Built as a Spring Boot microservice for seamless integration with OpenCDMP.
- **Standards-Based**: Fully compliant with the **RDA DMP Common Standard** for maDMPs.

## Key Endpoints

This service implements the following endpoints as per `FileTransformerController`:

### Export Endpoints

- **POST `/export/plan`**: Export a plan to RDA-compliant JSON.
- **POST `/export/description`**: Export a description to RDA-compliant JSON.

```bash
POST /export/plan
{
    "planModel": { ... },
    "format": "json"
}
```

```bash
POST /export/description
{
    "descriptionModel": { ... },
    "format": "json"
}
```

### Import Endpoints

- **POST `/import/plan`**: Import a plan from RDA-compliant JSON.
- **POST `/import/description`**: Import a description from RDA-compliant JSON.

```bash
POST /import/plan
{
    "planImportModel": { ... }
}
```

```bash
POST /import/description
{
    "descriptionImportModel": { ... }
}
```

### Configuration Endpoint

- **GET `/formats`**: Returns supported formats for import/export (JSON for RDA DMP).

## Example

To export a plan into RDA-compliant JSON format:

```bash
POST /export/plan
{
    "planModel": { ... },
    "format": "json"
}
```

To import a plan from RDA-compliant JSON format:

```bash
POST /import/plan
{
    "planImportModel": { ... }
}
```

## License

This repository is licensed under the [EUPL 1.2 License](LICENSE).

## Contact

For questions or support regarding this implementation, please contact:

- **Email**: opencdmp at cite.gr
