package org.igye.outline.data;

import org.igye.outline.modelv2.UserV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Component
public class UserDaoV2 {
    @Autowired
    private DaoUtils daoUtils;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public List<UserV2> loadUsers() {
        return doAsAdmin(() -> userRepository.findAll());
    }

    private <T> T doAsAdmin(Supplier<T> supplier) {
        return daoUtils.doAsAdmin(supplier);
    }
}
