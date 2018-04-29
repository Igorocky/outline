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
    private String newImagesDir = "D:\\Books\\math\\zorich2-img";

    @Autowired
    private Migrator migrator;

    @Test
    @Commit
    @Ignore
    public void migrate() {
        for (String file: new File(newImagesDir).list()) {
            if (!".".equals(file) && !"..".equals(file)) {
                throw new RuntimeException("newImagesDir is not empty.");
            }
        }
        List<ParagraphOld> paragraphsOld = migrator.loadOldData();
        User owner = new User();
        owner.setName("igor");
        owner.setPassword(BCrypt.hashpw("igor", BCrypt.gensalt(Authenticator.BCRYPT_SALT_ROUNDS)));
        migrator.saveNewData(owner, paragraphsOld);
        migrator.migrateImages(oldImagesDir, newImagesDir, paragraphsOld);
    }



}