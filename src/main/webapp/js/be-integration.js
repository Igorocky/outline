'use strict';

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

function getNodeById(id, responseHandler) {
    id = id?id:""
    doGetMocked({url: "/be/node/" + id, onSuccess: responseHandler, response:_.find(NODES, n => id == n[NODE.id])})
}

function patchNode(node,onSuccess) {
    doPatch("/be/node", node, onSuccess)
}

function createChildNode(currNode,onSuccess) {
    const request = {}
    request[NODE.parentId] = currNode[NODE.id]
    request[NODE.objectClass] = OBJECT_CLASS.node
    patchNode(request, onSuccess)
}