package org.igye.outline.data.repository;

import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    Topic findByOwnerAndId(User owner, UUID id);
}
