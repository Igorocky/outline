create table NODE
(
	ID uuid not null primary key,
	PARENT_NODE_ID uuid,
	NAME VARCHAR,
	ORD INTEGER not null,
	ICON_ID uuid,
	CREATED_WHEN TIMESTAMP not null,
	UPDATED_WHEN TIMESTAMP not null,
	constraint NODE_FK_PARENT_NODE_ID foreign key (PARENT_NODE_ID) references NODE
);

create table IMAGE
(
	ID uuid not null primary key,
	CREATED_WHEN TIMESTAMP not null,
	UPDATED_WHEN TIMESTAMP not null
);

create table IMAGE_REF
(
	ID uuid not null primary key,
	IMAGE_ID uuid,
	constraint IMAGE_REF_FK_ID foreign key (ID) references NODE,
	constraint IMAGE_REF_FK_IMAGE_ID foreign key (IMAGE_ID) references IMAGE
);

create table TEXT
(
	ID uuid not null primary key,
	TEXT VARCHAR,
	constraint TEXT_FK_ID foreign key (ID) references NODE
);

alter table NODE add constraint NODE_FK_ICON_ID foreign key (ICON_ID) references IMAGE;