package org.igye.outline.data.repository;

import org.igye.outline.model.User;
import org.igye.outline.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WordRepository extends JpaRepository<Word, UUID> {
    Word findByOwnerAndId(User owner, UUID id);
}
