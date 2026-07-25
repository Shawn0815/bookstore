package com.shawnyu.springbootmall.service.impl;

import com.shawnyu.springbootmall.dao.UserDao;
import com.shawnyu.springbootmall.dto.UserLoginRequest;
import com.shawnyu.springbootmall.dto.UserRegisterRequest;
import com.shawnyu.springbootmall.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // BCrypt 雜湊固定 60 字元、開頭 $2a$/$2b$/$2y$；這裡只是格式相符的假資料，不是真的雜湊值
    private static final String FAKE_BCRYPT_HASH = "$2a$10$abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ012345";

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserLoginRequest loginRequest(String email, String password) {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    @Test
    void register_emailAlreadyExists_throws400AndSkipsCreate() {
        when(userDao.getUserByEmail("a@example.com")).thenReturn(new User());
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("a@example.com");
        request.setPassword("Test1234");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userDao, never()).createUser(any());
    }

    @Test
    void register_success_passesBcryptEncodedPasswordToDao() {
        when(userDao.getUserByEmail("a@example.com")).thenReturn(null);
        when(passwordEncoder.encode("Test1234")).thenReturn(FAKE_BCRYPT_HASH);
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("a@example.com");
        request.setPassword("Test1234");

        userService.register(request);

        assertEquals(FAKE_BCRYPT_HASH, request.getPassword()); // 傳給 Dao 前已經被改寫成雜湊值
        verify(userDao).createUser(request);
    }

    @Test
    void login_emailNotFound_throws400() {
        when(userDao.getUserByEmail("nobody@example.com")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.login(loginRequest("nobody@example.com", "Test1234")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void login_bcryptPasswordMatches_returnsUserWithoutMigration() {
        User user = new User();
        user.setUserId(1);
        user.setPassword(FAKE_BCRYPT_HASH);
        when(userDao.getUserByEmail("a@example.com")).thenReturn(user);
        when(passwordEncoder.matches("Test1234", FAKE_BCRYPT_HASH)).thenReturn(true);

        User result = userService.login(loginRequest("a@example.com", "Test1234"));

        assertEquals(user, result);
        verify(userDao, never()).updatePassword(any(), anyString());
    }

    @Test
    void login_bcryptPasswordWrong_throws400() {
        User user = new User();
        user.setUserId(1);
        user.setPassword(FAKE_BCRYPT_HASH);
        when(userDao.getUserByEmail("a@example.com")).thenReturn(user);
        when(passwordEncoder.matches("WrongPass1", FAKE_BCRYPT_HASH)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.login(loginRequest("a@example.com", "WrongPass1")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userDao, never()).updatePassword(any(), anyString());
    }

    @Test
    void login_legacyMd5PasswordMatches_migratesPasswordToBcrypt() {
        String rawPassword = "Legacy1234";
        String md5Hash = DigestUtils.md5DigestAsHex(rawPassword.getBytes()); // 32 字元舊格式
        User user = new User();
        user.setUserId(5);
        user.setPassword(md5Hash);
        when(userDao.getUserByEmail("legacy@example.com")).thenReturn(user);
        when(passwordEncoder.encode(rawPassword)).thenReturn(FAKE_BCRYPT_HASH);

        User result = userService.login(loginRequest("legacy@example.com", rawPassword));

        assertEquals(user, result);
        verify(userDao, times(1)).updatePassword(5, FAKE_BCRYPT_HASH); // 無痛升級恰好觸發一次
    }

    @Test
    void login_legacyMd5PasswordWrong_throws400AndSkipsMigration() {
        String md5Hash = DigestUtils.md5DigestAsHex("Legacy1234".getBytes());
        User user = new User();
        user.setUserId(5);
        user.setPassword(md5Hash);
        when(userDao.getUserByEmail("legacy@example.com")).thenReturn(user);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.login(loginRequest("legacy@example.com", "WrongPass1")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userDao, never()).updatePassword(any(), anyString());
    }
}
