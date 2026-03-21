package com.thv.sport.system.respository;

import com.thv.sport.system.dto.response.product.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
}
