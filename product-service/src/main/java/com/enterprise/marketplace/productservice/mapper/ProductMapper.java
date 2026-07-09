package com.enterprise.marketplace.productservice.mapper;

import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.ProductDetailResponse;
import com.enterprise.marketplace.productservice.entity.ProductAttributeEntity;
import com.enterprise.marketplace.productservice.entity.ProductDocumentEntity;
import com.enterprise.marketplace.productservice.entity.ProductEntity;
import com.enterprise.marketplace.productservice.entity.ProductImageEntity;
import com.enterprise.marketplace.productservice.entity.ProductInventoryEntity;
import com.enterprise.marketplace.productservice.entity.ProductPriceEntity;
import com.enterprise.marketplace.productservice.entity.ProductWorkflowEntity;
import com.enterprise.marketplace.productservice.enums.ProductStatus;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", implementationName = "CanonicalProductMapperImpl", imports = ProductStatus.class)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "sellerId", source = "seller.sellerId")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "description", source = "product.description")
    @Mapping(target = "categoryId", source = "product.categoryId")
    @Mapping(target = "unitOfMeasure", source = "product.unitOfMeasure", qualifiedByName = "defaultUnitOfMeasure")
    @Mapping(target = "hsnCode", source = "product.hsnCode")
    @Mapping(target = "status", source = "product.status", qualifiedByName = "defaultStatus")
    @Mapping(target = "unitPrice", source = "pricing.unitPrice")
    @Mapping(target = "currency", source = "pricing.currency", qualifiedByName = "upperCurrency")
    @Mapping(target = "minOrderQuantity", source = "pricing.minQuantity", qualifiedByName = "defaultMinQuantity")
    @Mapping(target = "gstRate", ignore = true)
    ProductEntity toEntity(CanonicalProductRequest request);

    @Mapping(target = "price", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "workflow", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "minOrderQuantity", source = "minOrderQuantity")
    ProductDetailResponse toDetailResponse(ProductEntity product);

    ProductDetailResponse.PriceDetail toPriceDetail(ProductPriceEntity price);

    ProductDetailResponse.InventoryDetail toInventoryDetail(ProductInventoryEntity inventory);

    @Mapping(target = "key", source = "attrKey")
    @Mapping(target = "value", source = "attrValue")
    ProductDetailResponse.AttributeDetail toAttributeDetail(ProductAttributeEntity attribute);

    ProductDetailResponse.ImageDetail toImageDetail(ProductImageEntity image);

    ProductDetailResponse.DocumentDetail toDocumentDetail(ProductDocumentEntity document);

    ProductDetailResponse.WorkflowDetail toWorkflowDetail(ProductWorkflowEntity workflow);

    default ProductDetailResponse toDetailResponse(
            ProductEntity product,
            ProductPriceEntity price,
            ProductInventoryEntity inventory,
            List<ProductAttributeEntity> attributes,
            List<ProductImageEntity> images,
            List<ProductDocumentEntity> documents,
            ProductWorkflowEntity workflow,
            Map<String, String> tags) {
        ProductDetailResponse base = toDetailResponse(product);
        return ProductDetailResponse.builder()
                .id(base.getId())
                .sku(base.getSku())
                .name(base.getName())
                .description(base.getDescription())
                .sellerId(base.getSellerId())
                .categoryId(base.getCategoryId())
                .unitOfMeasure(base.getUnitOfMeasure())
                .hsnCode(base.getHsnCode())
                .status(base.getStatus())
                .unitPrice(base.getUnitPrice())
                .currency(base.getCurrency())
                .minOrderQuantity(base.getMinOrderQuantity())
                .createdAt(base.getCreatedAt())
                .updatedAt(base.getUpdatedAt())
                .price(price != null ? toPriceDetail(price) : null)
                .inventory(inventory != null ? toInventoryDetail(inventory) : null)
                .attributes(attributes != null
                        ? attributes.stream().map(this::toAttributeDetail).toList()
                        : Collections.emptyList())
                .images(images != null
                        ? images.stream().map(this::toImageDetail).toList()
                        : Collections.emptyList())
                .documents(documents != null
                        ? documents.stream().map(this::toDocumentDetail).toList()
                        : Collections.emptyList())
                .workflow(workflow != null ? toWorkflowDetail(workflow) : null)
                .tags(tags)
                .build();
    }

    @Named("defaultStatus")
    default ProductStatus defaultStatus(ProductStatus status) {
        return status != null ? status : ProductStatus.DRAFT;
    }

    @Named("defaultUnitOfMeasure")
    default String defaultUnitOfMeasure(String unitOfMeasure) {
        return unitOfMeasure != null && !unitOfMeasure.isBlank() ? unitOfMeasure : "EA";
    }

    @Named("defaultMinQuantity")
    default Integer defaultMinQuantity(Integer minQuantity) {
        return minQuantity != null ? minQuantity : 1;
    }

    @Named("upperCurrency")
    default String upperCurrency(String currency) {
        return currency != null ? currency.toUpperCase() : "INR";
    }
}
