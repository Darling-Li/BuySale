package com.rice.trade.mapper;

import com.rice.trade.entity.SaleSettlement;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SaleSettlementMapper {

    @Select("""
            select * from sale_settlements
            where sale_order_id = #{saleOrderId}
            order by settled_at desc, id desc
            """)
    List<SaleSettlement> findBySaleOrderId(@Param("saleOrderId") Long saleOrderId);

    @Select("""
            select coalesce(sum(amount), 0)
            from sale_settlements
            where sale_order_id = #{saleOrderId}
            """)
    BigDecimal sumAmountBySaleOrderId(@Param("saleOrderId") Long saleOrderId);

    @Insert("""
            insert into sale_settlements (
                sale_order_id, amount, channel, settled_at, remark, created_at, updated_at
            )
            values (
                #{saleOrderId}, #{amount}, #{channel}, #{settledAt}, #{remark}, now(), now()
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SaleSettlement settlement);
}
