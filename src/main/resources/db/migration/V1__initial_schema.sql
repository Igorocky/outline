create table IMAGE
(
	ID uuid not null primary key,
	CREATED_WHEN TIMESTAMP not null
);

create table NODE
(
	ID uuid not null primary key,
	NAME VARCHAR,
	CREATED_WHEN TIMESTAMP not null,
	ORD INTEGER not null,
	ICON_ID uuid,
	PARENT_NODE_ID uuid,
	constraint NODE_FK_PARENT_NODE_ID foreign key (PARENT_NODE_ID) references NODE,
	constraint NODE_FK_ICON_ID foreign key (ICON_ID) references IMAGE
);

create table IMAGE_REF
(
	ID uuid not null primary key,
	IMAGE_ID uuid,
	constraint IMAGE_REF_FK_IMAGE_ID foreign key (IMAGE_ID) references IMAGE,
	constraint IMAGE_REF_FK_ID foreign key (ID) references NODE
);

create table REVINFO
(
	REV INTEGER auto_increment primary key,
	REVTSTMP BIGINT
);

create table IMAGE_AUD
(
	ID uuid not null,
	REV INTEGER not null,
	REVTYPE TINYINT,
	CREATED_WHEN TIMESTAMP not null,
	primary key (ID, REV),
	constraint IMAGE_AUD_FK_REV foreign key (REV) references REVINFO
);

create table NODE_AUD
(
	ID uuid not null,
	REV INTEGER not null,
	REVTYPE TINYINT,
	CREATED_WHEN TIMESTAMP not null,
	NAME VARCHAR,
	ORD INTEGER not null,
	ICON_ID uuid,
	PARENT_NODE_ID uuid,
	primary key (ID, REV),
	constraint NODE_AUD_FK_REV foreign key (REV) references REVINFO
);

create table IMAGE_REF_AUD
(
	ID uuid not null,
	REV INTEGER not null,
	IMAGE_ID uuid,
	primary key (ID, REV),
	constraint IMAGE_REF_AUD_FK_ID_REV foreign key (ID, REV) references NODE_AUD
);

create table TEXT
(
	TEXT VARCHAR,
	ID uuid not null primary key,
	constraint TEXT_FK_ID foreign key (ID) references NODE
);

create table TEXT_AUD
(
	ID uuid not null,
	REV INTEGER not null,
	TEXT VARCHAR,
	primary key (ID, REV),
	constraint TEXT_AUD_ID_REV foreign key (ID, REV) references NODE_AUD
);

