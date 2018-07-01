package org.igye.outline;

import org.hibernate.Session;
import org.igye.outline.common.NotImplementedException;

public class TestUtils {
    public static <T> T exploreDB(Session session) {
        session.doWork(connection -> org.h2.tools.Server.startWebServer(connection));
        return null;
    }

    public static void notImplemented() {
        throw new NotImplementedException();
    }

}
