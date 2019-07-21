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
	IMG_ID uuid,
	constraint IMAGE_FK_ID foreign key (ID) references NODE
);

create table TEXT
(
	ID uuid not null primary key,
	TEXT VARCHAR,
	constraint TEXT_FK_ID foreign key (ID) references NODE
);

alter table NODE add constraint NODE_FK_ICON_ID foreign key (ICON_ID) references IMAGE;