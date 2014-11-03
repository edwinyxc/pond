/* MYSQL */
DROP DATABASE POND_DB_TEST;

CREATE DATABASE POND_DB_TEST;

USE POND_DB_TEST;

CREATE TABLE test (
    id varchar(64) primary key,
    `value` varchar(2000)
);

INSERT INTO test values('2333','233333');
INSERT INTO test values('2334','2333334');
