
const ImageShortView = ({node}) => {
    return RE.img({
        src:"/be/image/" + getTagSingleValue(node, TAG_ID.imgId),
        style: {margin:"10px"}
    })
}