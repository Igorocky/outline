
const ImageShortView = ({imgId}) => {
    return re('img', {key: "imageNode", src:"/be/image/" + imgId, style: {margin:"10px"}})
}