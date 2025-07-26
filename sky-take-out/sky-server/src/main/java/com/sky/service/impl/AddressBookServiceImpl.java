package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void addAddress(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0); // 默认不设置为默认地址
        addressBookMapper.insert(addressBook);
    }

    /**
     * 查询当前登录用户的地址列表
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.getByUserId(addressBook.getUserId());
    }

    /**
     * 设置默认地址
     * @param addressBook
     */
    @Override
    public void updateDefaultAddress(AddressBook addressBook) {
        // 更新默认地址时，先将其他地址的默认标志位设置为0
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateAllDefault(addressBook);

        // 更新当前地址的默认标志位为1
        addressBook.setIsDefault(1);
        addressBookMapper.updateDefaultAddress(addressBook);
    }

    /**
     * 查询默认地址
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> getDefaultAddress(AddressBook addressBook) {
        return addressBookMapper.getDefaultAddress(addressBook);
    }
}
