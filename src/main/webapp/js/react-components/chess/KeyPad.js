"use strict";

const KeyPad = ({keys}) => {
    return RE.Container.col.top.left({}, {style: {marginBottom:"1px"}},
        keys.map((row,ri) => RE.ButtonGroup({key:ri, variant:"contained"},
            row.map((key,ki) => RE.Button({
                    key:ki,
                    style:{width:"1em"},
                    onClick: key.onClick,
                },
                key.symbol
            ))
        ))
    )
}