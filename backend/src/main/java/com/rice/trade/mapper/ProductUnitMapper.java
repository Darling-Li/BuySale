package com.rice.trade.mapper;

import com.rice.trade.entity.ProductUnit;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductUnitMapper {

    @Select("""
            select * from product_units
            order by system_builtin desc, sort_order asc, id asc
            """)
    List<ProductUnit> listAll();

    @Select("""
            select * from product_units
            where enabled = true
            order by system_builtin desc, sort_order asc, id asc
            """)
    List<ProductUnit> listEnabled();

    @Select("select * from product_units where id = #{id}")
    ProductUnit findById(Long id);

    @Select("select * from product_units where name = #{name}")
    ProductUnit findByName(@Param("name") String name);

    @Insert("""
            insert into product_units (
                name, unit_to_jin, system_builtin, sort_order, enabled,
                remark, created_at, updated_at
            )
            values (
                #{name}, #{unitToJin}, #{systemBuiltin}, #{sortOrder}, #{enabled},
                #{remark}, now(), now()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductUnit unit);

    @Update("""
            update product_units
            set name = #{name},
                unit_to_jin = #{unitToJin},
                sort_order = #{sortOrder},
                enabled = #{enabled},
                remark = #{remark},
                updated_at = now()
            where id = #{id}
            """)
    int update(ProductUnit unit);
}
