CREATE TABLE config (
    key varchar NOT NULL,
    value varchar,
    CONSTRAINT pk_config PRIMARY KEY (key)
);

CREATE TABLE routes (
    host varchar NOT NULL,
    path varchar NOT NULL,
    targets json,
    CONSTRAINT pk_routes PRIMARY KEY (host, path)
);

CREATE TABLE domains (
     name varchar NOT NULL,
     ssl boolean,
     redirectToHttps boolean,
     csr varchar,
     certificate varchar,
     keypair varchar,
     expiry timestamp,
     CONSTRAINT pk_domains PRIMARY KEY (name)
);