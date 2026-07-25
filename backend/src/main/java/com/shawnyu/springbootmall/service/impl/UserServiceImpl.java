package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.dao.UserDao;
import com.shawnyu.springbootmall.dto.UserLoginRequest;
import com.shawnyu.springbootmall.dto.UserRegisterRequest;
import com.shawnyu.springbootmall.model.User;
import com.shawnyu.springbootmall.service.UserService;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserServiceImpl implements UserService {

    // 創建一個 Log 出來（括號內填入這個 class 的名稱）
    private final static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User getUserbyId(Integer userId) {
        return userDao.getUserbyId(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    @Override
    public Integer register(UserRegisterRequest userRegisterRequest) {

        // 1. 檢查帳號是否已被註冊
        User user = userDao.getUserByEmail(userRegisterRequest.getEmail());

        if (user != null) {
            log.warn("該 email {} 已經被註冊", userRegisterRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "該帳號已經被註冊");
        }

        // 使用 BCrypt 生成密碼的雜湊值（自動處理 salt，不用額外存欄位）
        String hashedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());
        userRegisterRequest.setPassword(hashedPassword);

        // 創建帳號
        return userDao.createUser(userRegisterRequest);
    }

    @Override
    public User login(UserLoginRequest userLoginRequest) {
        User user = userDao.getUserByEmail(userLoginRequest.getEmail());

        // 1. 檢查帳號是否存在
        if (user == null) {
            log.warn("該 email {} 尚未註冊", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "帳號不存在");
        }

        String rawPassword = userLoginRequest.getPassword();
        String storedPassword = user.getPassword();

        // 2. 檢查密碼是否正確：相容舊帳號的 MD5 雜湊，並在登入成功時無痛升級成 BCrypt
        boolean passwordMatched;
        if (isLegacyMd5Hash(storedPassword)) {
            String hashedPassword = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
            passwordMatched = storedPassword.equals(hashedPassword);

            if (passwordMatched) {
                userDao.updatePassword(user.getUserId(), passwordEncoder.encode(rawPassword));
            }
        } else {
            passwordMatched = passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!passwordMatched) {
            log.warn("email {} 的密碼不正確", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密碼錯誤");
        }

        return user;
    }

    // MD5 雜湊固定是 32 個十六進位字元；BCrypt 雜湊固定 60 個字元、開頭是 $2a$/$2b$/$2y$
    private boolean isLegacyMd5Hash(String storedPassword) {
        return storedPassword != null && storedPassword.length() == 32;
    }
}
