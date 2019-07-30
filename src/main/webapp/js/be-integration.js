'use strict';

const NODE = {
    childNodes: "childNodes",
    objectClass: "objectClass",
    parentId: "parentId",
    id: "id",
    name: "name",
    path: "path",
    icon: "icon",
    imgId: "imgId",
    text: "text"
}

const OBJECT_CLASS = {
    rootNode: "ROOT_NODE",
    node: "NODE",
    text: "TEXT",
    image: "IMAGE"
}

function getNodeById(id, responseHandler) {
    const url = (id?("/be/node/" + id):"/be/node") + "?depth=1"
    doGet(url, responseHandler)
}

function patchNode(request,onSuccess) {
    doPatch("/be/node", request, onSuccess)
}

function reorderNode(nodeId,direction,onSuccess) {
    doPatch("/be/reorderNode/" + nodeId + "/" + direction, {}, onSuccess)
}

function createChildNode(currNode,onSuccess) {
    const request = {}
    request[NODE.parentId] = currNode[NODE.id]
    request[NODE.objectClass] = OBJECT_CLASS.node
    patchNode(request, onSuccess)
}

function createChildTextNode(currNode,onSuccess) {
    const request = {}
    request[NODE.parentId] = currNode[NODE.id]
    request[NODE.objectClass] = OBJECT_CLASS.text
    patchNode(request, onSuccess)
}

function createChildImageNode(currNode,onSuccess) {
    const request = {}
    request[NODE.parentId] = currNode[NODE.id]
    request[NODE.objectClass] = OBJECT_CLASS.image
    patchNode(request, onSuccess)
}

function updateNodeName(nodeId,newName,onSuccess) {
    const request = {}
    request[NODE.id] = nodeId
    request[NODE.name] = newName
    patchNode(request, onSuccess)
}

function updateNodeIcon(nodeId,newIconId,onSuccess) {
    const request = {}
    request[NODE.id] = nodeId
    request[NODE.icon] = newIconId
    patchNode(request, onSuccess)
}

function updateTextNodeText(nodeId,newText,onSuccess) {
    const request = {}
    request[NODE.id] = nodeId
    request[NODE.text] = newText
    patchNode(request, onSuccess)
}

function updateImageNodeImage(nodeId,newImageId,onSuccess) {
    const request = {}
    request[NODE.id] = nodeId
    request[NODE.imgId] = newImageId
    patchNode(request, onSuccess)
}

function moveNodeToStart(nodeId,onSuccess) {
    reorderNode(nodeId, 1, onSuccess)
}

function moveNodeUp(nodeId,onSuccess) {
    reorderNode(nodeId, 2, onSuccess)
}

function moveNodeDown(nodeId,onSuccess) {
    reorderNode(nodeId, 3, onSuccess)
}

function moveNodeToEnd(nodeId,onSuccess) {
    reorderNode(nodeId, 4, onSuccess)
}