package com.shawnyu.springbootmall.service;

import com.shawnyu.springbootmall.dto.UserLoginRequest;
import com.shawnyu.springbootmall.dto.UserRegisterRequest;
import com.shawnyu.springbootmall.model.User;

public interface UserService {

    User getUserbyId(Integer userId);

    User getUserByEmail(String email);

    Integer register(UserRegisterRequest userRegisterRequest);

    User login(UserLoginRequest userLoginRequest);
}
