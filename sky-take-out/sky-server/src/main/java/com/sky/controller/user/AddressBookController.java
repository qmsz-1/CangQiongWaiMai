package com.sky.controller.user;


import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "用户地址管理接口")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 添加地址
     * @param addressBook
     * @return
     */
    @PostMapping("")
    @ApiOperation("添加地址")
    public Result add(@RequestBody AddressBook addressBook) {
        log.info("添加地址: {}", addressBook);
        addressBookService.addAddress(addressBook);
        return Result.success();
    }

    /**
     * 查询当前登录用户的地址列表
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的地址列表")
    public Result<List<AddressBook>> list() {
        log.info("查询地址列表");
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);
        return Result.success(list);
    }

    /**
     * 更新默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("更新默认地址")
    public Result updateDefaultAddress(@RequestBody AddressBook addressBook) {
        log.info("更新默认地址: {}", addressBook);
        addressBookService.updateDefaultAddress(addressBook);
        return Result.success();
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefaultAddress() {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(1);

        List<AddressBook> list = addressBookService.getDefaultAddress(addressBook);

        if (list != null && !list.isEmpty()) {
            log.info("查询到默认地址: {}", list.get(0));
            return Result.success(list.get(0));
        }

        return Result.success();
    }


}
