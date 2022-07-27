package com.octv.im.elasticSearch;


import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

@RestController
public class TestUserController {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 保存覆盖数据
     *
     * @return
     */
    @GetMapping("/save")
    public Object save() {
        TestUser testUser = new TestUser();
        testUser.setId("1");
        testUser.setName("张三");
        testUser.setAge(18);
        testUser.setSex("男");
        testUser.setPhone("15812341234");

        //indexName默认取@Document中的indexName
        elasticsearchRestTemplate.save(testUser);

        //indexName动态指定--索引如果不存在save会自动创建后在存入数据
        elasticsearchRestTemplate.save(testUser, IndexCoordinates.of("test_user_info"));

        elasticsearchRestTemplate.save(testUser, IndexCoordinates.of("test_user_info_dd"));

        return "ok";
    }


    /**
     * 查询
     *
     * @return
     */
    @GetMapping("/search")
    public Object search() {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(queryBuilder);
        NativeSearchQuery build = builder.build();

        SearchHits<TestUser> test_user = elasticsearchRestTemplate.search(build, TestUser.class);

        //indexName动态指定
        SearchHits<TestUser> test_user_info = elasticsearchRestTemplate.search(build, TestUser.class, IndexCoordinates.of("test_user_info"));

        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("test_user", test_user.getSearchHits());
        objectObjectHashMap.put("test_user_info", test_user_info.getSearchHits());
        return objectObjectHashMap;
    }


    /**
     * 删除
     *
     * @return
     */
    @GetMapping("/delete")
    public Object delete() {
        //根据ID删除-indexName默认取@Document中的indexName
        elasticsearchRestTemplate.delete("1", TestUser.class);

        //根据ID删除-indexName动态指定
        elasticsearchRestTemplate.delete("1", IndexCoordinates.of("test_user_info"));

        //自定义条件、自定义索引
//        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
//        queryBuilder.filter(QueryBuilders.matchQuery("name", "张三"));
//        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
//        builder.withQuery(queryBuilder);
//        NativeSearchQuery build = builder.build();
//        elasticsearchRestTemplate.delete(build, TestUser.class, IndexCoordinates.of("test_user_info"));

        return "ok";
    }


    public void searchAndSort() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery("title", "手机"));
        // 排序
        queryBuilder.withSorts(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        // 搜索，获取结果

    }
}
