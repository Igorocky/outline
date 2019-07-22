'use strict'

const CONTEXT_PATH = "/fe"

const PATH = {
    node: CONTEXT_PATH + "/node",
    nodeWithId: CONTEXT_PATH + "/node/:id",
    createNodeWithIdPath: id => CONTEXT_PATH + "/node/" + id,
    view1: CONTEXT_PATH + "/view1",
    view2: CONTEXT_PATH + "/view2",
}

function redirectTo(to) {
    return to ? re(Redirect,{key: to, to: to}) : null
}

const NODE_PARENT_ID = "parentId"
const NODE_ID = "id"
const NODE_NAME = "name"
const NODE_CHILDREN = "childNodes"
const NODE_ICON = "icon"
const NODE_OBJECT_CLASS = "objectClass"
const NODE_OBJECT_CLASS_TEXT = "TEXT"
const NODE_OBJECT_CLASS_NODE = "NODE"
const NODE_OBJECT_CLASS_IMAGE = "IMAGE"
const NODE_OBJECT_CLASS_ROOT_NODE = "ROOT_NODE"
const NODE_TEXT = "text"