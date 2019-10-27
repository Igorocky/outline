'use strict';

const NODE0 = {
    [NODE.parentId]: null,
    [NODE.id]: "0",
    [NODE.childNodes]: [
        {
            [NODE.id]: "11",
            [NODE.objectClass]: OBJECT_CLASS.text,
            [NODE.text]: "this is a text in node 0"
        },
        {
            [NODE.id]: "12",
            [NODE.objectClass]: OBJECT_CLASS.text,
            [NODE.text]: "yi\nolu\ni,m\nnrg\nbfcw"
        },
        {
            [NODE.id]: "1",
            [NODE.objectClass]: OBJECT_CLASS.container,
            [NODE.name]: "node1"
        }
    ]
}

const NODE1 = {
    [NODE.parentId]: NODE0[NODE.id],
    [NODE.id]: "1",
    [NODE.childNodes]: [
        {
            [NODE.id]: "2",
            [NODE.objectClass]: OBJECT_CLASS.container,
            [NODE.name]: "node2"
        },
        {
            [NODE.id]: "13",
            [NODE.objectClass]: OBJECT_CLASS.text,
            [NODE.text]: "this is a text in node 1"
        },
        {
            [NODE.id]: "14",
            [NODE.objectClass]: OBJECT_CLASS.text,
            [NODE.text]: "yi\nolu\ni,m\nnrg\nbfcw"
        },
        {
            [NODE.id]: "15",
            [NODE.objectClass]: OBJECT_CLASS.text,
            [NODE.text]: "xssf667456354efdchv"
        }
    ]
}

const NODE2 = {
    [NODE.parentId]: NODE1[NODE.id],
    [NODE.id]: "2",
    [NODE.childNodes]: [
        {
            [NODE.id]: "16",
            [NODE.objectClass]: OBJECT_CLASS.text,
            [NODE.text]: "this is a text in node 2"
        },
        {
            [NODE.id]: "17",
            [NODE.objectClass]: OBJECT_CLASS.text,
            [NODE.text]: "BBB"
        }
    ]
}

const NODES = [NODE0, NODE1, NODE2]