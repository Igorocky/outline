package org.igye.outline.controllers;

import org.igye.outline.data.DaoUtils;
import org.igye.outline.htmlforms.SessionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import static org.igye.outline.common.OutlineUtils.getCurrentUser;

@Service
public class CommonModelMethods {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private DaoUtils daoUtils;

    public void initModel(Model model) {
        model.addAttribute("sessionData", sessionData);
        model.addAttribute("currentUser", sessionData.getCurrentUser().getName());
        model.addAttribute("isAdmin", daoUtils.isAdmin(getCurrentUser()));
    }
}
