package org.igye.outline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;

@Data
@AllArgsConstructor
@Builder
@Entity
public class Image extends Content {
}
