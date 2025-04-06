FROM postgres

USER postgres
WORKDIR /app

COPY /LiftDropRepositoryJdbi/src/sql/create-schemas.sql /docker-entrypoint-initdb.d/create-schemas.sql
COPY /LiftDropRepositoryJdbi/src/sql/insert-data-testing.sql /docker-entrypoint-initdb.d/insert-data-testing.sql

COPY --chown=postgres:postgres /docker/scripts/wait-for-pg.sh ./bin/wait-for-pg.sh
RUN chmod +x ./bin/wait-for-pg.sh

EXPOSE 5432