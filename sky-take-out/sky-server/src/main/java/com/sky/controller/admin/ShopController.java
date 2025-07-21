package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    public static final String key = "SHOP_STATUS";
    private static final Integer DEFAULT_STATUS = 1;

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    /**
     * 设置营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        if(status == null || (status != 1 && status != 0)){
            return Result.error(MessageConstant.ILLEGAL_STATUS_VALUE);
        }
        log.info("设置营业状态: {}", status == 1? "营业中":"打样中");
        redisTemplate.opsForValue().set(key, status);
        return Result.success();
    }

    /**
     * 获取营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
//        if(status == null){
//            status = DEFAULT_STATUS;
//            redisTemplate.opsForValue().set(key, status);
//            log.info("使用默认营业状态: {}", status == 1?"营业中":"打样中");
//        }
        log.info("获取店铺状态: {}", status == 1?"营业中":"打样中");
        return Result.success(status);
    }

}
