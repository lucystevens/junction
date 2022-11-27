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