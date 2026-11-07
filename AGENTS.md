# TallerTech - Agent Instructions

## Project Overview

TallerTech is an Android mobile application for managing a technical repair workshop.

The application manages:

- Clients
- Equipment
- Service orders
- Diagnoses
- Repairs
- Users

The application is being developed with an offline-first architecture.

## Technology Stack

Use the existing project stack.

- Android
- Java
- XML Views
- Material Design
- Room
- SQLite
- Repository pattern
- DAO pattern
- Gradle Kotlin DSL
- minSdk 29

Do not migrate the project to Kotlin.

Do not introduce Jetpack Compose.

## Architecture

Maintain the current architecture:

UI Fragment
→ Repository
→ DAO
→ Room
→ SQLite

Current packages include:

- `ui.clients`
- `ui.equipment`
- `ui.orders`
- `ui.home`
- `ui.profile`
- `data.local`
- `data.local.dao`
- `data.local.entity`
- `data.repository`

Respect the existing package organization.

## Persistence

Room is the local persistence layer.

Existing entities include:

- ClientEntity
- EquipmentEntity
- ServiceOrderEntity
- DiagnosisEntity
- RepairEntity
- UserEntity

Use UUID strings for locally created entity IDs unless there is a strong technical reason not to.

Operational records use fields such as:

- `updatedAt`
- `syncStatus`

Possible synchronization states may include:

- `PENDING`
- `SYNCED`
- `DELETED`

Do not implement remote synchronization unless explicitly requested.

## Offline-First Rules

The application must continue working without Internet access.

Local writes should be persisted in Room first.

Remote API synchronization will be implemented later.

Do not make UI functionality depend directly on Internet connectivity.

## UI Rules

Preserve the existing UI unless a requested task requires changes.

Do not redesign existing screens unnecessarily.

Keep existing navigation functional.

Do not rename resource IDs unless necessary.

Maintain Material Design and the existing TallerTech visual style.

## Java Code Style

Use Java for application source code.

Use concise and useful comments.

Do not comment every line.

Prefer clear method and variable names.

Avoid unnecessary abstractions.

Keep code understandable for a university technical defense.

## Data Access Rules

Do not perform Room operations directly on the Android main thread.

Use repositories and background executors for database operations.

Fragments should not execute DAO queries directly when a repository already exists or should exist.

Preserve referential integrity between entities.

For example:

ClientEntity
→ EquipmentEntity through `clientId`

EquipmentEntity
→ ServiceOrderEntity through `equipmentId`

## Development Safety

Before changing code:

1. Inspect the relevant existing files.
2. Understand the current implementation.
3. Avoid rewriting working functionality unnecessarily.

When modifying existing functionality:

- Preserve working behavior.
- Make the smallest coherent change.
- Do not delete unrelated code.
- Do not modify unrelated modules.

## Build Verification

After completing a coding task, run the project build.

Preferred command on Windows:

`.\gradlew.bat assembleDebug`

If the build fails because of code introduced by the task, fix the errors before finishing.

Report any remaining warning or error clearly.

## Git Rules

Do not automatically:

- commit
- push
- merge
- rebase
- force push
- delete branches
- modify tags

unless explicitly instructed.

The stable baseline is available in Git history and the tag:

`checkpoint-room-clientes`

Work on the current task branch and leave Git publishing decisions to the user.

## Scope Control

Only implement what the current task requests.

Do not proactively implement:

- Retrofit
- REST API
- cloud deployment
- authentication backend
- synchronization engine

unless explicitly requested.

## Current Development State

The frontend is substantially implemented.

Room infrastructure is working.

Client persistence already works through:

RegisterClientFragment
→ ClientRepository
→ ClientDao
→ Room

Clients persist after the application is closed and reopened.

The next persistence work should extend the same architecture to other modules without breaking the existing Clients implementation.

## Final Response

When finishing a task, summarize:

- files created
- files modified
- functionality implemented
- build result
- important technical decisions

Do not claim something was tested unless it was actually executed or verified.