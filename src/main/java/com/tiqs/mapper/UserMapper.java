package com.tiqs.mapper;

import com.tiqs.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("SELECT id, username, password_hash AS passwordHash, role, created_at AS createdAt FROM users WHERE username=#{username}")
    User findByUsername(String username);

    @Select("SELECT id, username, password_hash AS passwordHash, role, created_at AS createdAt FROM users WHERE id=#{id}")
    User findById(Long id);

    @Insert("INSERT INTO users(username, password_hash, role, created_at) VALUES(#{username}, #{passwordHash}, #{role}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
