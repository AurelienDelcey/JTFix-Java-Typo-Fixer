# JTFix — Extractor Module

## Goal

The extractor is a low-level incremental Java structure analysis engine.

Its purpose is to traverse Java source code character-by-character
in order to extract structurally contextualized tokens.

The extractor does not attempt to build a full Java AST
or perform semantic analysis.

Instead, it focuses on deriving deterministic structural information
from lexical transitions, contextual ownership, and structural depth.

The extractor also acts as an early structural knowledge accumulation layer.

During extraction, the engine progressively gathers deterministic information
about the codebase, such as:
- declared types
- enum constants
- contextual ownership
- structural declaration zones
- executable contexts
- parameter declaration contexts

This information is later reused by higher-level modules in order to:
- classify identifiers
- infer expected naming conventions
- pre-sort identifiers by expected casing styles
- improve token segmentation
- reduce ambiguity
- provide contextual correction hints

Extracted tokens are later consumed by higher-level modules such as:
- classifiers
- segmentation pipelines
- correction systems

---

## Extraction Model

The extractor is built as a deterministic multi-level extraction pipeline.

Source files are traversed incrementally, character-by-character,
allowing structural information to emerge progressively through
context transitions and structural ownership.

The extraction process is divided into multiple structural layers:
- character analysis
- contextual transitions
- token extraction
- structural pre-sorting

The engine relies on contextual structure rather than isolated lexical analysis.

Contexts are used as structural ownership boundaries,
allowing the extractor to progressively refine interpretation
while preserving deterministic behavior.

Each layer is responsible for transforming low-level structural signals
into increasingly contextualized information.

The extraction pipeline is designed to remain independent
from traversal order.

Structural extraction results must remain deterministic
regardless of exploration sequencing.

Extraction results are intentionally produced as mergeable
structural units in order to support future parallel traversal strategies.

---

## Scope & Non-Goals

The extractor is not intended to become a full Java compiler frontend.

Its purpose is to provide deterministic structural extraction
and contextualized token ownership for higher-level analysis pipelines.

The engine intentionally avoids responsibilities such as:
- full AST generation
- semantic resolution
- type inference
- bytecode analysis
- compilation validation

The extractor focuses exclusively on structural interpretation
required for contextual identifier analysis and naming classification.

Structural certainty is preferred over exhaustive language modeling.

The extractor intentionally avoids relying on existing parsing solutions.

The engine is designed specifically around the structural requirements
of contextual identifier analysis and naming classification,
while preserving the lightweight execution model expected from a CLI tool.

This allows the extraction pipeline to remain:
- structurally specialized
- incrementally composable
- predictable in behavior
- and free from unnecessary abstraction overhead

---

## Internal Structural Model

The extractor operates as a stateful structural traversal engine.

Structural interpretation is driven through contextual structure
rather than isolated lexical analysis.

The engine maintains multiple layers of structural state during traversal:
- active contexts
- prepared contexts
- structural depths
- incremental token extraction

Contexts act as structural interpretation boundaries.

When a new context becomes active,
underlying contexts are temporarily interrupted
until structural ownership returns to the parent context.

Prepared contexts are used to model structural transitions
which are declared before becoming structurally active.

This allows the engine to progressively derive structural meaning
before complete scope materialization occurs.

The extraction pipeline is internally divided into specialized trackers.

Each tracker is responsible for a dedicated structural concern, such as:
- comments
- strings
- lambdas
- generics
- structural depth
- token boundaries

This specialization helps isolate stateful structural logic
while preserving incremental traversal behavior.

Context lifecycle management is intentionally centralized.

Active contexts are maintained through a dedicated context management layer
responsible for stack consistency and structural transition integrity.

This encapsulation helps:
- preserve structural invariants
- reduce unintended side effects
- isolate context lifecycle mutations
- maintain deterministic traversal behavior

---

## Invariants

The extractor relies heavily on structural invariants
to preserve deterministic behavior during incremental traversal.

These invariants define the structural rules
which guarantee contextual coherence,
context lifecycle integrity,
and predictable structural interpretation.

Most invariants emerged progressively through:
- edge-case handling
- structural debugging
- context desynchronization fixes
- incremental parser refinement

They now form the backbone of the extraction model.

### Structural Invariants

- An open brace must always open a context.

- A closing brace must always close a context.

- All contexts exist within their structural depth.

- A context may only be closed when its relative structural depth reaches zero.

- An opened context interrupts the processing of underlying contexts.

- Any context opened without prior preparation and without a specific structural hint is, or is assimilable to, a method context (method / constructor / init block / static init...).

---

### Prepared Context Invariants

- Every preparedContext must be consumed exactly once.

- A declarative keyword prepares the opening of its associated context.

- A token extracted under a declarative preparedContext is a type.

---

### Hierarchical Invariants

- A lambda block may only exist above a lambda context.

- Closing a lambda block implicitly closes its underlying lambda context.

---

### Context Ownership Invariants

- During an ignored context, characters must only be processed by the owner responsible for that context.

- The deepest active context owns character interpretation.

- An extracted token always belongs to the current context if one exists.

- The absence of a linked context is itself contextual information.

---

### Parenthesis Context Invariants

- Under a root context, a relative depth of:
  - brace == 0
  - paren == 1

  implies a declarative parameter context.

- If no active context exists, a parenthesis depth equal to 1 implies a declarative parameter context.

- Any parenthesis opening under a prepared control structure context implies a boolean expression context.

- Any parenthesis opening without a structural hint is, or is assimilable to, an executable parameter context.

---

### Enum Invariants

- A token extracted inside an enum context, with:
  - braceDepth == 1
  - parenDepth == 0
  - before any semicolon detection

  is an enum constant.

---

### End-of-File Invariants

- At end-of-file, braceDepth must equal zero.

- At end-of-file, parenDepth must equal zero.

- At end-of-file, no context must still exist.

---

## Current Limitations

The extractor is still under active development
and some structural behaviors remain intentionally unsupported or unvalidated.

Known limitations and partially validated behaviors currently include:

- Annotations are not yet structurally analyzed.

- Nested type declarations are not fully validated yet.
  The current model may support them correctly,
  but large-scale validation is still incomplete.

- Generic expressions may still produce false positives
  inside boolean expression contexts in cases such as:
  `a < b > c`

- Internal switch expression structures are not fully analyzed yet,
  although structural traversal support already exists.

- Parenthesis-driven contextual structures are not implemented yet.

  The extractor currently tracks parenthesis depth only.

  Dedicated contextual interpretation for:
  - declarative parameter zones
  - executable parameter zones
  - control structure expressions
  - record parameter declarations

  is still under architectural exploration.

  The current model may eventually:
  - introduce dedicated parenthesis-driven contexts
  - or derive structural meaning directly from relative depths

- Some advanced contextual interpretations are intentionally deferred
  to higher-level classification layers.

The extraction engine is continuously refined through:
- structural debugging
- invariant formalization
- large-scale repository traversal
- edge-case discovery

---

## Planned Evolutions

Several structural evolutions are already identified
as part of the long-term extraction model.

Current exploration areas include:
- parenthesis-driven contextual interpretation
- relative-depth-based context lifecycle management
- contextual preclassification layers
- progressive structural knowledge accumulation
- mergeable extraction passes
- deeper enum and switch structure analysis

The extractor is also progressively evolving toward:
- richer structural token metadata
- stronger contextual ownership modeling
- reduced ambiguity through structural deduction
- larger-scale parallel traversal capabilities

Some architectural directions are still intentionally undecided.

In particular, the engine is still exploring whether:
- certain contextual interpretations should become explicit contexts
- or emerge implicitly from structural depth and traversal state

The extraction model is expected to continue evolving
through iterative structural refinement and large-scale repository traversal.

---

## Architectural Tradeoffs

The extractor intentionally stays close to its low-level traversal model.

Rather than hiding structural traversal behind heavy abstractions,
the engine preserves explicit state transitions,
incremental mutations,
and direct structural flow visibility.

This approach increases apparent mechanical complexity,
but helps maintain:
- deterministic behavior
- predictable structural transitions
- lightweight execution
- and direct reasoning over parser state

The extracted structural representation intentionally remains flat and offset-based.

Structural meaning progressively emerges from:
- contextual transitions
- structural depths
- traversal state
- and incremental token extraction

without relying on heavy intermediate structural representations.

Mutations and localized side effects are intentionally tolerated
when they help preserve traversal clarity
or reduce unnecessary indirection.

Abstractions are introduced only when they provide:
- meaningful structural isolation
- invariant protection
- lifecycle consistency
- or real complexity reduction