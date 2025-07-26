package com.sky.mapper;


import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    /**
     * 新增地址
     * @param addressBook
     */
    void insert(AddressBook addressBook);

    /**
     * 查询当前登录用户的地址列表
     * @param userId
     * @return
     */
    List<AddressBook> getByUserId(Long userId);

    /**
     * 将当前用户的所有地址都设置为非默认
     * @param addressBook
     */
    void updateAllDefault(AddressBook addressBook);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void updateDefaultAddress(AddressBook addressBook);

    /**
     * 查询默认
     * @param addressBook
     * @return
     */
    List<AddressBook> getDefaultAddress(AddressBook addressBook);
}
