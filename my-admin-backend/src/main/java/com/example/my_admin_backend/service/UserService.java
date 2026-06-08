package com.example.my_admin_backend.service;

import com.example.my_admin_backend.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO createUser(UserDTO dto);
    UserDTO updateUser(Long id, UserDTO dto);
    void deleteUser(Long id, Long currentUserId);
    void resetPassword(Long id, String newPassword);
}