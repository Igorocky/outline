package org.igye.outline2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hibernate.Session;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.exceptions.OutlineException;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OutlineUtils {
    public static <T> T getSingleValue(List<T> values) {
        if (values.size() > 1) {
            throw new OutlineException("values.size() > 1");
        } else if (values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }

    public static <A> A nullSafeGetterWithDefault(A obj, A defaultValue) {
        return obj != null ? obj : defaultValue;
    }

    public static <A,B> B nullSafeGetterWithDefault(A obj, Function<A,B> getter, B defaultValue) {
        B result = nullSafeGetter(obj, getter);
        return result != null ? result : defaultValue;
    }
    public static <A,B> B nullSafeGetter(A obj, Function<A,B> getter) {
        return obj == null ? null : getter.apply(obj);
    }

    public static <A,B,C> C nullSafeGetter(A obj, Function<A,B> getter1, Function<B,C> getter2) {
        return nullSafeGetter(
                obj,
                a -> nullSafeGetter(
                        getter1.apply(a),
                        b -> getter2.apply(b)
                )
        );
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

    public static <A,B> Set<B> map(Set<A> set, Function<A,B> mapper) {
        Set<B> result = new HashSet<>();
        for (A a : set) {
            B b = mapper.apply(a);
            result.add(b);
        }
        return result;
    }

    public static <A,B> List<B> map(List<A> list, Function<A,B> mapper) {
        List<B> result = new ArrayList<>();
        for (A a : list) {
            B b = mapper.apply(a);
            result.add(b);
        }
        return result;
    }

    public static <A,B> List<B> map(A[] array, Function<A,B> mapper) {
        List<B> res = new ArrayList<>();
        for (A a : array) {
            res.add(mapper.apply(a));
        }
        return res;
    }

    public static <A,B> Set<B> mapToSet(A[] array, Function<A,B> mapper) {
        Set<B> set = new HashSet<>();
        for (A a : array) {
            B b = mapper.apply(a);
            set.add(b);
        }
        return set;
    }

    public static <A,B> Set<B> mapToSet(List<A> list, Function<A,B> mapper) {
        Set<B> set = new HashSet<>();
        for (A a : list) {
            B b = mapper.apply(a);
            set.add(b);
        }
        return set;
    }

    public static <E,K,V> Map<K,List<V>> mapToMap(Collection<E> collection,
                                                  Function<E,K> keyExtractor, Function<E,V> valueExtractor) {
        Map<K,List<V>> result = new HashMap<>();
        for (E elem : collection) {
            K key = keyExtractor.apply(elem);
            if (!result.containsKey(key)) {
                result.put(key, new ArrayList<>());
            }
            result.get(key).add(valueExtractor.apply(elem));
        }
        return result;
    }

    public static <T> Set<T> filter(Set<T> set, Predicate<T> predicate) {
        Set<T> result = new HashSet<>();
        for (T t : result) {
            if (predicate.test(t)) {
                result.add(t);
            }
        }
        return result;
    }

    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T elem : list) {
            if (predicate.test(elem)) {
                result.add(elem);
            }
        }
        return result;
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

    public static <E> List<E> listOf(E... elems) {
        return ImmutableList.copyOf(elems);
    }

    public static <E> Set<E> setOf(E... elems) {
        return ImmutableSet.copyOf(elems);
    }

    public static <K,V> Map<K,V> mapOf(K k1, V v1) {
        Map<K, V> resp = new HashMap<>();
        resp.put(k1, v1);
        return resp;
    }

    public static <K,V> Map<K,V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> resp = mapOf(k2,v2);
        resp.put(k1, v1);
        return resp;
    }

    public static <K,V> Map<K,V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> resp = mapOf(k2,v2,k3,v3);
        resp.put(k1, v1);
        return resp;
    }

    public static <T> void ifPresent(OptVal<T> optVal, Consumer<T> consumer) {
        if (optVal == null) {
            consumer.accept(null);
        } else {
            optVal.ifPresent(consumer);
        }
    }
}