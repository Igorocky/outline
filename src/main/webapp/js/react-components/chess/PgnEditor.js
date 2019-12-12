"use strict";

const PgnEditor = ({pgnStr, onSave, onCancel}) => {
    return re(ChessComponent, {
        showPracticeTab:false,
        onBackendCreated: backend => backend.call("loadFromPgn", {pgn:pgnStr, tabToOpen:"MOVES"}),
        onSave:onSave,
        onCancel:onCancel,
    })
}