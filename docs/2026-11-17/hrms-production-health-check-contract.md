# HRMS Production Health Check Contract

## Endpoint separation
Define a lightweight liveness check for process health and a readiness check for whether the instance can safely receive traffic.

## Dependency checks
Readiness may verify essential database connectivity and required dependencies, but must not expose credentials, connection strings, or detailed infrastructure internals.

## Operational behavior
Set bounded timeouts, avoid expensive queries, and ensure health endpoints do not trigger migrations or mutate business data.

## Verification
Test healthy, database-unavailable, startup, shutdown, and timeout states. Confirm monitoring can distinguish an unhealthy process from an instance that is alive but not ready.