package org.igye.outline.data.repository;

import org.igye.outline.model.Paragraph;
import org.igye.outline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParagraphRepository extends JpaRepository<Paragraph, UUID> {
    Paragraph findByOwnerAndId(User owner, UUID id);
}
