# 数据库说明

后端启用了 Flyway，启动时会自动执行：

```text
backend/src/main/resources/db/migration/V1__init_schema.sql
```

核心库存规则：

- 采购保存后，会生成一条采购单、一条入库流水，并增加库存。
- 销售保存前，会校验库存是否足够；保存后生成销售单、一条出库流水，并扣减库存。
- 库存按 `仓库 + 商品类型 + 商品名称` 汇总。
- 商品类型包括：`RICE` 稻谷、`SEED` 稻种、`FERTILIZER` 化肥。

