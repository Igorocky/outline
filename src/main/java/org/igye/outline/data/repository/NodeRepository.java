package org.igye.outline.data.repository;

import org.igye.outline.model.Node;
import org.igye.outline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeRepository extends JpaRepository<Node, UUID> {
    List<Node> findByOwnerAndParentNodeIsNullOrderByName(User owner);
    Node findByOwnerAndId(User owner, UUID id);
}
