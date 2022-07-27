package com.octv.im.elasticSearch.dao;

import com.octv.im.elasticSearch.enntity.BlogModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BlogRepository extends ElasticsearchRepository<BlogModel, String> {

}
