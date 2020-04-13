"use strict";

const KeyPad = ({keys, componentKey, size, variant, buttonWidth}) => {
    function getKeyContent(key) {
        if (hasValue(key.symbol)) {
            return key.symbol
        } else if (hasValue(key.icon)) {
            return key.icon
        } else if (hasValue(key.iconName)) {
            return RE.Icon({}, key.iconName)
        }
    }

    return RE.Container.col.top.left({key:componentKey}, {style: {marginBottom:"1px"}},
        keys.map((row,ri) => RE.ButtonGroup({key:ri, variant:variant?variant:"contained", size:size?size:"large"},
            row.map((key,ki) => RE.Button({
                    key:ki,
                    style:{width:buttonWidth?buttonWidth:"1em", ...(key.style?key.style:{})},
                    disabled: key.disabled,
                    onClick: () => key.onClick?key.onClick():null,
                },
                getKeyContent(key)
            ))
        ))
    )
}