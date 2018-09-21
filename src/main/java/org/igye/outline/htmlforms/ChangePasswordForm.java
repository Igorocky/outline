package org.igye.outline.htmlforms;

import lombok.Data;

@Data
public class ChangePasswordForm {
    private String oldPassword;
    private String newPassword1;
    private String newPassword2;
}
