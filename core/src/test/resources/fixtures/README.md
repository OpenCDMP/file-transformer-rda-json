# Captured plan fixtures

`ExportSnapshotTest` and `ImportRoundTripTest` pick up every `*.plan.json` file here as a scenario called
`captured-<file name>`. Its expected output is kept under `../snapshots/`, like the hand-built scenarios in
`Scenarios.java`.

A fixture is the exact JSON body that OpenCDMP sends to `POST /api/file-transformer/export/plan` when it exports
a plan: the plan, its blueprint, and each description with its template and answers.

## Capture a plan

1. Run the plugin locally with capture switched on:

   ```
   RDA_FILE_TRANSFORMER_FIXTURE_CAPTURE_ENABLED=true
   RDA_FILE_TRANSFORMER_FIXTURE_CAPTURE_DIRECTORY=fixture-capture   # relative to the working directory
   ```

   Capture saves whole plans, personal data included. Switch it on only on your own machine, never in production.

2. Register the plugin with a local OpenCDMP installation (see
   [Run and test a plugin locally](https://opencdmp.github.io/developers/plugins/run-locally/)) and export the plan
   as RDA JSON.
3. The body of every `POST` request is saved as `fixture-capture/<timestamp>-<number>-<endpoint>.json`, and the
   path is logged at WARN level. The fixture is the `...-export-plan.json` file. The `fixture-capture/` folder is
   git-ignored.

## Anonymise it

Captured plans hold real answers, names, email addresses and identifiers such as ORCIDs. **Anonymise every file
before committing it.** Replace people, emails, personal identifiers and free-text answers with made-up values,
keeping the structure, the semantics and the kinds of values (dates stay dates, URLs stay URLs). For an ORCID,
use a made-up value such as `0000-0000-0000-0000` or ORCID's test identity `0000-0002-1825-0097`.

Then save it as `<short-name>.plan.json` (for example `river-survey.plan.json`) and run:

```
mvn -pl core test
```

The first run writes the new snapshots and fails once so you review them. Then run again.

## Update snapshots

When a change to the output is intended, accept it with:

```
mvn -pl core test -Dsnapshots.update=true
```

and review the snapshot changes in the git diff before committing.
