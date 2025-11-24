package com.tiqs.mapper;

import com.tiqs.entity.UserProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileMapper {
    @Select("SELECT user_id AS userId, real_name AS realName, email, phone FROM user_profile WHERE user_id=#{userId}")
    UserProfile findByUserId(Long userId);

    @Insert("INSERT INTO user_profile(user_id, real_name, email, phone) VALUES(#{userId}, #{realName}, #{email}, #{phone}) " +
            "ON DUPLICATE KEY UPDATE real_name=VALUES(real_name), email=VALUES(email), phone=VALUES(phone)")
    void upsert(UserProfile profile);
}
