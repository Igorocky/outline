package org.igye.outline.htmlforms;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class EditUserForm {
    private UUID id;
    private String name;
    private Set<UUID> roles = new HashSet<>();
    private String newPassword1;
    private String newPassword2;
    private String errorMsg;

    public boolean isCreateNewUser() {
        return id == null;
    }
}
