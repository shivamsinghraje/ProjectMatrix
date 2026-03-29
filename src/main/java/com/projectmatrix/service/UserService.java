package com.projectmatrix.service;

import com.projectmatrix.dto.UserDTO;
import com.projectmatrix.entity.Role;
import com.projectmatrix.entity.User;
import com.projectmatrix.exception.CustomException;
import com.projectmatrix.repository.UserRepository;
import com.projectmatrix.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MapperUtil mapperUtil;
    private final ActivityService activityService;

    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new CustomException("Email already exists");
        }

        User user = mapperUtil.toUser(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setActive(true);

        User savedUser = userRepository.save(user);

        activityService.logActivity("USER_CREATED", "User", savedUser.getId(),
                "User created by admin", getCurrentUser());

        return mapperUtil.toUserDTO(savedUser);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found"));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(userDTO.getRole());
        user.setActive(userDTO.isActive());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);

        activityService.logActivity("USER_UPDATED", "User", updatedUser.getId(),
                "User updated", getCurrentUser());

        return mapperUtil.toUserDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found"));

        userRepository.delete(user);

        activityService.logActivity("USER_DELETED", "User", id,
                "User deleted", getCurrentUser());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found"));
        return mapperUtil.toUserDTO(user);
    }

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapperUtil::toUserDTO);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapperUtil::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDTO assignRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        activityService.logActivity("ROLE_ASSIGNED", "User", userId,
                "Role " + role + " assigned to user", getCurrentUser());

        return mapperUtil.toUserDTO(updatedUser);
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Current user not found"));
    }
}