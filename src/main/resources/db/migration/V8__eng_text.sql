CREATE TABLE WORD (
    ID VARCHAR(255) NOT NULL,
    OWNER_ID VARCHAR(255),
    ENG_TEXT_ID VARCHAR(255),
    LEARN_GROUP VARCHAR (100),
    WORD_IN_TEXT VARCHAR (100),
    WORD VARCHAR (100),
    TRANSCRIPTION VARCHAR (100),
    MEANING VARCHAR (4000)
);
ALTER TABLE WORD ADD CONSTRAINT WORD_PK PRIMARY KEY(ID);

CREATE TABLE ENG_TEXT(
    ID VARCHAR(255) NOT NULL,
    TEXT VARCHAR(30000) NOT NULL,
    IGNORE_LIST VARCHAR(4000) NOT NULL,
    LEARN_GROUPS VARCHAR(1000) NOT NULL,
    LANG VARCHAR(10) NOT NULL,
    PCT INTEGER NOT NULL

);
ALTER TABLE ENG_TEXT ADD CONSTRAINT ENG_TEXT PRIMARY KEY(ID);


ALTER TABLE ENG_TEXT ADD CONSTRAINT ENG_TEXT_TO_NODE FOREIGN KEY(ID) REFERENCES NODE(ID);
ALTER TABLE WORD ADD CONSTRAINT WORD_TO_USER FOREIGN KEY(OWNER_ID) REFERENCES USER(ID);
ALTER TABLE WORD ADD CONSTRAINT WORD_TO_ENG_TEXT FOREIGN KEY(ENG_TEXT_ID) REFERENCES ENG_TEXT(ID);