package com.rice.trade.mapper;

import com.rice.trade.entity.PurchaseOrder;
import com.rice.trade.enums.ProductType;
import java.time.LocalDate;
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

@Mapper
public interface PurchaseOrderMapper {

    @Select("""
            <script>
            select * from purchase_orders
            where 1 = 1
            <if test="productType != null">
              and product_type = #{productType}
            </if>
            <if test="warehouseId != null">
              and warehouse_id = #{warehouseId}
            </if>
            <if test="keyword != null">
              and (
                lower(product_name) like lower(concat('%', #{keyword}, '%'))
                or lower(counterparty_name) like lower(concat('%', #{keyword}, '%'))
                or counterparty_phone like concat('%', #{keyword}, '%')
              )
            </if>
            order by purchased_at desc, id desc
            </script>
            """)
    @ResultMap("purchaseOrderMap")
    List<PurchaseOrder> search(
            @Param("productType") ProductType productType,
            @Param("warehouseId") Long warehouseId,
            @Param("keyword") String keyword
    );

    @Select("select * from purchase_orders where id = #{id}")
    @Results(id = "purchaseOrderMap", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "warehouse", column = "warehouse_id",
                    one = @One(select = "com.rice.trade.mapper.WarehouseMapper.findById"))
    })
    PurchaseOrder findById(Long id);

    @Select("""
            <script>
            select * from purchase_orders
            where purchased_at between #{startDate} and #{endDate}
            <if test="productType != null">
              and product_type = #{productType}
            </if>
            </script>
            """)
    @ResultMap("purchaseOrderMap")
    List<PurchaseOrder> findForTrend(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("productType") ProductType productType
    );

    @Insert("""
            insert into purchase_orders (
                product_type, product_name, warehouse_id,
                counterparty_name, counterparty_phone, counterparty_address,
                quantity, unit_name, unit_to_jin, unit_price,
                weight_jin, price_per_jin, total_amount, purchased_at,
                remark, created_at, updated_at
            )
            values (
                #{productType}, #{productName}, #{warehouse.id},
                #{counterpartyName}, #{counterpartyPhone}, #{counterpartyAddress},
                #{quantity}, #{unitName}, #{unitToJin}, #{unitPrice},
                #{weightJin}, #{pricePerJin}, #{totalAmount}, #{purchasedAt},
                #{remark}, now(), now()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PurchaseOrder order);
}
