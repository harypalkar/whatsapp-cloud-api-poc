# MASTER-01 — Folder Structure

Target monorepo layout for WhatsFlow (to be created in later MASTER gates — **not scaffolded now**).

```text
whatsflow/
├── README.md
├── LICENSE
├── .gitignore
├── .env.example
├── docker-compose.yml                 # local full stack
├── Makefile                           # optional DX
│
├── docs/
│   ├── architecture/                  # living architecture (MASTER packs)
│   ├── adr/                           # Architecture Decision Records
│   ├── api/                           # OpenAPI exports
│   └── runbooks/                      # ops
│
├── apps/
│   ├── api/                           # Spring Boot modular monolith
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/whatsflow/
│   │       │   │   ├── WhatsFlowApiApplication.java
│   │       │   │   ├── shared/                    # kernel
│   │       │   │   │   ├── tenant/
│   │       │   │   │   ├── security/
│   │       │   │   │   ├── error/
│   │       │   │   │   ├── persistence/
│   │       │   │   │   └── event/
│   │       │   │   ├── identity/
│   │       │   │   │   ├── api/
│   │       │   │   │   ├── application/
│   │       │   │   │   ├── domain/
│   │       │   │   │   └── infrastructure/
│   │       │   │   ├── tenant/
│   │       │   │   ├── billing/
│   │       │   │   ├── meta/
│   │       │   │   ├── whatsapp/
│   │       │   │   ├── crm/
│   │       │   │   ├── campaign/
│   │       │   │   ├── conversation/
│   │       │   │   ├── inbox/
│   │       │   │   ├── agent/
│   │       │   │   ├── workflow/
│   │       │   │   ├── forms/
│   │       │   │   ├── notification/
│   │       │   │   ├── analytics/
│   │       │   │   ├── audit/
│   │       │   │   ├── settings/
│   │       │   │   └── platformadmin/
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       ├── application-local.yml
│   │       │       ├── db/migration/              # Flyway
│   │       │       └── db/callback/
│   │       └── test/
│   │
│   ├── worker/                        # same modules, worker entrypoint
│   │   ├── pom.xml
│   │   └── src/main/java/.../WhatsFlowWorkerApplication.java
│   │
│   └── web/                           # Angular 20
│       ├── package.json
│       ├── angular.json
│       └── src/
│           ├── app/
│           │   ├── core/
│           │   ├── shared/
│           │   ├── features/
│           │   │   ├── auth/
│           │   │   ├── onboarding/
│           │   │   ├── crm/
│           │   │   ├── campaigns/
│           │   │   ├── inbox/
│           │   │   ├── forms/
│           │   │   ├── automations/
│           │   │   ├── analytics/
│           │   │   ├── billing/
│           │   │   ├── settings/
│           │   │   └── platform-admin/
│           │   └── layouts/
│           ├── assets/
│           └── environments/
│
├── libs/                              # optional shared TS / OpenAPI client
│   └── api-client/
│
├── deploy/
│   ├── nginx/
│   ├── prometheus/
│   ├── grafana/
│   ├── elk/
│   └── k8s/                           # later
│
└── .github/
    └── workflows/
        ├── ci.yml
        ├── build-api.yml
        └── build-web.yml
```

## Module independence rule

- No `import` from another module’s `infrastructure` or `domain.internal`.
- Cross-module collaboration via **application ports** or **domain events** only.
- Extracting `campaign` to a microservice later should not require rewriting domain rules.

## Relation to this repo

Current work lives on branch `feature/business_model_whatsapp` inside `whatsapp-cloud-api-poc` as the **architecture seed**. Implementation monorepo scaffolding is deferred to MASTER-02+.
