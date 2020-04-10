package org.igye.outline2.manager;

import org.apache.commons.io.FileUtils;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.TagIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.util.UUID;

import static org.igye.outline2.common.OutlineUtils.getImgFile;

@Component
public class ImageManager {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();

    @Value("${images.location}")
    private String imagesLocation;
    @Autowired
    private DtoConverter dtoConverter;

    @Transactional
    public NodeDto createImage(UUID parentId, MultipartFile file, boolean isNodeIcon) throws IOException {
        Node image = createNewImage(parentId, isNodeIcon);

        File imgFile = getImgFile(imagesLocation, UUID.fromString(image.getTagSingleValue(TagIds.IMG_ID)));
        File parentDir = imgFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(new File(imgFile.getAbsolutePath()));

        return dtoConverter.toDto(image, 0, tag -> true);
    }

    @Transactional
    public Node createNewImage(UUID parentId, boolean isNodeIcon) {
        Node image = new Node();
        image.setClazz(isNodeIcon ? NodeClasses.NODE_ICON : NodeClasses.IMAGE);
        image.setCreatedWhen(clock.instant());
        image.setTagSingleValue(TagIds.IMG_ID, UUID.randomUUID().toString());
        if (parentId != null) {
            final Node parentNode = nodeRepository.getOne(parentId);
            parentNode.addChild(image);
            parentNode.setTagSingleValue(TagIds.NODE_ICON_IMG_ID, image.getTagSingleValue(TagIds.IMG_ID));
        } else {
            nodeRepository.save(image);
        }
        return image;
    }

    public byte[] getImgFileById(UUID id) {
        try {
            return FileUtils.readFileToByteArray(getImgFile(imagesLocation, id).getAbsoluteFile());
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }
}
