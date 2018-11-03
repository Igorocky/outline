drop table IMAGE;
drop table PARAGRAPH_TAG;
drop table TEXT;
drop table CONTENT;
drop table SYNOPSISTOPIC;
drop table TOPIC_TAG;
drop table TAG;
drop table TOPIC;
drop table PARAGRAPH;
drop table USER_ROLE;
drop table ROLE;
drop table USER;

alter table CONTENTV2 rename to CONTENT;
alter table IMAGEV2 rename to IMAGE;
alter table NODEV2 rename to NODE;
alter table PARAGRAPHV2 rename to PARAGRAPH;
alter table ROLEV2 rename to ROLE;
alter table TEXTV2 rename to TEXT;
alter table TOPICV2 rename to TOPIC;
alter table USER_ROLE_V2 rename to USER_ROLE;
alter table USERV2 rename to USER;

ALTER TABLE USER_ROLE ALTER COLUMN USERV2_ID RENAME TO USER_ID;