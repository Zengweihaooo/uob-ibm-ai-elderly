package com.example.demo.repository;

import com.example.demo.pojo.User;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 定义统一的用户数据访问方法，支持多种数据库实现
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
public interface UserRepository {
    
    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象，如果不存在返回Optional.empty()
     */
    Optional<User> findById(Long id);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 用户对象，如果不存在返回Optional.empty()
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 保存用户
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    User save(User user);
    
    /**
     * 更新用户
     * @param user 用户对象
     * @return 更新后的用户对象
     */
    User update(User user);
    
    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteById(Long id);
    
    /**
     * 获取所有用户
     * @return 所有用户列表
     */
    List<User> findAll();
    
    /**
     * 根据状态查找用户
     * @param status 用户状态
     * @return 符合条件的用户列表
     */
    List<User> findByStatus(String status);
    
    /**
     * 根据角色查找用户
     * @param role 用户角色
     * @return 符合条件的用户列表
     */
    List<User> findByRole(String role);
    
    /**
     * 统计用户总数
     * @return 用户总数
     */
    long count();
}
