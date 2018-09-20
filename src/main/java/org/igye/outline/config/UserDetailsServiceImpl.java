package org.igye.outline.config;


import org.igye.outline.data.UserRepository;
import org.igye.outline.modelv2.UserV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        UserV2 user = userRepository.findByName(name);
        if (user == null) {
            throw new UsernameNotFoundException(name);
        } else {
            return new UserDetailsImpl(user);
        }
    }
}
