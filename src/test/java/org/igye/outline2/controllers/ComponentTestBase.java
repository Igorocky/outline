package org.igye.outline2.controllers;

import org.igye.outline2.App;
import org.igye.outline2.manager.NodeManager;
import org.igye.outline2.manager.NodeRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@ContextConfiguration(classes = ComponentTestConfig.class)
public class ComponentTestBase {
    @Autowired
    protected NodeManager nodeManager;
    @Autowired
    protected NodeRepository nodeRepository;
}