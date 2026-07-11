package com.enterprise.marketplace.searchservice.repository;

import com.enterprise.marketplace.searchservice.document.ProductSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDocument, String> {}
