package com.rice.trade.mapper;

import com.rice.trade.entity.SystemUser;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemUserMapper {

    @Select("select * from system_users order by id desc")
    List<SystemUser> findAll();

    @Select("select * from system_users where id = #{id}")
    SystemUser findById(Long id);

    @Select("select * from system_users where username = #{username}")
    SystemUser findByUsername(String username);

    @Select("""
            select r.code
            from system_roles r
            join system_user_roles ur on ur.role_id = r.id
            where ur.user_id = #{userId}
              and r.enabled = true
            order by r.id
            """)
    List<String> findRoleCodesByUserId(Long userId);

    @Insert("""
            insert into system_users (username, password_hash, display_name, enabled, created_at, updated_at)
            values (#{username}, #{passwordHash}, #{displayName}, #{enabled}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SystemUser user);

    @Update("""
            update system_users
            set username = #{username},
                display_name = #{displayName},
                enabled = #{enabled},
                updated_at = now()
            where id = #{id}
            """)
    int updateBasic(SystemUser user);

    @Update("""
            update system_users
            set password_hash = #{passwordHash},
                updated_at = now()
            where id = #{id}
            """)
    int updatePassword(SystemUser user);

    @Delete("delete from system_user_roles where user_id = #{userId}")
    int deleteRoles(Long userId);

    @Delete("""
            <script>
            delete from system_user_roles
            where user_id in
            <foreach collection="userIds" item="userId" open="(" close=")" separator=",">
              #{userId}
            </foreach>
            </script>
            """)
    int deleteRolesByUserIds(@Param("userIds") List<Long> userIds);

    @Insert("""
            insert into system_user_roles (user_id, role_id)
            select #{userId}, id
            from system_roles
            where code = #{roleCode}
            """)
    int insertRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);

    @Delete("""
            <script>
            delete from system_users
            where id in
            <foreach collection="ids" item="id" open="(" close=")" separator=",">
              #{id}
            </foreach>
            </script>
            """)
    int deleteByIds(@Param("ids") List<Long> ids);
}
