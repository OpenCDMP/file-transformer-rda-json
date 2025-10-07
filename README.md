# File Transformer RDA JSON for OpenCDMP

`file-transformer-rda-json` is an implementation of the [file-transformer-base](https://github.com/OpenCDMP/file-transformer-base) package designed to handle both and **import** and **export** OpenCDMP plans in JSON format based on the **[RDA DMP Common Standard](https://github.com/RDA-DMP-Common/RDA-DMP-Common-Standard)** for machine-actionable Data Management Plans (maDMPs).

---

## Overview

This service integrates with the OpenCDMP platform to provide import and export functionality for RDA-compliant JSON files. It ensures that data is structured according to the Research Data Alliance (RDA) recommendations for DMP interoperability.

---

## Features

- **JSON Export**: Export OpenCDMP plans to RDA-compliant JSON format.
- **JSON Import**: Import RDA-compliant JSON files into OpenCDMP as plans.
- **Spring Boot Microservice**: Built as a Spring Boot microservice for seamless integration with OpenCDMP.
- **Standards-Based**: Fully compliant with the **RDA DMP Common Standard** for maDMPs.

**Supported operations:**
- ✅ Export plans to RDA JSON
- ✅ Import plans from RDA JSON
- ❌ Description import/export (not supported)
---

## Key Endpoints

This service implements the following endpoints as per `FileTransformerController`:

### Export Endpoints

- **POST `/export/plan`**: Export a plan to RDA-compliant JSON.

```bash
POST /export/plan
{
    "planModel": { ... },
    "format": "json"
}
```

### Import Endpoints

- **POST `/import/plan`**: Import a plan from RDA-compliant JSON.

```bash
POST /import/plan
{
    "planImportModel": { ... }
}
```

### Configuration Endpoint

- **GET `/formats`**: Returns supported formats for import/export (JSON for RDA DMP).

### RDA DMP Common Standard
This service implements the RDA DMP Common Standard specification for machine-actionable DMPs, enabling interoperability between different DMP tools and systems. Learn more about the standard: https://github.com/RDA-DMP-Common/RDA-DMP-Common-Standard

---

## Integration with OpenCDMP

To integrate this service with your OpenCDMP deployment, configure the file transformer plugin in the OpenCDMP admin interface.

For detailed integration instructions, see see the [File Transformers RDA configuration](https://opencdmp.github.io/getting-started/configuration/backend/file-transformers/#rda-file-transformer) and the [OpenCDMP File Transformers Service Authentication](https://opencdmp.github.io/getting-started/configuration/backend/#file-transformer-service-authentication).

---

## See Also

- **File Transformers Overview**: https://opencdmp.github.io/optional-services/file-transformers
- **Developer Plugin Guide**: https://opencdmp.github.io/developers/plugins/file-transformers

---

### License
This repository is licensed under the [EUPL-1.2 License](LICENSE).

---
### Contact

For questions, support, or feedback:

- **Email**: opencdmp at cite.gr
- **GitHub Issues**: https://github.com/OpenCDMP/file-transformer-rda-json/issues
---

*This service is part of the OpenCDMP ecosystem. For general OpenCDMP documentation, visit [opencdmp.github.io](https://opencdmp.github.io).*
