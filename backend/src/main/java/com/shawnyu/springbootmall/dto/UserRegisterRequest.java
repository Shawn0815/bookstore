package com.shawnyu.springbootmall.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegisterRequest {

    @NotBlank(message = "名字不可為空白")
    private String name;

    @NotBlank(message = "帳號不可為空白")
    @Email(message = "帳號請填正確的 email 格式")
    private String email;

    @NotBlank(message = "密碼不可為空白")
    @Size(min = 8, message = "密碼至少 8 碼")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "密碼必須包含大小寫字母與數字")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
