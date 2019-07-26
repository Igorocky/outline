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

