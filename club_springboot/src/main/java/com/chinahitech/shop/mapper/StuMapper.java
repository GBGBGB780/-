package com.chinahitech.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chinahitech.shop.bean.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface StuMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE user_id = #{num}")
    User getByNum(String num);

    @Select("SELECT * FROM user WHERE user_name = #{name}")
    User getByName(String name);

    @Insert("INSERT INTO user(user_id, password, email, salt, create_time, modify_time, status) " +
            "VALUES (#{stuNumber}, #{password}, #{email}, #{salt}, #{createTime}, #{modifyTime}, #{status})")
    int addStudent(@Param("stuNumber") String stuNumber,
                   @Param("password") String password,
                   @Param("email") String email,
                   @Param("salt") String salt,
                   @Param("createTime") Date createTime,
                   @Param("modifyTime") Date modifyTime,
                   @Param("status") int status);

    @Update("UPDATE user SET password = #{password}, modify_time = #{modifyTime} WHERE user_id = #{stuNumber}")
    int updatePassword(@Param("stuNumber") String stuNumber,
                       @Param("password") String password,
                       @Param("modifyTime") Date modifyTime);

    @Update("UPDATE `user` SET phone = #{phone}, modify_time = #{modifyTime} WHERE user_id = #{stuNumber}")
    int updatePhone(@Param("stuNumber") String stuNumber,
                    @Param("phone") String phone,
                    @Param("modifyTime") Date modifyTime);

    @Update("UPDATE `user` SET description = #{description}, modify_time = #{modifyTime} WHERE user_id = #{stuNumber}")
    int updateDescription(@Param("stuNumber") String stuNumber,
                          @Param("description") String description,
                          @Param("modifyTime") Date modifyTime);

    @Update("UPDATE `user` SET nickname = #{nickname}, modify_time = #{modifyTime} WHERE user_id = #{stuNumber}")
    int updateNickname(@Param("stuNumber") String stuNumber,
                       @Param("nickname") String nickname,
                       @Param("modifyTime") Date modifyTime);

}
