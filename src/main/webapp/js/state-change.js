'use strict'

const NODE = {
    childNodes: "childNodes",
    objectClass: "objectClass",
    parentId: "parentId",
    id: "id",
    name: "name",
    text: "text"

}

const OBJECT_CLASS = {
    rootNode: "ROOT_NODE",
    node: "NODE",
    text: "TEXT",
    image: "IMAGE"
}

function addChildNode(state) {
    const result = _.extend({}, state)
    result[NODE.childNodes] = state[NODE.childNodes].slice()
    const newNode = {}
    newNode[NODE.objectClass] = OBJECT_CLASS.rootNode
    result[NODE.childNodes].push(newNode)
    return result
}