"use strict";

const NodeIcon = ({imgId, popupActions}) => {
    const [anchorEl, setAnchorEl] = useState(null);

    function openPopup(e) {
        setAnchorEl(e.currentTarget)
    }

    if (!imgId) {
        return null
    } else {
        return RE.Fragment({key:imgId},
            RE.img({src:"/be/image/" + imgId, onClick: popupActions?openPopup:()=>null}),
            anchorEl
                ? clickAwayListener({
                    onClickAway: () => setAnchorEl(null),
                    children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                        RE.Paper({}, popupActions)
                    )
                })
                : null
        )

    }
}