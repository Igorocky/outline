package org.igye.outline;

import org.hibernate.Session;
import org.igye.outline.common.NotImplementedException;

public class TestUtils {
    public static final String SQL_DEBUG_LOGGER_NAME = "sql-debug";

    public static void exploreDB(Session session) {
        session.doWork(connection -> org.h2.tools.Server.startWebServer(connection));
    }

    public static void notImplemented() {
        throw new NotImplementedException();
    }

}
