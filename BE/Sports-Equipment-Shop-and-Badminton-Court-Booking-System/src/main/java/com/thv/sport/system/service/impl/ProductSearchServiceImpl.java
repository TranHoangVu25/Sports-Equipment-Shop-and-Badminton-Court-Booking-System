package com.thv.sport.system.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.thv.sport.system.dto.CountItemDTO;
import com.thv.sport.system.dto.SearchResultDTO;
import com.thv.sport.system.dto.response.product.ProductDocument;
import com.thv.sport.system.mapper.ProductSearchMapper;
import com.thv.sport.system.model.Product;
import com.thv.sport.system.respository.ProductRepository;
import com.thv.sport.system.respository.ProductSearchRepository;
import com.thv.sport.system.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ProductSearchMapper productSearchMapper;
    private final ElasticsearchClient elasticClient;
    @Override
    public ProductDocument syncProductToElasticsearch(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductDocument document = productSearchMapper.toDocument(product);
        return productSearchRepository.save(document);    }

    @Override
    public Iterable<ProductDocument> syncAllProducts() {
        List<Product> products = productRepository.findAll();

        List<ProductDocument> documents = products.stream()
                .map(productSearchMapper::toDocument)
                .toList();

        return productSearchRepository.saveAll(documents);
    }

    @Override
    public List<ProductDocument> searchByName(String keyword) {
        return List.of();
    }

    @Override
    public void deleteProductFromElasticsearch(Long productId) {

    }

    @Override
    public SearchResultDTO searchProducts(
            String name,
            List<String> mainCategory,
            List<String> subCategory,
            Double minPrice,
            Double maxPrice,
            String status,
            int page,
            int limit,
            String sortBy,
            String sortOrder
    ) throws IOException {

        int from = (page - 1) * limit;

        // Tạo truy vấn fuzzy cho name
        Query byNameQuery = null;
        if (name != null && !name.isEmpty()) {
            byNameQuery = MatchQuery.of(m -> m
                    .field("name")
                    .query(name)
                    .fuzziness("2")
            )._toQuery();
        }

        // Tạo danh sách filter
        List<Query> filters = new ArrayList<>();

        if (mainCategory != null && !mainCategory.isEmpty()) {
            filters.add(TermsQuery.of(t -> t
                    .field("mainCategory.keyword")
                    .terms(v -> v.value(mainCategory.stream().map(FieldValue::of).toList()))
            )._toQuery());
        }

        if (subCategory != null && !subCategory.isEmpty()) {
            filters.add(TermsQuery.of(t -> t
                    .field("subCategory.keyword")
                    .terms(v -> v.value(subCategory.stream().map(FieldValue::of).toList()))
            )._toQuery());
        }

        if (minPrice != null || maxPrice != null) {
            RangeQuery.Builder range = new RangeQuery.Builder().field("price");
            if (minPrice != null) range.gte(JsonData.of(minPrice));
            if (maxPrice != null) range.lte(JsonData.of(maxPrice));
            filters.add(range.build()._toQuery());
        }

        if (status != null && !status.isEmpty()) {
            // Sửa lại: Dùng TermQuery trên trường "status" (vì nó là keyword gốc)
            filters.add(TermQuery.of(t -> t
                    .field("status.keyword")
                    .value(status)
                    .caseInsensitive(true)
            )._toQuery());
        }

        // Kết hợp tất cả điều kiện
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        if (byNameQuery != null) boolBuilder.must(byNameQuery);
        if (!filters.isEmpty()) boolBuilder.filter(filters);

        // Sắp xếp
//        List<SortOptions> sortOptions = new ArrayList<>();
//        if (sortBy != null && !sortBy.isEmpty()) {
//            sortOptions.add(SortOptions.of(s -> s
//                    .field(f -> f
//                            .field(sortBy)
//                            .order("desc".equalsIgnoreCase(sortOrder) ? SortOrder.Desc : SortOrder.Asc)
//                    )
//            ));
//        } else {
//            sortOptions.add(SortOptions.of(s -> s
//                    .field(f -> f.field("id").order(SortOrder.Desc))
//            ));
//        }

// Nếu có tìm theo name => ưu tiên sắp theo score
        List<SortOptions> sortOptions = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            sortOptions.add(SortOptions.of(s -> s
                    .score(sc -> sc.order(SortOrder.Desc))
            ));
        } else if (sortBy != null && !sortBy.isEmpty()) {
            sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f
                            .field(sortBy)
                            .order("desc".equalsIgnoreCase(sortOrder) ? SortOrder.Desc : SortOrder.Asc)
                    )
            ));
        } else {
            sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f.field("productId").order(SortOrder.Desc))
            ));
        }

        // Gửi truy vấn Elasticsearch
        SearchResponse<ProductDocument> response = elasticClient.search(s -> s
                        .index("product_service")
                        .query(boolBuilder.build()._toQuery())
                        .from(from)
                        .size(limit)
                        .sort(sortOptions)
                        .aggregations("categories_count", a -> a.terms(t -> t.field("mainCategory.keyword")))
                        .aggregations("characters_count", a -> a.terms(t -> t.field("subCategory.keyword")))
                        .aggregations("status_count", a -> a.terms(t -> t.field("status.keyword"))), // <--- SỬA LẠI 2: Bỏ
                ProductDocument.class
        );

        // Lấy kết quả
        List<ProductDocument> results = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

        long total = response.hits().total() != null
                ? response.hits().total().value()
                : results.size();

        // Xử lý Aggregation (nếu có)
        List<CountItemDTO> categoriesCount = new ArrayList<>();
        List<CountItemDTO> charactersCount = new ArrayList<>();
        List<CountItemDTO> statusCount = new ArrayList<>();

        if (response.aggregations().get("categories_count") != null) {
            categoriesCount = response.aggregations().get("categories_count").sterms().buckets().array().stream()
                    .map(b -> new CountItemDTO(b.key().stringValue(), b.docCount()))
                    .toList();
        }

        if (response.aggregations().get("characters_count") != null) {
            charactersCount = response.aggregations().get("characters_count").sterms().buckets().array().stream()
                    .map(b -> new CountItemDTO(b.key().stringValue(), b.docCount()))
                    .toList();
        }

        if (response.aggregations().get("status_count") != null) {
            statusCount = response.aggregations().get("status_count").sterms().buckets().array().stream()
                    .map(b -> new CountItemDTO(b.key().stringValue(), b.docCount()))
                    .toList();
        }
        return new SearchResultDTO(total, page, limit, status, results, charactersCount, categoriesCount, statusCount);
    }
}
