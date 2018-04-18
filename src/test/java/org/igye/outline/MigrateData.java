package org.igye.outline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.igye.outline.datamigration.MigrateConfig;
import org.igye.outline.datamigration.Migrator;
import org.igye.outline.model.User;
import org.igye.outline.oldmodel.ParagraphOld;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MigrateConfig.class)
public class MigrateData {
    private static final Logger LOG = LogManager.getLogger(MigrateData.class);

    @Autowired
    private Migrator migrator;

    @Test
    @Commit
    public void migrate() {
        List<ParagraphOld> paragraphOlds = migrator.loadOldData();
        User owner = new User();
        owner.setName("igor");
        owner.setPassword("igor");
        migrator.saveNewData(owner, paragraphOlds);
    }



}