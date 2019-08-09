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

function patchNode(request,onSuccess) {
    doRpcCall("rpcPatchNode", {request:request}, onSuccess)
}

function setSingleTagForNode(nodeId,tagId,tagValue,tagRef,onSuccess) {
    const request = {}
    request[NODE.id] = nodeId
    request[NODE.tags] = {}
    request[NODE.tags][tagId] = [{}]
    request[NODE.tags][tagId][0][TAG.value] = tagValue
    request[NODE.tags][tagId][0][TAG.ref] = tagRef
    patchNode(request, onSuccess)
}

function removeTagFromNode(nodeId,tagId,onSuccess) {
    const request = {}
    request[NODE.id] = nodeId
    request[NODE.tags] = {}
    request[NODE.tags][tagId] = []
    patchNode(request, onSuccess)
}

function setSingleTagValueForNode(nodeId,tagId,tagValue,onSuccess) {
    setSingleTagForNode(nodeId,tagId,tagValue,null, onSuccess)
}

function setSingleTagRefForNode(nodeId,tagId,tagRef,onSuccess) {
    setSingleTagForNode(nodeId,tagId,null, tagRef, onSuccess)
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
        setSingleTagValueForNode(nodeId, TAG_ID.name, newName, onSuccess)
    } else {
        removeTagFromNode(nodeId, TAG_ID.name, onSuccess)
    }
}

function updateNodeIcon(nodeId,newIconId,onSuccess) {
    if (newIconId) {
        setSingleTagRefForNode(nodeId, TAG_ID.icon, newIconId, onSuccess)
    } else {
        removeTagFromNode(nodeId, TAG_ID.icon, onSuccess)
    }
}

function updateTextNodeText(nodeId,newText,onSuccess) {
    if (newText) {
        setSingleTagValueForNode(nodeId, TAG_ID.text, newText, onSuccess)
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
    doRpcCall("rpcPasteNodesFromClipboard", {to:idOfNodeToPasteToOrNull}, onSuccess)
}
