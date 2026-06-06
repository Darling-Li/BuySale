package com.rice.trade.mapper;

import com.rice.trade.entity.ProductCategory;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductCategoryMapper {

    @Select("""
            select * from product_categories
            order by sort_order asc, id asc
            """)
    List<ProductCategory> listAll();

    @Select("""
            select * from product_categories
            where enabled = true
            order by sort_order asc, id asc
            """)
    List<ProductCategory> listEnabled();

    @Select("select * from product_categories where id = #{id}")
    ProductCategory findById(Long id);

    @Select("select * from product_categories where code = #{code}")
    ProductCategory findByCode(@Param("code") String code);

    @Insert("""
            insert into product_categories (
                code, name, sort_order, enabled, remark, created_at, updated_at
            )
            values (
                #{code}, #{name}, #{sortOrder}, #{enabled}, #{remark}, now(), now()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductCategory category);

    @Update("""
            update product_categories
            set code = #{code},
                name = #{name},
                sort_order = #{sortOrder},
                enabled = #{enabled},
                remark = #{remark},
                updated_at = now()
            where id = #{id}
            """)
    int update(ProductCategory category);
}
