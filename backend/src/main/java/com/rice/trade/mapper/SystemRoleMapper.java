package com.rice.trade.mapper;

import com.rice.trade.entity.SystemRole;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemRoleMapper {

    @Select("select * from system_roles order by id")
    List<SystemRole> findAll();

    @Select("select * from system_roles where id = #{id}")
    SystemRole findById(Long id);

    @Select("select * from system_roles where code = #{code}")
    SystemRole findByCode(String code);

    @Insert("""
            insert into system_roles (code, name, description, enabled, created_at, updated_at)
            values (#{code}, #{name}, #{description}, #{enabled}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SystemRole role);

    @Update("""
            update system_roles
            set code = #{code},
                name = #{name},
                description = #{description},
                enabled = #{enabled},
                updated_at = now()
            where id = #{id}
            """)
    int update(SystemRole role);
}
