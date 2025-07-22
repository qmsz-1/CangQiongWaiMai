package com.sky.mapper;


import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户信息
     * @param openid
     * @return
     */
    User getByOpenid(String openid);

    /**
     * 插入用户信息
     * @param user
     */
    @AutoFill(OperationType.INSERT)
    void insert(User user);


}


