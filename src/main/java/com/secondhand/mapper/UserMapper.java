package com.secondhand.mapper;

import com.secondhand.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    User findById(@Param("userId") Long userId);
    User findByUsername(@Param("username") String username);
    User findByPhone(@Param("phone") String phone);
    User findByEmail(@Param("email") String email);
    List<User> findAll();
    int insert(User user);
    int update(User user);
    int deleteById(@Param("userId") Long userId);
    int countByUsername(@Param("username") String username);
    int countByPhone(@Param("phone") String phone);
    List<User> findPage(@Param("keyword") String keyword,
                        @Param("role") String role,
                        @Param("offset") int offset,
                        @Param("limit") int limit);
    int countPage(@Param("keyword") String keyword, @Param("role") String role);
}