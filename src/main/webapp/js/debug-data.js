'use strict';

const NODE_PARENT_ID = "parentId"
const NODE_ID = "id"
const NODE_NAME = "name"
const NODE_CHILDREN = "children"
const NODE_OBJECT_CLASS = "objectClass"
const NODE_OBJECT_CLASS_TEXT = "TEXT"
const NODE_OBJECT_CLASS_NODE = "NODE"
const NODE_TEXT = "text"

const NODE0 = {
    [NODE_PARENT_ID]: null,
    [NODE_ID]: "0",
    [NODE_CHILDREN]: [
        {
            [NODE_ID]: "c-id1",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_TEXT,
            [NODE_TEXT]: "this is a text in node 0"
        },
        {
            [NODE_ID]: "c-id2",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_TEXT,
            [NODE_TEXT]: "yi\nolu\ni,m\nnrg\nbfcw"
        },
        {
            [NODE_ID]: "1",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_NODE,
            [NODE_NAME]: "node1"
        }
    ]
}

const NODE1 = {
    [NODE_PARENT_ID]: NODE0[NODE_ID],
    [NODE_ID]: "1",
    [NODE_CHILDREN]: [
        {
            [NODE_ID]: "2",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_NODE,
            [NODE_NAME]: "node2"
        },
        {
            [NODE_ID]: "c-id1",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_TEXT,
            [NODE_TEXT]: "asdf asdf asdf asfd as f"
        },
        {
            [NODE_ID]: "c-id2",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_TEXT,
            [NODE_TEXT]: "yi\nolu\ni,m\nnrg\nbfcw"
        },
        {
            [NODE_ID]: "c-id3",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_TEXT,
            [NODE_TEXT]: "xssf667456354efdchv"
        }
    ]
}

const NODE2 = {
    [NODE_PARENT_ID]: NODE1[NODE_ID],
    [NODE_ID]: "2",
    [NODE_CHILDREN]: [
        {
            [NODE_ID]: "c-id1",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_TEXT,
            [NODE_TEXT]: "AAA"
        },
        {
            [NODE_ID]: "c-id2",
            [NODE_OBJECT_CLASS]: NODE_OBJECT_CLASS_TEXT,
            [NODE_TEXT]: "BBB"
        }
    ]
}

const NODES = [NODE0, NODE1, NODE2]