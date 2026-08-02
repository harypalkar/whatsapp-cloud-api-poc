# Runbook
- API down: check `docker compose ps`, logs `docker compose logs api`
- DB: `pg_isready`, restore from `scripts/restore.sh`
- Meta webhook failures: verify public URL + verify token
