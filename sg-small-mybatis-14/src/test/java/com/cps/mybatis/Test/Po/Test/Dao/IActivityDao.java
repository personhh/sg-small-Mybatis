package com.cps.mybatis.Test.Po.Test.Dao;


import com.cps.mybatis.Test.Po.Test.Po.Activity;

public interface IActivityDao {
    Activity queryActivityById(Long activityId);
    Integer insert(Activity activity);
}