package com.mycompany.wheretogo.service;

import com.mycompany.wheretogo.model.User;
import com.mycompany.wheretogo.util.exception.NotFoundException;

import java.util.List;

public interface UserService {
    User add(User user);

    User get(Integer id) throws NotFoundException;

    User getByEmail(String email) throws NotFoundException;

    void update(User user);

    void delete(int id) throws NotFoundException;

    List<User> getAll();
}