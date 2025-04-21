# Use the official PostGIS-enabled PostgreSQL image
FROM postgis/postgis:17-3.4

USER postgres
WORKDIR /app

# These SQL files will be automatically run when the container starts
COPY /LiftDropRepositoryJdbi/src/sql/create-postgis-extension.sql /docker-entrypoint-initdb.d/create-postgis-extension.sql
COPY /LiftDropRepositoryJdbi/src/sql/create-schemas.sql /docker-entrypoint-initdb.d/create-schemas.sql
COPY /LiftDropRepositoryJdbi/src/sql/insert-data-testing.sql /docker-entrypoint-initdb.d/insert-data-testing.sql

# Copy wait script (optional, for orchestration)
COPY --chown=postgres:postgres /docker/scripts/wait-for-pg.sh ./bin/wait-for-pg.sh
RUN chmod +x ./bin/wait-for-pg.sh

EXPOSE 5432
