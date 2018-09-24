package org.igye.outline.controllers;

import fj.data.Validation;
import org.apache.commons.lang3.StringUtils;
import org.igye.outline.data.UserDaoV2;
import org.igye.outline.htmlforms.ChangePasswordForm;
import org.igye.outline.htmlforms.EditUserForm;
import org.igye.outline.modelv2.RoleV2;
import org.igye.outline.modelv2.UserV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.hashPwd;
import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.common.OutlineUtils.redirect;

@Controller
@RequestMapping(UsersController.PREFIX)
public class UsersController {
    protected static final String PREFIX = "";

    private static final String USERS = "users";
    private static final String CHANGE_PASSWORD = "changePassword";
    private static final String EDIT_USER = "editUser";

    @Value("${homeUrl}")
    private String homeUrl;
    @Autowired
    private UserDaoV2 userDao;
    @Autowired
    private CommonModelMethods commonModelMethods;


    @GetMapping(USERS)
    public String users(Model model) {
        commonModelMethods.initModel(model);
        model.addAttribute("users", userDao.loadUsers());
        return prefix(USERS);
    }

    @GetMapping(CHANGE_PASSWORD)
    public String changePassword(Model model) {
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        model.addAttribute("changePasswordForm", changePasswordForm);
        return prefix(CHANGE_PASSWORD);
    }

    @PostMapping(CHANGE_PASSWORD)
    public String changePasswordPost(Model model, ChangePasswordForm changePasswordForm) {
        model.addAttribute("changePasswordForm", changePasswordForm);
        if (!pass1EqPass2(changePasswordForm) || newPassIsEmpty(changePasswordForm)) {
            changePasswordForm.setErrorMsg("!pass1EqPass2 || newPassIsEmpty");
            return prefix(CHANGE_PASSWORD);
        } else {
            Validation<String, Void> res = userDao.changePassword(
                    changePasswordForm.getOldPassword(), changePasswordForm.getNewPassword1()
            );
            if (res.isSuccess()) {
                return redirect(homeUrl);
            } else {
                changePasswordForm.setErrorMsg(res.fail());
                return prefix(CHANGE_PASSWORD);
            }
        }
    }


    @GetMapping(EDIT_USER)
    public String editUser(Model model, @RequestParam Optional<UUID> userId) {
        commonModelMethods.initModel(model);
        model.addAttribute("allRoles", userDao.loadAllRoles());
        EditUserForm editUserForm = new EditUserForm();
        userId.ifPresent(id -> {
            UserV2 user = userDao.loadUser(id);
            editUserForm.setId(user.getId());
            editUserForm.setName(user.getName());
            editUserForm.getRoles().addAll(map(user.getRoles(), RoleV2::getId));
        });
        model.addAttribute("editUserForm", editUserForm);
        return prefix(EDIT_USER);
    }

    @PostMapping(EDIT_USER)
    public String editUserPost(Model model, EditUserForm editUserForm) {
        model.addAttribute("editUserForm", editUserForm);
        model.addAttribute("allRoles", userDao.loadAllRoles());
        if (editUserForm.isCreateNewUser()) {
            if (!pass1EqPass2(editUserForm) || newPassIsEmpty(editUserForm)) {
                editUserForm.setErrorMsg("!pass1EqPass2 || newPassIsEmpty");
                return prefix(EDIT_USER);
            } else {
                userDao.createUser(
                        editUserForm.getName(),
                        editUserForm.getNewPassword1(),
                        editUserForm.getRoles()
                );
                return redirect(prefix(USERS));
            }
        } else {
            boolean passwordWasChanged = !newPassIsEmpty(editUserForm);
            if (passwordWasChanged && !pass1EqPass2(editUserForm)) {
                editUserForm.setErrorMsg("!pass1EqPass2 || newPassIsEmpty");
                return prefix(EDIT_USER);
            } else {
                userDao.updateUser(
                        editUserForm.getId(),
                        user -> {
                            user.setName(editUserForm.getName());
                            if (passwordWasChanged) {
                                user.setPassword(hashPwd(editUserForm.getNewPassword1()));
                            }
                            user.getRoles().clear();
                            user.getRoles().addAll(userDao.loadRoles(editUserForm.getRoles()));
                        }
                );
                return redirect(prefix(USERS));
            }
        }
    }

    private boolean pass1EqPass2(ChangePasswordForm changePasswordForm) {
        return changePasswordForm.getNewPassword1().equals(changePasswordForm.getNewPassword2());
    }

    private boolean newPassIsEmpty(ChangePasswordForm changePasswordForm) {
        return StringUtils.isEmpty(StringUtils.trim(changePasswordForm.getNewPassword1()));
    }

    private boolean pass1EqPass2(EditUserForm editUserForm) {
        return editUserForm.getNewPassword1().equals(editUserForm.getNewPassword2());
    }

    private boolean newPassIsEmpty(EditUserForm editUserForm) {
        return StringUtils.isEmpty(StringUtils.trim(editUserForm.getNewPassword1()));
    }

    public static String prefix(String url) {
        return "" + PREFIX + "/" + url;
    }
}
