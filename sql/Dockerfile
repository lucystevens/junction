FROM postgres:13

# Set up postgres user
ENV POSTGRES_PASSWORD password

COPY schema.sql /docker-entrypoint-initdb.d/00-schema.sql
COPY data.sql /docker-entrypoint-initdb.d/01-data.sql
