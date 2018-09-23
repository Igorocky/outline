package org.igye.outline.controllers;

import org.igye.outline.data.NodeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(NodeController.PREFIX)
public class NodeController {
    protected static final String PREFIX = "v2";

    private static final String MIGRATE_DATA = "migrate-data";

    @Value("${homeUrl}")
    private String homeUrl;
    @Autowired
    private CommonModelMethods commonModelMethods;
    @Autowired
    private NodeDao nodeDao;



    @GetMapping(MIGRATE_DATA)
    public String migrateData(Model model) {
        commonModelMethods.initModel(model);
//        nodeDao.migrateData();
        return prefix(homeUrl);
    }

    public static String prefix(String url) {
        return "" + PREFIX + "/" + url;
    }
}
