<!--

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

-->

# Royale Code Graph Exporter Implementation Plan

## Objective

Add a compiler-backed exporter that produces deterministic, machine-readable descriptions of the public Apache Royale API. The output must be complete enough for clients to determine how to use every public class, interface, package function, field, accessor, method, event, style, effect, and MXML component, including all referenced types.

The exporter belongs in `royale-compiler`. Project orchestration and release packaging belong in the sibling `royale-asjs` repository and should be handled only after the compiler exporter is stable.

## Why This Must Use Compiler Semantics

Do not parse ActionScript or MXML with regular expressions. The exporter must use resolved compiler definitions because Royale APIs may depend on:

- `COMPILE::JS` and `COMPILE::SWF` conditional compilation.
- Imports, namespaces, package functions, and external SWCs.
- Inheritance, interface implementation, and overrides.
- Getter/setter pairs and target-specific signatures.
- ASDoc tags such as `@copy` and `@private`.
- Structured metadata such as `Event`, `Style`, `Effect`, `Bindable`, `DefaultProperty`, and `Inspectable`.
- Definitions that are reachable or exported by a SWC rather than every source declaration.

`UIBase` in `royale-asjs/frameworks/projects/Basic` is an important eventual integration case. Its `typeNames` field is simple, while `width`, `parent`, and `transformElement` demonstrate target-specific declarations and inheritance behavior.

## Compiler Infrastructure and References

Start by reading these files:

- `compiler-jx/src/main/java/org/apache/royale/compiler/clients/MXMLJSCRoyale.java`
- `compiler-jx/src/main/java/org/apache/royale/compiler/internal/driver/mxml/royale/MXMLRoyaleSWCBackend.java`
- `compiler-jx/src/main/java/org/apache/royale/compiler/internal/projects/RoyaleJSProject.java`
- `compiler-jx/src/main/java/org/apache/royale/compiler/internal/targets/RoyaleSWCTarget.java`
- `compiler-jx/src/main/java/org/apache/royale/compiler/asdoc/royale/ASDocComment.java`
- `compiler-jx/src/main/java/org/apache/royale/compiler/internal/parsing/as/RoyaleASDocDelegate.java`

The compiler infrastructure already provides:

1. Normal compiler configuration and workspace setup.
2. `RoyaleSWCTarget` construction.
3. Target roots through `getReachableCompilationUnits(...)`.
4. Compiler-project ordering of reachable units.
5. Extern and external-linkage information.
6. Separate resolved JS and SWF configurations.
7. Parsed ASDoc comments through the compiler delegate and comment model.

Do not create an unrelated parser or duplicate compiler setup. Reuse the lower-level compiler lifecycle where appropriate, but keep the code graph client independent from the ASDoc client and output model. `CODEGRAPH` is not a kind of `ASDOCJSC`; any shared behavior is incidental compiler infrastructure and does not justify inheritance. The code graph path must not require ASDoc-specific configuration, emitters, or exclusions unless a rule is independently part of the code graph contract.

## Recommended Shape

Keep the first implementation in `compiler-jx` because that module contains the Royale compiler client, SWC backend, project, and target infrastructure needed by the exporter.

The standalone client should extend the common compiler client infrastructure, use the normal Royale SWC backend, and own its target setup, reachable-unit selection, and graph output. Existing clients such as `ASDOCJSC` should remain unchanged unless a genuinely shared lower-level abstraction is introduced for multiple compiler clients.

Suggested classes are names, not mandatory API decisions:

```text
org.apache.royale.compiler.clients.CODEGRAPH
org.apache.royale.compiler.internal.codegen.graph.CodeGraphExporter
org.apache.royale.compiler.internal.codegen.graph.CodeGraphModel
org.apache.royale.compiler.internal.codegen.graph.CodeGraphWriter
```

Prefer a small model and writer over embedding JSON calls throughout AST visitors. Compiler traversal should populate the model; serialization should be deterministic and independently testable.

The initial command should resemble existing compiler clients:

```sh
java -cp ... org.apache.royale.compiler.clients.CODEGRAPH \
  -load-config+=path/to/config.xml \
  -compiler.define+=COMPILE::JS,true \
  -compiler.define+=COMPILE::SWF,false \
  -output=target/codegraph/graph.js.json
```

One invocation exports one resolved target. Merging JS and SWF into one logical release index can come later. Keeping target runs separate matches current compiler and Maven behavior and avoids inventing a second conditional-compilation evaluator.

## First Vertical Slice

The first pull request should prove semantic extraction, not solve release packaging.

1. Add a compiler test fixture containing:
   - One public class and one interface.
   - A base class and inherited member.
   - A constructor.
   - A public variable with a default value.
   - A constant.
   - Getter and setter declarations.
   - A method with required, optional, and rest parameters.
   - A package-level function.
   - `Event`, `Bindable`, and `DefaultProperty` metadata.
   - An ASDoc description and tags.
   - A JS-only member and a SWF-only member.
   - A reference to a type from an external SWC.

2. Build the fixture through the normal compiler target.

3. Enumerate only reachable AS/MXML compilation units using the normal SWC target, extern configuration, and external-linkage rules owned by the code graph client.

4. Export public top-level definitions and their directly declared public members.

5. Resolve every referenced type through `ICompilerProject`. Emit a stable symbol reference even when the definition belongs to an external library.

6. Serialize a deterministic JSON document.

7. Assert the complete output as a golden fixture and run the export twice to verify byte-identical output.

Do not begin with inherited-member materialization, Maven attachment, graph compression, npm packaging, or all Royale framework projects. Those are follow-up slices.

## Minimum Version 1 Data Model

The exact JSON shape should be finalized with tests, but version 1 needs these concepts.

### Document

```json
{
  "schemaVersion": "1.0",
  "target": "js",
  "module": "UIBase",
  "symbols": [],
  "externalSymbols": []
}
```

Do not include timestamps or absolute machine paths. They break deterministic release artifacts.

### Stable Symbol IDs

Use qualified semantic identities rather than source locations:

```text
as3://org.apache.royale.core/UIBase
as3://org.apache.royale.core/UIBase#typeNames
as3://org.apache.royale.core/UIBase#width:get
as3://org.apache.royale.core/UIBase#setWidth(Number,Boolean)
as3://org.apache.royale.utils/sendEvent
```

Overloads are uncommon in AS3 but IDs must still distinguish callable signatures. Constructors, getters, setters, methods, fields, constants, and package functions need unambiguous IDs.

### Definition Data

For each public type or package-level definition, include:

- Stable ID, qualified name, base name, package, and kind.
- Namespace/visibility.
- Declaring source using a repository-relative or configured source-root-relative path.
- Base type and implemented interfaces as symbol references.
- Flags such as static, final, dynamic, override, abstract, and native when available.
- Parsed ASDoc description and structured tags.
- Structured metadata with ordered key/value arguments.
- Directly declared members.

For members, include:

- Kind, name, stable ID, and declaring type.
- Type or return type as a resolved symbol reference.
- Ordered parameters with type, optional/rest state, and default value representation.
- Getter/setter identity.
- Constant or field default value when available.
- ASDoc and metadata.

For external references, emit at least:

- Stable ID.
- Qualified name.
- Kind when known.
- Origin/library path in portable form when available.
- A marker that the full declaration is external to this graph.

Never silently replace an unresolved type with a simple string. Emit an explicit unresolved reference and a compiler problem so validation can find it.

## Compiler APIs to Prefer

Use public definition and scope interfaces where possible:

- `IDefinition`
- `ITypeDefinition`
- `IClassDefinition`
- `IInterfaceDefinition`
- `IFunctionDefinition`
- `IAccessorDefinition`
- `IVariableDefinition`
- `IConstantDefinition`
- `IParameterDefinition`
- `IMetaTag` and metadata attribute APIs
- `ICompilerProject`
- `ICompilationUnit`

Resolve types with the active compiler project. Do not infer qualification from source imports manually.

Use AST nodes only for facts absent from definitions, such as preserving a source-level default expression. Keep semantic identity and type resolution definition-based.

## Public API Rules

Initially include:

- Public top-level classes and interfaces.
- Public constructors and members.
- Public package functions, variables, and constants.
- Metadata that affects client usage.

Initially exclude:

- Private, protected, and internal declarations.
- Declarations excluded from the public documentation contract, including `@private`.
- Compiler-generated implementation details unless they are actually part of the exported public SWC API.
- Method bodies and local-variable dependency graphs.

The objective is a public API/type graph, not a whole-program call graph.

## ASDoc Handling

Use `RoyaleASDocDelegate` and the existing parsed comment model for documentation extraction only. This does not make the exporter an ASDoc client and must not require the ASDoc backend or ASDoc configuration class. Preserve descriptions and tags structurally.

For the first slice, retain `@copy` as a structured tag/reference. Resolve and materialize copied text in a follow-up only after direct comments are correct. Similarly, preserve unknown tags rather than dropping them.

Treat `@private` consistently with the compiler's parsed documentation semantics.

## Target Handling

Run the exporter once per target configuration:

```text
COMPILE::JS=true,  COMPILE::SWF=false -> graph.js.json
COMPILE::JS=false, COMPILE::SWF=true  -> graph.swf.json
```

Each graph describes what the compiler actually sees for that target. A later merger may combine matching stable IDs and mark availability as `js`, `swf`, or both.

Do not make the exporter inspect inactive conditional branches itself.

## Determinism Requirements

- Sort top-level symbols by stable ID.
- Sort members by stable ID, not hash-map iteration order.
- Preserve parameter order and metadata argument order where order is meaningful.
- Normalize path separators to `/`.
- Omit timestamps, temporary paths, and absolute checkout paths.
- Use a fixed JSON encoding and newline policy.
- Fail tests if two runs produce different bytes.

## Tests

Place focused tests under `compiler-jx/src/test/java` and fixtures under the existing compiler-jx test resource conventions.

Required first tests:

1. `typeNames`-style public field: type, default value, docs, metadata, owner.
2. Class/interface inheritance and declared-member ownership.
3. Getter and setter represented distinctly but linked by property name.
4. Parameter types, defaults, optional parameters, and rest parameters.
5. Package-level function.
6. JS/SWF conditional members produce different target outputs.
7. External type reference is retained.
8. Excluded/private definitions are absent.
9. Deterministic byte output.
10. Unresolved types create an explicit problem or validation failure.

The tests must also cover the standalone client path, not only graph model helpers:

- Normal compiler configuration and include options, without ASDoc-only options.
- SWC target setup and reachable-unit filtering.
- Output path handling and target identification.
- No graph output after compiler errors unless `create-target-with-errors` explicitly permits it.
- A target-built golden graph, in addition to focused in-memory definition tests.

Use explicit assertions in test helpers so a missing symbol or member reports its semantic identity instead of failing later with a null-pointer exception. Exact JSON golden assertions are appropriate because byte-level stability is part of the exporter contract.

Run the narrow module tests first:

```sh
./mvnw -pl compiler-jx -am test
```

Follow repository conventions if the existing compiler test harness requires additional environment properties.

## Repository Conventions and Review Gate

Before considering the first compiler PR review-ready:

- Follow the existing Apache headers, package layout, four-space indentation, brace placement, explicit generic types, `Test*` naming, JUnit 4, and compiler test-base patterns.
- Keep imports consistent; do not use fully qualified collection types inline when normal imports are already used.
- Add concise class-level Javadocs to the new production classes, consistent with neighboring compiler code.
- Check compiler problems after target construction and before writing output. Match neighboring client behavior for `create-target-with-errors`.
- Keep `ASDOCJSC` unchanged. The code graph client must not inherit from it or use `MXMLRoyaleASDocBackend`.
- Keep deterministic writer tests, but supplement them with compiler-backed and CLI-level tests.
- Run editor diagnostics and the complete `compiler-jx` reactor tests before review.

Passing the existing suite is necessary but not sufficient: the new client must have direct regression coverage for configuration, target setup, filtering, error handling, and output generation.

## Current Implementation Status

Implemented and covered by focused compiler-backed tests:

- Public classes, interfaces, implicit interface members, package definitions, and directly declared public members.
- Expanded directory-valued include sources and unreferenced included source files, with or without a positional target file.
- Stable IDs, deterministic JSON, source-root-relative provenance, module identity, and external library origins.
- Resolved and explicitly unresolved signature references, with compiler diagnostics retained for unresolved types.
- Base, interface, override, and implementation relationships.
- Visibility, declaration modifiers, parameter defaults, and variable/constant initial values.
- Parsed ASDoc, `@private` exclusion, structured metadata, metadata ASDoc, and type-bearing metadata references.
- JS/SWF conditional selection, compiler error gating, two-run determinism, and an exact golden document.
- A packaged Draft 2020-12 JSON Schema with validation of unit-built and compiler-backed graph documents.

`@copy` targets remain structured, opaque tag values, matching Royale's existing parser and emitter semantics. Effective inherited-member views remain derivable from explicit base/interface edges. Aggregate build-tool integration remains deferred.

### Schema Compatibility Policy

- `schemaVersion` uses `major.minor` numbering and identifies the graph contract, independently of compiler releases.
- Additive optional fields and new enum values require a minor version. Removing, renaming, changing the meaning of a field, or making an optional field required requires a major version.
- Each supported contract has a packaged, immutable schema named `codegraph-<major>.<minor>.schema.json` beside the graph model classes.
- Writers emit one exact schema version. Consumers must reject unsupported major versions and may accept newer minor versions only when they tolerate unknown optional fields and enum values.
- The schema, model version constant, writer output, and canonical golden document must change together and pass schema validation.

## Suggested Pull Request Sequence

### Slice 1: Semantic exporter MVP

- Graph model and deterministic JSON writer.
- Compiler-backed collection of public types and directly declared members.
- ASDoc and metadata extraction.
- JS/SWF fixture tests. Implemented with opposite compiler define sets that verify target labels and active-member selection.
- CLI entry point in `compiler-jx`.

### Slice 2: Completeness

- Package-level definitions. Implemented with focused function/variable/constant coverage and a compiler-backed reachable package-function fixture.
- External and unresolved symbol records. Implemented with explicit unresolved markers, origins, and compiler-problem coverage.
- Inheritance/override edges. Implemented for base types, interfaces, overridden methods, and interface implementations.
- Effective inherited public member view if clients require it.
- `@copy` is preserved as an opaque structured tag, matching Royale parser and emitter behavior.
- JSON Schema and schema compatibility policy. Implemented with a packaged versioned schema and validator-backed tests.

### Slice 3: Build-tool integration

- `compile-codegraph` goal in `royale-maven-plugin`. Implemented with dedicated graph configs and separate JS/SWF outputs.
- Ant/tool registration and SDK launcher scripts. Implemented with a `codegraph.jar` entry point and Unix/Windows launchers.
- Target-specific dependency classifiers. Implemented to match `CompileASDocMojo` without invoking the ASDoc compiler.

### Slice 4: `royale-asjs` integration

This work happens in the sibling repository:

- Generate one graph per framework project and target.
- Add module/Maven coordinates and dependency closure to an aggregate index.
- Add MXML manifest URI/tag mappings.
- Validate exports against released SWCs.
- Package identical graph data into Maven classifiers, SDK downloads, and `@apache-royale/codegraphs` on npm.
- Add release hashes and signatures.

## Integration Contract With `royale-asjs`

The compiler exporter should accept normal compiler configuration and produce one graph file. It should not need to know the Royale reactor, npm package layout, or release staging paths.

`royale-asjs` will be responsible for supplying:

- Project source paths and include classes.
- JS or SWF dependency paths.
- Compiler defines.
- Project/module identity.
- Manifest files and MXML namespace mapping if not already available through compiler configuration.
- Final output location and release packaging.

The graph format must allow `royale-asjs` to add module metadata without rewriting semantic symbol records.

## First Session Checklist

1. Build `compiler-jx` unchanged and record the working test command.
2. Run a normal SWC test configuration on a tiny fixture.
3. Trace one reachable compilation unit from `RoyaleSWCTarget` to its resolved top-level definition.
4. Prove extraction of class name, base class, interfaces, and one public field into an in-memory model.
5. Add a deterministic writer and golden test.
6. Add JS/SWF conditional fixture coverage.
7. Only then add the standalone `CODEGRAPH` client.

## Definition of Done for the Compiler Phase

The compiler phase is ready for `royale-asjs` integration when:

- A clean compiler checkout can invoke one documented command to generate a graph.
- JS and SWF runs reflect their active conditional declarations.
- All public signatures in the test fixture retain resolved type references.
- ASDoc and relevant metadata are present.
- External and unresolved types are explicit.
- Output is byte-identical across repeated runs.
- The `compiler-jx` test suite passes.
- No application-specific or `royale-asjs` project list is hard-coded in the exporter.

## Out of Scope for the First Compiler PR

- Rendering HTML documentation.
- Application call graphs or method-body analysis.
- npm publication.
- Maven artifact attachment.
- SDK release assembly.
- Compressing graph files.
- Combining all Royale projects.
- Replacing current ASDoc output.

Keep the first change narrow: resolved public compiler facts in deterministic JSON.