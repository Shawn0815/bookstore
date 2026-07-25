package com.shawnyu.springbootmall.dao;

import com.shawnyu.springbootmall.dto.UserRegisterRequest;
import com.shawnyu.springbootmall.model.User;

public interface UserDao {

    User getUserbyId(Integer userId);

    Integer createUser(UserRegisterRequest userRegisterRequest);

    User getUserByEmail(String email);
}
