package com.octv.im.elasticSearch.dao;

import com.octv.im.elasticSearch.enntity.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductDao extends ElasticsearchRepository<Product, Long> {
}