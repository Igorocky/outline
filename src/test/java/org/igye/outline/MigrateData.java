package org.igye.outline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.igye.outline.controllers.Authenticator;
import org.igye.outline.datamigration.MigrateConfig;
import org.igye.outline.datamigration.Migrator;
import org.igye.outline.model.User;
import org.igye.outline.oldmodel.ParagraphOld;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MigrateConfig.class)
public class MigrateData {
    private static final Logger LOG = LogManager.getLogger(MigrateData.class);
    private String oldImagesDir = "D:\\Books\\math\\zorich-img";
    private String newImagesDir = "D:\\temp\\outline-dev\\images";

    @Autowired
    private Migrator migrator;

    @Test
    @Commit
    @Ignore
    public void createSchema() {
        migrator.createSchema();
    }

    @Test
    @Commit
    @Ignore
    public void migrateData() {
        for (String file: new File(newImagesDir).list()) {
            if (!".".equals(file) && !"..".equals(file)) {
                throw new RuntimeException("newImagesDir is not empty.");
            }
        }
        List<ParagraphOld> paragraphsOld = migrator.loadOldData();
        migrator.saveNewData("qweqweqwe", paragraphsOld);
        migrator.migrateImages(oldImagesDir, newImagesDir, paragraphsOld);
    }



}