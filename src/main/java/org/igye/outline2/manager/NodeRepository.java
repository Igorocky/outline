package org.igye.outline2.manager;

import org.igye.outline2.pm.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeRepository extends JpaRepository<Node, UUID> {
    List<Node> findByParentNodeId(UUID parentId);
}
