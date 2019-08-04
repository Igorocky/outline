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

import static org.igye.outline2.OutlineUtils.getImgFile;

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
        Image image = createNewImage();

        File imgFile = getImgFile(imagesLocation, image.getId());
        File parentDir = imgFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(new File(imgFile.getAbsolutePath()));

        return DtoConverter.toDto(image);
    }

    @Transactional
    public Image createNewImage() {
        Image image = new Image();
        image.setCreatedWhen(clock.instant());
        imageRepository.save(image);
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
