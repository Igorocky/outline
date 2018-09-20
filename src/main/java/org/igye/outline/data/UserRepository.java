package org.igye.outline.data;

import org.igye.outline.modelv2.UserV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserV2, UUID> {
    UserV2 findByName(String name);
}
