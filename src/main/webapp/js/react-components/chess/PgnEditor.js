"use strict";

const PgnEditor = ({pgnStr, onSave}) => {
    return RE.Paper({}, re(ChessComponent, {
        showPracticeTab:false,
        onBackendCreated: backend => backend.call("loadFromPgn", {pgn:pgnStr, tabToOpen:"MOVES"}),
    }))
}