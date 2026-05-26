package com.rice.trade.mapper;

import com.rice.trade.entity.Warehouse;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WarehouseMapper {

    @Select("select * from warehouses order by id desc")
    List<Warehouse> findAll();

    @Select("select * from warehouses where id = #{id}")
    Warehouse findById(Long id);

    @Insert("""
            insert into warehouses (name, address, contact_name, contact_phone, remark, created_at, updated_at)
            values (#{name}, #{address}, #{contactName}, #{contactPhone}, #{remark}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Warehouse warehouse);

    @Update("""
            update warehouses
            set name = #{name},
                address = #{address},
                contact_name = #{contactName},
                contact_phone = #{contactPhone},
                remark = #{remark},
                updated_at = now()
            where id = #{id}
            """)
    int update(Warehouse warehouse);
}

