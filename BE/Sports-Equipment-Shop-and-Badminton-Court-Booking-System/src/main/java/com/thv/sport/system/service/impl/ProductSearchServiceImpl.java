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
            List<String> sizes,
            int page,
            int limit,
            String sortBy,
            String sortOrder
    ) throws IOException {

        int from = (page - 1) * limit;

        // ===== MUST =====
        List<Query> mustQueries = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            mustQueries.add(MatchQuery.of(m -> m
                    .field("name")
                    .query(name)
                    .fuzziness("2")
            )._toQuery());
        }

        // ===== FILTER =====
        List<Query> filters = new ArrayList<>();

        if (mainCategory != null && !mainCategory.isEmpty()) {
            filters.add(TermsQuery.of(t -> t
                    .field("mainCategory")
                    .terms(v -> v.value(mainCategory.stream().map(FieldValue::of).toList()))
            )._toQuery());
        }

        if (subCategory != null && !subCategory.isEmpty()) {
            filters.add(TermsQuery.of(t -> t
                    .field("subCategory")
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
            filters.add(TermQuery.of(t -> t
                    .field("status")
                    .value(status)
                    .caseInsensitive(true)
            )._toQuery());
        }

        // ===== ✅ SIZE FILTER (FIX CHUẨN) =====
        if (sizes != null && !sizes.isEmpty()) {

            List<Query> sizeShould = new ArrayList<>();

            for (String size : sizes) {
                sizeShould.add(Query.of(q -> q
                        .prefix(p -> p
                                .field("variants.size")
                                .value(size) // "3U" → match "3U5", "3U6"
                        )
                ));
            }

            Query nestedSizeQuery = Query.of(q -> q
                    .nested(n -> n
                            .path("variants")
                            .query(nq -> nq
                                    .bool(b -> b
                                            .should(sizeShould)
                                            .minimumShouldMatch("1")
                                    )
                            )
                    )
            );

            // 👉 QUAN TRỌNG: đưa vào FILTER
            filters.add(nestedSizeQuery);
        }

        // ===== BUILD BOOL =====
        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (!mustQueries.isEmpty()) bool.must(mustQueries);
        if (!filters.isEmpty()) bool.filter(filters);

        Query finalQuery = bool.build()._toQuery();

        // ===== SORT =====
        List<SortOptions> sortOptions = new ArrayList<>();

        if (sortBy != null && !sortBy.isEmpty()) {
            sortOptions.add(SortOptions.of(s -> s
                    .field(f -> f
                            .field(sortBy)
                            .order("desc".equalsIgnoreCase(sortOrder)
                                    ? SortOrder.Desc
                                    : SortOrder.Asc)
                    )
            ));
        } else {
            sortOptions.add(SortOptions.of(s -> s
                    .score(sc -> sc.order(SortOrder.Desc))
            ));
        }

        // ===== SEARCH =====
        SearchResponse<ProductDocument> response = elasticClient.search(s -> s
                        .index("product_service")
                        .query(finalQuery)
                        .from(from)
                        .size(limit)
                        .sort(sortOptions),
                ProductDocument.class
        );

        // ===== RESULT =====
        List<ProductDocument> results = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

        long total = response.hits().total() != null
                ? response.hits().total().value()
                : results.size();

        return new SearchResultDTO(
                total,
                page,
                limit,
                status,
                results,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
