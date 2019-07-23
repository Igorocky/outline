package org.igye.outline2.pm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
public class Text extends Node {
    private String text;
}
