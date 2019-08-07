'use strict';

const NODE = {
    id: "id",
    objectClass: "clazz",
    tags: "tags",
    parentId: "parentId",
    childNodes: "childNodes",
    path: "path",
    canPaste: "canPaste"
}

const OBJECT_CLASS = {
    rootNode: "TOP_CONTAINER",
    node: "CONTAINER",
    text: "TEXT",
    image: "IMAGE"
}

const TAG = {
    tagId: "tagId",
    ref: "ref",
    value: "value"
}

const TAG_ID = {
    name: "name",
    icon: "icon",
    text: "text"
}

function doRpcCall(methodName, params, onSuccess) {
    doPatch("/be/rpc/" + methodName, params, onSuccess)
}

function getNode(params, onSuccess) {
    doRpcCall("getNode", params, onSuccess)
}

function getNodeById(id, responseHandler) {
    getNode({id:id, depth: 1, includeCanPaste: true}, responseHandler)
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
    request[NODE.tags] = {}
    if (newText) {
        request[NODE.tags][TAG_ID.text] = [{}];
        request[NODE.tags][TAG_ID.text][0][TAG.value] = newText
    } else {
        request[NODE.tags][TAG_ID.text] = [];
    }
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

function putNodeIdsToClipboard(nodeIds,onSuccess) {
    doPatch("/be/putNodeIdsToClipboard", nodeIds, onSuccess)
}

function canPasteNodesFromClipboard(idOfNodeToPasteToOrNull,onSuccess) {
    doGet("/be/canPasteNodesFromClipboard/" + idOfNodeToPasteToOrNull, onSuccess)
}

function pasteNodesFromClipboard(idOfNodeToPasteToOrNull,onSuccess) {
    doPatch("/be/pasteNodesFromClipboard/" + idOfNodeToPasteToOrNull, {}, onSuccess)
}
