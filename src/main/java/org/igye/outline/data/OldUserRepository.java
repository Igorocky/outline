package org.igye.outline.data;

import org.igye.outline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OldUserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByIdNotIn(List<UUID> ids);
}
