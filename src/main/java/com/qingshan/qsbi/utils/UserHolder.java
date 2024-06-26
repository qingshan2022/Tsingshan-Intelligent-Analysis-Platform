package com.qingshan.qsbi.utils;

import com.qingshan.qsbi.model.dto.user.UserDTO;

/**
 * @author adorabled4
 * @className UserHolder
 * @date : 2023/05/04/ 16:13
 **/
public class UserHolder {
    public static ThreadLocal<UserDTO> user = new ThreadLocal<>();

    public static UserDTO getUser() {
        return user.get();
    }

    public static void setUser(UserDTO userDTO) {
        user.set(userDTO);
    }

}