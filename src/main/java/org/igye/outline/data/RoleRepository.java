package org.igye.outline.data;

import org.igye.outline.modelv2.RoleV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleV2, UUID> {
}
