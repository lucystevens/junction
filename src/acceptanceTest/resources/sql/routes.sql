CREATE TABLE routes (
    host varchar NOT NULL,
    path varchar NOT NULL,
    targets json,
    CONSTRAINT pk_routes PRIMARY KEY (host, path)
);