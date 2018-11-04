package org.igye.outline.data.repository;

import org.igye.outline.model.Icon;
import org.igye.outline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IconRepository extends JpaRepository<Icon, UUID> {
    Icon findByOwnerAndId(User owner, UUID id);
}
