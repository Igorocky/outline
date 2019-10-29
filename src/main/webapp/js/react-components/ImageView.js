
const ImageShortView = ({node}) => {
    return re('img', {
        key: "imageNode",
        src:"/be/image/" + getTagSingleValue(node, TAG_ID.imgId),
        style: {margin:"10px"}
    })
}