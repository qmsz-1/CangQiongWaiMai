package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段的赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");

        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型

        //获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }

        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType == OperationType.INSERT){
            //为4个公共字段赋值
            try {
                // 检查并调用 setCreateTime 方法
                Method setCreateTime = getMethodIfExists(entity.getClass(), AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                if (setCreateTime != null) {
                    setCreateTime.invoke(entity, now);
                }

                // 检查并调用 setCreateUser 方法
                Method setCreateUser = getMethodIfExists(entity.getClass(), AutoFillConstant.SET_CREATE_USER, Long.class);
                if (setCreateUser != null) {
                    setCreateUser.invoke(entity, currentId);
                }

                // 检查并调用 setUpdateTime 方法
                Method setUpdateTime = getMethodIfExists(entity.getClass(), AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                if (setUpdateTime != null) {
                    setUpdateTime.invoke(entity, now);
                }

                // 检查并调用 setUpdateUser 方法
                Method setUpdateUser = getMethodIfExists(entity.getClass(), AutoFillConstant.SET_UPDATE_USER, Long.class);
                if (setUpdateUser != null) {
                    setUpdateUser.invoke(entity, currentId);
                }
            } catch (Exception e) {
                log.error("自动填充字段时发生异常", e);
            }
        }else if(operationType == OperationType.UPDATE){
            //为2个公共字段赋值
            try {
                // 检查并调用 setUpdateTime 方法
                Method setUpdateTime = getMethodIfExists(entity.getClass(), AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                if (setUpdateTime != null) {
                    setUpdateTime.invoke(entity, now);
                }

                // 检查并调用 setUpdateUser 方法
                Method setUpdateUser = getMethodIfExists(entity.getClass(), AutoFillConstant.SET_UPDATE_USER, Long.class);
                if (setUpdateUser != null) {
                    setUpdateUser.invoke(entity, currentId);
                }
            } catch (Exception e) {
                log.error("自动填充更新字段时发生异常", e);
            }
        }
    }

    /**
     * 检查类中是否存在指定方法，如果存在则返回Method对象，否则返回null
     * @param clazz 类对象
     * @param methodName 方法名
     * @param parameterTypes 参数类型
     * @return Method对象或null
     */
    private Method getMethodIfExists(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            log.debug("方法 {} 不存在于类 {} 中", methodName, clazz.getSimpleName());
            return null;
        }
    }
}
