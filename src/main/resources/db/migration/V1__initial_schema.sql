create table NODE
(
	ID uuid not null primary key,
	CLAZZ VARCHAR(50),
	CREATED_WHEN TIMESTAMP,
	PARENT_NODE_ID uuid,
	ORD INTEGER,
	constraint NODE_FK foreign key (PARENT_NODE_ID) references NODE
);

create table REVINFO
(
	REV INTEGER auto_increment primary key,
	REVTSTMP BIGINT
);

create table NODE_AUD
(
	ID uuid not null,
	REV INTEGER not null,
	REVTYPE TINYINT,
	CLAZZ VARCHAR(50),
	CREATED_WHEN TIMESTAMP,
	ORD INTEGER,
	PARENT_NODE_ID uuid,
	primary key (ID, REV),
	constraint NODE_AUD_FK foreign key (REV) references REVINFO
);

create table TAG
(
	ID uuid not null primary key,
	NODE_ID uuid,
	TAG_ID VARCHAR not null,
	VALUE VARCHAR not null,
	constraint TAG_FK foreign key (NODE_ID) references NODE
);

create table TAG_AUD
(
	ID uuid not null,
	REV INTEGER not null,
	REVTYPE TINYINT,
	NODE_ID uuid,
	TAG_ID VARCHAR,
	VALUE VARCHAR,
	primary key (ID, REV),
	constraint TAG_AUD_FK foreign key (REV) references REVINFO
);

