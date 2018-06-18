package org.igye.outline.common;

import org.igye.outline.controllers.Authenticator;
import org.igye.outline.exceptions.OutlineException;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class OutlineUtils {
    public static <T> T accessDenied() {
        throw new OutlineException("Access denied.");
    }

    public static String hashPwd(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt(Authenticator.BCRYPT_SALT_ROUNDS));
    }

    public static boolean checkPwd(String pwd, String hashedPwd) {
        return BCrypt.checkpw(pwd, hashedPwd);
    }

}
