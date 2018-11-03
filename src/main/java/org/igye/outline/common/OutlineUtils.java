package org.igye.outline.common;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.igye.outline.config.UserDetailsImpl;
import org.igye.outline.exceptions.AccessDeniedException;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OutlineUtils {
    public static final String SQL_DEBUG_LOGGER_NAME = "sql-debug";
    public static final String NOTHING = "nothing";
    public static final String UUID_CHAR = "uuid-char";
    public static int BCRYPT_SALT_ROUNDS = 10;

    public static <T> T accessDenied() {
        throw new AccessDeniedException("Access denied.");
    }

    public static String hashPwd(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt(BCRYPT_SALT_ROUNDS));
    }

    public static boolean checkPwd(String pwd, String hashedPwd) {
        return BCrypt.checkpw(pwd, hashedPwd);
    }

    public static String prefix(String prefix, String url) {
        if (StringUtils.isEmpty(prefix)) {
            return url;
        } else {
            return "" + prefix + "/" + url;
        }
    }

    public static String redirect(String url) {
        return "redirect:/" + url;
    }

    public static String  redirect(HttpServletResponse response, String path, Map<String, Object> params) throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        params.forEach((k,v) -> builder.queryParam(k, v));
        response.sendRedirect("/" + path + "?" + builder.build().getQuery());
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

    public static <T> Optional<T> getNextSibling(List<T> list, Function<T,Boolean> comparator, boolean toTheRight) {
        if (CollectionUtils.isEmpty(list) ||
                !toTheRight && comparator.apply(list.get(0)) ||
                toTheRight && comparator.apply(list.get(list.size() - 1))) {
            return Optional.empty();
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (comparator.apply(list.get(i))) {
                    return Optional.of(list.get(i + (toTheRight ? 1 : -1)));
                }
            }
            throw new OutlineException("getNextSibling");
        }
    }

    public static <T> Optional<T> getFurthestSibling(List<T> list, Function<T,Boolean> comparator, Boolean toTheRight) {
        if (CollectionUtils.isEmpty(list) ||
                !toTheRight && comparator.apply(list.get(0)) ||
                toTheRight && comparator.apply(list.get(list.size() - 1))) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(toTheRight ? list.size() - 1 : 0));
        }
    }

    public static Session getCurrentSession(EntityManager entityManager) {
        return entityManager.unwrap(Session.class);
    }

    public static User getCurrentUser() {
        User user = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        return User.builder()
                .id(user.getId())
                .name(user.getName())
                .roles(user.getRoles())
                .build()
                ;
    }

    public static <A,B> Set<B> map(Set<A> collection, Function<A,B> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <A,B> List<B> map(List<A> collection, Function<A,B> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    public static <A,B> Set<B> mapToSet(List<A> collection, Function<A,B> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <T> Set<T> filter(Set<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(Collectors.toSet());
    }

    public static <T> List<T> filter(List<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(Collectors.toList());
    }

    public static <T> Set<T> filterToSet(List<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(Collectors.toSet());
    }

    public static <E,K,V> Map<K,V> toMap(Collection<E> collection, Function<E,K> keyExtr, Function<E,V> valueExtr) {
        return collection.stream().collect(Collectors.toMap(keyExtr, valueExtr));
    }

    public static <E,K> Map<K,E> toMap(Collection<E> collection, Function<E,K> keyExtr) {
        return toMap(collection, keyExtr, Function.identity());
    }
}
