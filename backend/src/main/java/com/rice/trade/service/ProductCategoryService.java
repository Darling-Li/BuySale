package com.rice.trade.service;

import com.rice.trade.dto.ProductCategoryRequest;
import com.rice.trade.dto.ProductCategoryResponse;
import com.rice.trade.dto.ProductTypeOption;
import com.rice.trade.entity.ProductCategory;
import com.rice.trade.exception.BusinessException;
import com.rice.trade.exception.ResourceNotFoundException;
import com.rice.trade.mapper.ProductCategoryMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;

    public ProductCategoryService(ProductCategoryMapper productCategoryMapper) {
        this.productCategoryMapper = productCategoryMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> listAll() {
        return productCategoryMapper.listAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductTypeOption> enabledOptions() {
        return productCategoryMapper.listEnabled().stream()
                .map(category -> new ProductTypeOption(category.getCode(), category.getName()))
                .toList();
    }

    @Transactional
    public ProductCategoryResponse create(ProductCategoryRequest request) {
        ProductCategory category = new ProductCategory();
        apply(category, request);
        assertCodeAvailable(category.getCode(), null);
        productCategoryMapper.insert(category);
        return toResponse(productCategoryMapper.findById(category.getId()));
    }

    @Transactional
    public ProductCategoryResponse update(Long id, ProductCategoryRequest request) {
        ProductCategory category = productCategoryMapper.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("商品种类不存在：" + id);
        }
        if (!category.getCode().equals(normalizeCode(request.code()))) {
            throw new BusinessException("商品种类编码创建后不能修改");
        }
        apply(category, request);
        assertCodeAvailable(category.getCode(), id);
        productCategoryMapper.update(category);
        return toResponse(productCategoryMapper.findById(id));
    }

    @Transactional(readOnly = true)
    public String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        ProductCategory category = productCategoryMapper.findByCode(normalizeCode(code));
        return category == null ? code : category.getName();
    }

    @Transactional(readOnly = true)
    public String requireEnabledCode(String code) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode.isBlank()) {
            throw new BusinessException("商品类型不能为空");
        }
        ProductCategory category = productCategoryMapper.findByCode(normalizedCode);
        if (category == null || !category.isEnabled()) {
            throw new BusinessException("商品种类不存在或已禁用：" + normalizedCode);
        }
        return normalizedCode;
    }

    private void apply(ProductCategory category, ProductCategoryRequest request) {
        category.setCode(normalizeCode(request.code()));
        category.setName(request.name().trim());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setEnabled(request.enabled() == null || request.enabled());
        category.setRemark(trim(request.remark()));
    }

    private void assertCodeAvailable(String code, Long currentId) {
        ProductCategory existing = productCategoryMapper.findByCode(code);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException("商品种类编码已存在：" + code);
        }
    }

    private ProductCategoryResponse toResponse(ProductCategory category) {
        return new ProductCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getSortOrder(),
                category.isEnabled(),
                category.getRemark(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
