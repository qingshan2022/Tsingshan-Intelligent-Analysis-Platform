package com.qingshan.qsbi.service;

import com.qingshan.qsbi.common.BaseResponse;

import java.text.ParseException;

/**
 * @author adorabled4
 * @className LoginAdapter
 * @date : 2023/11/08/ 20:56
 **/
public interface Login3rdTarget {


    BaseResponse loginByGitee(String state, String code ) throws ParseException;

    BaseResponse loginByGithub(String state, String code) throws ParseException;
}
