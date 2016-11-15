CREATE SCHEMA PUBLIC;

CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_8B80E190_A88F_48F0_AD69_3DD191990060 START WITH 1;

CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_E75FD7C5_3A92_4977_97D2_F1C5175C3A72 START WITH 1;

CREATE TABLE PUBLIC.ROLE ( 
	ID                   integer  DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_E75FD7C5_3A92_4977_97D2_F1C5175C3A72) NOT NULL,
	NAME                 varchar(100)   NOT NULL,
	CONSTRAINT CONSTRAINT_26 PRIMARY KEY ( ID )
 );

CREATE TABLE PUBLIC."USER" ( 
	ID                   bigint  DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_8B80E190_A88F_48F0_AD69_3DD191990060) NOT NULL,
	NAME                 varchar(100)   NOT NULL,
	CONSTRAINT CONSTRAINT_2 PRIMARY KEY ( ID )
 );

CREATE TABLE PUBLIC.USER_X_ROLE ( 
	USER_ID              bigint   NOT NULL,
	ROLE_ID              bigint   NOT NULL,
	CONSTRAINT IDX_USER_X_ROLE_1 PRIMARY KEY ( USER_ID, ROLE_ID )
 );

CREATE INDEX IDX_USER_X_ROLE ON PUBLIC.USER_X_ROLE ( USER_ID );

CREATE INDEX IDX_USER_X_ROLE_0 ON PUBLIC.USER_X_ROLE ( ROLE_ID );

CREATE TABLE PUBLIC.VIDEO ( 
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	USER_ID              integer   NOT NULL,
	NAME                 varchar(100)   NOT NULL,
	CONSTRAINT PK_VIDEOS PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_VIDEOS ON PUBLIC.VIDEO ( USER_ID );

CREATE TABLE PUBLIC.FILE ( 
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	FILENAME             varchar(255)   NOT NULL,
	SIZE_BYTES           bigint   ,
	VIDEOS_ID            bigint   NOT NULL,
	CONSTRAINT PK_FILES PRIMARY KEY ( ID )
 );

CREATE INDEX IDX_FILES ON PUBLIC.FILE ( VIDEOS_ID );

CREATE TABLE PUBLIC.SNAPSHOT ( 
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	FILES_ID             bigint   NOT NULL,
	FILENAME             varchar(255)   NOT NULL,
	CONSTRAINT PK_SNAPSHOTS PRIMARY KEY ( ID )
 );

CREATE INDEX idx_SNAPSHOTS ON PUBLIC.SNAPSHOT ( FILES_ID );

CREATE TABLE PUBLIC.SURVEY ( 
	ID                   bigint GENERATED ALWAYS AS IDENTITY  NOT NULL,
	USER_ID              bigint   NOT NULL,
	JSONDATA             varchar(1000000)   NOT NULL,
	NAME                 varchar(100)   NOT NULL,
	CONSTRAINT PK_SURVEY PRIMARY KEY ( ID )
 );

CREATE INDEX idx_SURVEY ON PUBLIC.SURVEY ( USER_ID );

ALTER TABLE PUBLIC.FILE ADD CONSTRAINT FK_FILES_VIDEOS FOREIGN KEY ( VIDEOS_ID ) REFERENCES PUBLIC.VIDEO( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE PUBLIC.SNAPSHOT ADD CONSTRAINT FK_SNAPSHOT FOREIGN KEY ( FILES_ID ) REFERENCES PUBLIC.VIDEO( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE PUBLIC.SURVEY ADD CONSTRAINT FK_SURVEY_USER FOREIGN KEY ( USER_ID ) REFERENCES PUBLIC."USER"( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE PUBLIC.USER_X_ROLE ADD CONSTRAINT FK_USER_X_ROLE_USER FOREIGN KEY ( USER_ID ) REFERENCES PUBLIC."USER"( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE PUBLIC.USER_X_ROLE ADD CONSTRAINT FK_USER_X_ROLE_ROLE FOREIGN KEY ( ROLE_ID ) REFERENCES PUBLIC.ROLE( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE PUBLIC.VIDEO ADD CONSTRAINT FK_VIDEOS_USER FOREIGN KEY ( USER_ID ) REFERENCES PUBLIC."USER"( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION;

