'use strict';

const NODE_PARENT_ID = "parentId"
const NODE_ID = "id"
const NODE_CHILDREN = "children"
const NODE_OBJECT_CLASS = "objectClass"
const NODE_OBJECT_CLASS_TEXT = "TEXT"
const NODE_TEXT = "text"

const NODE = {
    [NODE_PARENT_ID]: "parentId",
    [NODE_ID]: "id",
    [NODE_CHILDREN]: [
        {
            [NODE_ID]: "c-id1",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_TEXT,
            [NODE_TEXT]: "asdf asdf asdf asfd as f"
        },
        {
            [NODE_ID]: "c-id2",
            [NODE_OBJECT_CLASS]: "TEXT",
            [NODE_TEXT]: "yi\nolu\ni,m\nnrg\nbfcw"
        },
        {
            [NODE_ID]: "c-id3",
            [NODE_OBJECT_CLASS]: "TEXT",
            [NODE_TEXT]: "xssf667456354efdchv"
        }
    ]
}