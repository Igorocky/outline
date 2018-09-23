package org.igye.outline.data;

import org.igye.outline.common.OutlineUtils;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.modelv2.RoleV2;
import org.igye.outline.modelv2.UserV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class DaoUtils {
    @Autowired
    private SessionData sessionData;

    public <T> T doAsAdmin(Supplier<T> supplier) {
        if (!isAdmin(sessionData.getUser())) {
            return OutlineUtils.accessDenied();
        } else {
            return supplier.get();
        }
    }

    public <T> void doAsAdminV(Runnable runnable) {
        doAsAdmin(() -> {
            runnable.run();
            return null;
        });
    }


    public boolean isAdmin(UserV2 user) {
        for (RoleV2 role : user.getRoles()) {
            if (UserDao.ADMIN_ROLE_NAME.equals(role.getName())) {
                return true;
            }
        }
        return false;
    }
}
