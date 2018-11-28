package org.igye.outline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.UUID_CHAR;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "WORD")
public class Word {
    @Id
    @Type(type = UUID_CHAR)
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "OWNER_ID")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "ENG_TEXT_ID")
    private EngText engText;

    @Column(name = "LEARN_GROUP")
    private String group;
    @Column(name = "WORD_IN_TEXT")
    private String wordInText;
    @Column(name = "WORD")
    private String word;
    @Column(name = "TRANSCRIPTION")
    private String transcription;
    @Column(name = "MEANING")
    private String meaning;
}
