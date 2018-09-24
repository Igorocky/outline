package org.igye.outline.data.repository;

import org.igye.outline.modelv2.NodeV2;
import org.igye.outline.modelv2.UserV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeRepository extends JpaRepository<NodeV2, UUID> {
    List<NodeV2> findByOwnerAndParentNodeIsNullOrderByName(UserV2 owner);
}
