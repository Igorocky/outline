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
    text: "text",
    imgId: "imgId",
}

function doRpcCall(methodName, params, onSuccess) {
    doPatch("/be/rpc/" + methodName, params, onSuccess)
}

function getNode(params, onSuccess) {
    doRpcCall("rpcGetNode", params, onSuccess)
}

function patchNode(nodeDto,onSuccess) {
    doRpcCall("rpcPatchNode", {nodeDto:nodeDto}, onSuccess)
}

function setSingleTagForNode(nodeId,tagId,value,onSuccess) {
    doRpcCall("rpcSetSingleTagForNode", {nodeId:nodeId, tagId:tagId, value:value}, onSuccess)
}

function removeTagFromNode(nodeId,tagId,onSuccess) {
    doRpcCall("rpcRemoveTagsFromNode", {nodeId:nodeId, tagId:tagId}, onSuccess)
}

function getNodeById(id, responseHandler) {
    getNode({id:id, depth: 1, includeCanPaste: true}, responseHandler)
}

function reorderNode(nodeId,direction,onSuccess) {
    doRpcCall("rpcReorderNode", {nodeId:nodeId, direction:direction}, onSuccess)
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
    if (newName) {
        setSingleTagForNode(nodeId, TAG_ID.name, newName, onSuccess)
    } else {
        removeTagFromNode(nodeId, TAG_ID.name, onSuccess)
    }
}

function updateNodeIcon(nodeId,newIconId,onSuccess) {
    if (newIconId) {
        setSingleTagForNode(nodeId, TAG_ID.icon, newIconId, onSuccess)
    } else {
        removeTagFromNode(nodeId, TAG_ID.icon, onSuccess)
    }
}

function updateTextNodeText(nodeId,newText,onSuccess) {
    if (newText) {
        setSingleTagForNode(nodeId, TAG_ID.text, newText, onSuccess)
    } else {
        removeTagFromNode(nodeId, TAG_ID.text, onSuccess)
    }
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
    doRpcCall("rpcPutNodeIdsToClipboard", {ids:nodeIds}, onSuccess)
}

function pasteNodesFromClipboard(idOfNodeToPasteToOrNull,onSuccess) {
    doRpcCall("rpcMoveNodesFromClipboard", {to:idOfNodeToPasteToOrNull}, onSuccess)
}
