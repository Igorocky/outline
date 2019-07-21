package org.igye.outline2.pm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Image extends Node {
    private UUID imgId = UUID.randomUUID();
}
