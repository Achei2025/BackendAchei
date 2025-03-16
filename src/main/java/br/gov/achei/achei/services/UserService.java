/*
 * Achei: Stolen Object Tracking System.
 * Copyright (C) 2025  Team Achei
 * 
 * This file is part of Achei.
 * 
 * Achei is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Achei is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Achei.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact information: teamachei.2024@gmail.com
*/

package br.gov.achei.achei.services;

import br.gov.achei.achei.models.User;
import br.gov.achei.achei.repositories.UserRepository;
import br.gov.achei.achei.utils.EncryptionUtil;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String encryptedCpf, String password) {
        if (userRepository.findByUsername(encryptedCpf).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
    
        User user = new User();
        user.setUsername(encryptedCpf);
        user.setPassword(EncryptionUtil.hashPassword(password));
        user.setRoles(Set.of("ROLE_CITIZEN"));
        return userRepository.save(user);
    }        

    public Optional<User> findByUsername(String rawUsername) {
        String encryptedUsername = EncryptionUtil.encrypt(rawUsername);
        return userRepository.findByUsername(encryptedUsername);
    }        

    public void updatePassword(String username, String newPassword) {
        String encryptedUsername = EncryptionUtil.encrypt(username);
    
        User user = userRepository.findByUsername(encryptedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
        user.setPassword(EncryptionUtil.hashPassword(newPassword));
        userRepository.save(user);
    }
}
