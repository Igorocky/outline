package org.igye.outline.data;

import fj.data.Validation;
import org.igye.outline.data.repository.RoleRepository;
import org.igye.outline.data.repository.UserRepository;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.modelv2.RoleV2;
import org.igye.outline.modelv2.UserV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.igye.outline.common.OutlineUtils.BCRYPT_SALT_ROUNDS;
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
            Boolean lockedBefore = user.isLocked();
            updates.accept(user);
            if (sessionData.getCurrentUser().getId().equals(id) && !lockedBefore && user.isLocked()) {
                user.setLocked(false);
            }
        });
    }

    @Transactional
    public Validation<String, Void> changePassword(String oldPassword, String newPassword) {
        UserV2 user = userRepository.findById(sessionData.getCurrentUser().getId()).get();
        if (BCrypt.checkpw(oldPassword, user.getPassword())) {
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_SALT_ROUNDS)));
            return Validation.success(null);
        } else {
            return Validation.fail("Old password doesn't match.");
        }
    }

    @Transactional
    public List<RoleV2> loadAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public List<RoleV2> loadRoles(Set<UUID> ids) {
        return roleRepository.findAllById(ids);
    }

    @Transactional
    public UserV2 loadUserById(UUID id) {
        return doAsAdmin(() -> userRepository.findById(id).get());
    }

    private <T> T doAsAdmin(Supplier<T> supplier) {
        return daoUtils.doAsAdmin(supplier);
    }

    private <T> void doAsAdminV(Runnable runnable) {
        daoUtils.doAsAdminV(runnable);
    }
}
