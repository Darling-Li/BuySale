package com.rice.trade.mapper;

import com.rice.trade.entity.SaleOrder;
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
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SaleOrderMapper {

    @Select("""
            <script>
            select * from sale_orders
            where 1 = 1
            <if test="productType != null">
              and product_type = #{productType}
            </if>
            <if test="warehouseId != null">
              and warehouse_id = #{warehouseId}
            </if>
            <if test="settled != null">
              and settled = #{settled}
            </if>
            <if test="keyword != null">
              and (
                lower(product_name) like lower(concat('%', #{keyword}, '%'))
                or lower(buyer_name) like lower(concat('%', #{keyword}, '%'))
                or buyer_phone like concat('%', #{keyword}, '%')
              )
            </if>
            order by sold_at desc, id desc
            </script>
            """)
    @ResultMap("saleOrderMap")
    List<SaleOrder> search(
            @Param("productType") String productType,
            @Param("warehouseId") Long warehouseId,
            @Param("settled") Boolean settled,
            @Param("keyword") String keyword
    );

    @Select("select * from sale_orders where id = #{id}")
    @Results(id = "saleOrderMap", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "warehouse", column = "warehouse_id",
                    one = @One(select = "com.rice.trade.mapper.WarehouseMapper.findById"))
    })
    SaleOrder findById(Long id);

    @Select("""
            select * from sale_orders
            where buyer_phone = #{phone}
            order by sold_at desc, id desc
            """)
    @ResultMap("saleOrderMap")
    List<SaleOrder> findByBuyerPhone(@Param("phone") String phone);

    @Select("""
            <script>
            select * from sale_orders
            where sold_at between #{startDate} and #{endDate}
            <if test="productType != null">
              and product_type = #{productType}
            </if>
            </script>
            """)
    @ResultMap("saleOrderMap")
    List<SaleOrder> findForTrend(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("productType") String productType
    );

    @Insert("""
            insert into sale_orders (
                product_type, product_name, warehouse_id,
                buyer_name, buyer_phone, buyer_address,
                quantity, unit_name, unit_to_jin, unit_price,
                weight_jin, price_per_jin, total_amount, settled, sold_at,
                remark, created_at, updated_at
            )
            values (
                #{productType}, #{productName}, #{warehouse.id},
                #{buyerName}, #{buyerPhone}, #{buyerAddress},
                #{quantity}, #{unitName}, #{unitToJin}, #{unitPrice},
                #{weightJin}, #{pricePerJin}, #{totalAmount}, #{settled}, #{soldAt},
                #{remark}, now(), now()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SaleOrder order);

    @Update("""
            update sale_orders
            set settled = #{settled}, updated_at = now()
            where id = #{id}
            """)
    int updateSettlement(@Param("id") Long id, @Param("settled") Boolean settled);
}
