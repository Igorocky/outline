package org.igye.outline.data;

import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.modelv2.UserV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.igye.outline.common.OutlineUtils.hashPwd;
import static org.igye.outline.common.OutlineUtils.map;

@Component
public class UserDaoV2 {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private DaoUtils daoUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public List<UserV2> loadUsers() {
        return doAsAdmin(() -> userRepository.findAll());
    }

    @Transactional
    public UserV2 loadUser(UUID id) {
        return doAsAdmin(() -> userRepository.getOne(id));
    }

    @Transactional
    public void createUser(String name, String password, Set<UUID> roles) {
        doAsAdminV(() -> {
            UserV2 newUser = new UserV2();
            newUser.setName(name);
            newUser.setPassword(hashPwd(password));
            newUser.setRoles(map(roles, roleRepository::getOne));
            userRepository.save(newUser);
        });
    }

    @Transactional
    public void updateUser(UUID id, Consumer<UserV2> updates) {
        doAsAdminV(() -> {
            UserV2 user = userRepository.getOne(id);
            Boolean lockedBefore = user.getLocked();
            updates.accept(user);
            if (sessionData.getUser().getId().equals(id) && !lockedBefore && user.getLocked()) {
                user.setLocked(false);
            }
        });
    }

    private <T> T doAsAdmin(Supplier<T> supplier) {
        return daoUtils.doAsAdmin(supplier);
    }

    private <T> void doAsAdminV(Runnable runnable) {
        daoUtils.doAsAdminV(runnable);
    }
}
