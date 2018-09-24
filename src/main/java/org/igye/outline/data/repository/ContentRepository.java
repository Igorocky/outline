package org.igye.outline.data.repository;

import org.igye.outline.modelv2.ContentV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<ContentV2, UUID> {
}
