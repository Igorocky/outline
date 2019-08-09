package org.igye.outline2.manager;

import org.apache.commons.io.FileUtils;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClass;
import org.igye.outline2.pm.TagId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.getImgFile;

@Component
public class ImageManager {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();

    @Value("${images.location}")
    private String imagesLocation;

    @Transactional
    public NodeDto createImage(UUID parentId, MultipartFile file) throws IOException {
        Node image = createNewImage(parentId);

        File imgFile = getImgFile(imagesLocation, image.getTagSingleRef(TagId.IMG_ID));
        File parentDir = imgFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(new File(imgFile.getAbsolutePath()));

        return DtoConverter.toDto(image, 0);
    }

    @Transactional
    public Node createNewImage(UUID parentId) {
        Node image = new Node();
        image.setClazz(NodeClass.IMAGE);
        image.setCreatedWhen(clock.instant());
        image.setTagSingleValue(TagId.IMG_ID, UUID.randomUUID());
        if (parentId != null) {
            nodeRepository.getOne(parentId).addChild(image);
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
