package com.rice.trade.mapper;

import com.rice.trade.entity.InventoryItem;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryItemMapper {

    @Select("""
            <script>
            select * from inventory_items
            where 1 = 1
            <if test="productType != null">
              and product_type = #{productType}
            </if>
            <if test="warehouseId != null">
              and warehouse_id = #{warehouseId}
            </if>
            <if test="keyword != null">
              and lower(product_name) like lower(concat('%', #{keyword}, '%'))
            </if>
            order by product_type asc, product_name asc, warehouse_id asc
            </script>
            """)
    @ResultMap("inventoryItemMap")
    List<InventoryItem> search(
            @Param("productType") String productType,
            @Param("warehouseId") Long warehouseId,
            @Param("keyword") String keyword
    );

    @Select("select * from inventory_items where id = #{id}")
    @Results(id = "inventoryItemMap", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "warehouse", column = "warehouse_id",
                    one = @One(select = "com.rice.trade.mapper.WarehouseMapper.findById"))
    })
    InventoryItem findById(Long id);

    @Select("""
            select * from inventory_items
            where warehouse_id = #{warehouseId}
              and product_type = #{productType}
              and product_name = #{productName}
            """)
    @ResultMap("inventoryItemMap")
    InventoryItem findByScope(
            @Param("warehouseId") Long warehouseId,
            @Param("productType") String productType,
            @Param("productName") String productName
    );

    @Insert("""
            insert into inventory_items (
                warehouse_id, product_type, product_name,
                quantity_jin, average_cost_per_jin, created_at, updated_at
            )
            values (
                #{warehouse.id}, #{productType}, #{productName},
                #{quantityJin}, #{averageCostPerJin}, now(), now()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(InventoryItem item);

    @Update("""
            update inventory_items
            set quantity_jin = #{quantityJin},
                average_cost_per_jin = #{averageCostPerJin},
                updated_at = now()
            where id = #{id}
            """)
    int update(InventoryItem item);
}
