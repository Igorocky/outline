package org.igye.outline.data;

import org.igye.outline.modelv2.ParagraphV2;
import org.igye.outline.modelv2.UserV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParagraphRepository extends JpaRepository<ParagraphV2, UUID> {
    ParagraphV2 findByOwnerAndId(UserV2 owner, UUID id);
}
