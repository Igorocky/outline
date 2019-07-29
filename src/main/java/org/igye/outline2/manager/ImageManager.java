package org.igye.outline2.manager;

import org.apache.commons.io.FileUtils;
import org.igye.outline2.dto.ImageDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.util.UUID;

@Component
public class ImageManager {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();

    @Value("${images.location}")
    private String imagesLocation;

    @Transactional
    public ImageDto createImage(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setCreatedWhen(clock.instant());
        imageRepository.save(image);

        File imgFile = getImgFile(imagesLocation, image.getId());
        File parentDir = imgFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(new File(imgFile.getAbsolutePath()));

        return DtoConverter.toDto(image);
    }

    public byte[] getImgFileById(UUID id) {
        try {
            return FileUtils.readFileToByteArray(getImgFile(imagesLocation, id).getAbsoluteFile());
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }

    private File getImgFile(String imagesLocation, UUID imgId) {
        String idStr = imgId.toString();
        return new File(imagesLocation + "/" + idStr.substring(0,2) + "/" + idStr);
    }
}
