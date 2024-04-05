package com.qingshan.qsbi.controller;

import com.qingshan.qsbi.common.BaseResponse;
import com.qingshan.qsbi.service.impl.Login3rdAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;

/**
 * @author adorabled4
 * @className LoginController
 * @date : 2023/11/08/ 21:04
 **/
@RestController
@RequestMapping(value = "/login3rd")
@Slf4j
public class LoginController {

    @Resource
    Login3rdAdapter login3rdAdapter;
    @GetMapping("/gitee/callback")
    public BaseResponse loginByGitee(String code, String state) throws ParseException {
        return login3rdAdapter.loginByGitee(state,code);
    }

    @GetMapping("/github/callback")
    public BaseResponse loginByGithub(String code,String state) throws ParseException {
        return login3rdAdapter.loginByGithub(state,code);
    }
}
