package com.cps.mybatis.Test.Dao;

import com.cps.mybatis.Test.Po.User;

public interface IUserDao {
    User queryUserInfoById(Long uId);
}
