package com.cps.mybatis.Test.Dao;


import com.cps.mybatis.Test.Po.Activity;

public interface IActivityDao {
    Activity queryActivityById(Long activityId);
}