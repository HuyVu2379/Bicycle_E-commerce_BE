-- Script to initialize multiple databases for microservices
-- This script creates separate databases for each service within a single PostgreSQL instance

CREATE DATABASE userdb;
CREATE DATABASE cartdb;
CREATE DATABASE orderdb;
CREATE DATABASE paymentdb;

-- Grant privileges to postgres user for all databases
GRANT ALL PRIVILEGES ON DATABASE userdb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE cartdb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE orderdb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE paymentdb TO postgres;

-- Optional: Create schemas for better organization
\c userdb;
CREATE SCHEMA IF NOT EXISTS user_schema;
GRANT ALL ON SCHEMA user_schema TO postgres;

\c cartdb;
CREATE SCHEMA IF NOT EXISTS cart_schema;
GRANT ALL ON SCHEMA cart_schema TO postgres;

\c orderdb;
CREATE SCHEMA IF NOT EXISTS order_schema;
GRANT ALL ON SCHEMA order_schema TO postgres;

\c paymentdb;
CREATE SCHEMA IF NOT EXISTS payment_schema;
GRANT ALL ON SCHEMA payment_schema TO postgres;
