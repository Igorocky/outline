ALTER TABLE Topic DROP COLUMN IMAGES;

create table SynopsisTopic (
       id varchar(255) not null,
        primary key (id)
    );
alter table SynopsisTopic add constraint FKl1pwxce082ghp0jdm9yhdlxam foreign key (id) references Topic;

create table Content (
       id varchar(255) not null,
        owner_id varchar(255),
        topic_id varchar(255),
        contents_ORDER integer,
        primary key (id)
    );
alter table Content add constraint FKrye0mru34j4f7vqw3lhnx2yv1 foreign key (owner_id) references User;
alter table Content add constraint FK89dhuitcvk2bvnmf65ctmcdqi foreign key (topic_id) references SynopsisTopic;

create table Image (
       id varchar(255) not null,
        primary key (id)
    );
alter table Image add constraint FKpirkdt994ewsah700p8p41ray foreign key (id) references Content;

create table Text (
       text varchar(255),
        id varchar(255) not null,
        primary key (id)
    );
alter table Text add constraint FK19l58c1carkhq38klytde0faf foreign key (id) references Content;