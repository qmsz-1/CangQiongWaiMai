package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    /**
     * 新增地址
     * @param addressBook
     */
    void addAddress(AddressBook addressBook);

    /**
     * 查询当前登录用户的地址列表
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void updateDefaultAddress(AddressBook addressBook);

    /**
     * 查询默认地址
     * @param addressBook
     * @return
     */
    List<AddressBook> getDefaultAddress(AddressBook addressBook);
}
