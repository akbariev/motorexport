CREATE EXTENSION pgcrypto;

CREATE TYPE engine_group AS ENUM ('DIESEL','GASOLINE','TURBO','ATMO','LPG');

CREATE TYPE gear_type AS ENUM ('ALL_WHEEL_DRIVE','FORWARD_CONTROL');

CREATE TYPE transmission AS ENUM (
    'AUTOMATIC',
    'ROBOT',
    'VARIATOR',
    'MECHANICAL'
    );

CREATE TYPE body_type_group AS ENUM (
    'SEDAN',
    'WAGON',
    'CABRIO'
    );

CREATE TYPE in_stock AS ENUM (
    'IN_STOCK',
    'ON_ORDER',
    'IN_TRANSIT',
    'SOLD'
    );


CREATE TABLE IF NOT EXISTS car(
    id              UUID        DEFAULT gen_random_uuid(),
    engine_group    engine_group                          NOT NULL,
    gear_type       gear_type                             NOT NULL,
    transmission    transmission                          NOT NULL,
    body_type_group body_type_group                       NOT NULL,
    in_stock         in_stock                              NOT NULL,
    year            BIGINT,
    price           BIGINT,
    mileage         BIGINT,
    displacement    BIGINT,
    country         VARCHAR(3),
    created_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);
