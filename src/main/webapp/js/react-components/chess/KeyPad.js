"use strict";

const KeyPad = ({keys, componentKey}) => {
    return RE.Container.col.top.left({key:componentKey}, {style: {marginBottom:"1px"}},
        keys.map((row,ri) => RE.ButtonGroup({key:ri, variant:"contained", size:"large"},
            row.map((key,ki) => RE.Button({
                    key:ki,
                    style:{width:"1em", ...(key.style?key.style:{})},
                    onClick: () => key.onClick?key.onClick():null,
                },
                hasValue(key.symbol)?key.symbol:key.icon
            ))
        ))
    )
}