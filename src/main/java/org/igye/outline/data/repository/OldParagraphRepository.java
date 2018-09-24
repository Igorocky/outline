package org.igye.outline.data.repository;

import org.igye.outline.model.Paragraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OldParagraphRepository extends JpaRepository<Paragraph, UUID> {
    List<Paragraph> findByParentParagraphIsNull();
}
