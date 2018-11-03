package org.igye.outline.data.repository;

import org.igye.outline.model.Image;
import org.igye.outline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
    Image findByOwnerAndId(User owner, UUID id);
}
