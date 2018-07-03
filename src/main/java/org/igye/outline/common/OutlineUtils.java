package org.igye.outline.common;

import org.igye.outline.controllers.Authenticator;
import org.igye.outline.exceptions.OutlineException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class OutlineUtils {
    public static final String SQL_DEBUG_LOGGER_NAME = "sql-debug";
    public static final String NOTHING = "nothing";

    public static <T> T accessDenied() {
        throw new OutlineException("Access denied.");
    }

    public static String hashPwd(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt(Authenticator.BCRYPT_SALT_ROUNDS));
    }

    public static boolean checkPwd(String pwd, String hashedPwd) {
        return BCrypt.checkpw(pwd, hashedPwd);
    }

    public static String redirect(HttpServletResponse response, String path, Map<String, Object> params) throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        params.forEach((k,v) -> builder.queryParam(k, v));
        response.sendRedirect(path + "?" + builder.build().getQuery());
        return NOTHING;
    }

    public static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new OutlineException("obj == null");
        }
    }

    public static File getImgFile(String imagesLocation, UUID imgId) {
        String idStr = imgId.toString();
        return new File(imagesLocation + "/" + idStr.substring(0,2) + "/" + idStr);
    }

}
