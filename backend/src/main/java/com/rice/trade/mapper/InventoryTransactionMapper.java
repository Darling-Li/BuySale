package com.rice.trade.mapper;

import com.rice.trade.entity.InventoryTransaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface InventoryTransactionMapper {

    @Insert("""
            insert into inventory_transactions (
                transaction_type, business_type, business_id,
                product_type, product_name, warehouse_id,
                weight_jin, price_per_jin, occurred_at, remark
            )
            values (
                #{transactionType}, #{businessType}, #{businessId},
                #{productType}, #{productName}, #{warehouse.id},
                #{weightJin}, #{pricePerJin}, #{occurredAt}, #{remark}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(InventoryTransaction transaction);
}

