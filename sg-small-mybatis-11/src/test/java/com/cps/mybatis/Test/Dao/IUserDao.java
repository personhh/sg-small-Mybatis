package com.cps.mybatis.Test.Dao;

import com.cps.mybatis.Test.Po.User;

import java.util.List;

public interface IUserDao {
    User queryUserInfoById(Long id);
    User queryUserInfo(User req);

    List<User> queryUserInfoList();

    int updateUserInfo(User req);

    void insertUserInfo(User req);

    int deleteUserInfoByUserId(String userId);
}
