package org.igye.outline.data.repository;

import org.igye.outline.modelv2.ImageV2;
import org.igye.outline.modelv2.UserV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<ImageV2, UUID> {
    ImageV2 findByOwnerAndId(UserV2 owner, UUID id);
}
