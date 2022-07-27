package com.octv.im.elasticSearch;

import com.google.common.collect.Lists;
import com.octv.im.elasticSearch.dao.BlogRepository;
import com.octv.im.elasticSearch.dao.ProductDao;
import com.octv.im.elasticSearch.enntity.BlogModel;
import com.octv.im.elasticSearch.enntity.Product;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@RestController
public class SpringDataEsProductDaoTest {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private ProductDao productDao;

    /**
     * 新增
     */
    @PostMapping("/es/add")
    public String add(@RequestBody BlogModel blogModel) {
        blogRepository.save(blogModel);
        return "success ";
    }


    @GetMapping("/es/save")
    public void save() {
        Product product = new Product();
        product.setId(11L);
        product.setTitle("in is dog");
        product.setCategory("test2");
        product.setPrice(912D);
        product.setType("in is dog");
        product.setTestName("in is dog");
        product.setImages("http2");
        elasticsearchRestTemplate.save(product);
    }
    //POSTMAN, GET http://localhost:9200/shopping/_doc/2

    //修改
    @PutMapping("/es/put")
    public void update() {
        Product product = new Product();
        product.setId(2L);
        product.setTitle("小米手机");
        product.setCategory("手机");
        product.setPrice(9999.0);
        product.setImages("http://www.test/xm.jpg");

    }
    //POSTMAN, GET http://localhost:9200/shopping/_doc/2


    //根据 id 查询
    @GetMapping("/es/id")
    public List<BlogModel> findById(@RequestParam(value = "type",required = false) String type, @RequestParam(value = "param",required = false) String param) {
//        Product product = new Product();
//        product.setId(4L);
//        product.setTitle("苹果");
//        product.setCategory("ipad");
//        product.setPrice(2399D);
//        product.setImages("http://www.apple.com");
//        productDao.save(product);
//        System.out.println("success ");
        List<BlogModel> list = Lists.newArrayList();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        RangeQueryBuilder balance = QueryBuilders.rangeQuery("age").gte(1).lte(3000);
//        boolQueryBuilder.filter(balance);

//
        if (!Objects.equals(type, "1")) {
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("title", type);
            boolQueryBuilder.must(matchQuery);
        }

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                //.withAggregations(AggregationBuilders.terms(""))
                //.withQuery(QueryBuilders.termQuery("title","手机"))
                .withQuery(boolQueryBuilder)
                .build();

        SearchHits<BlogModel> searchHits = elasticsearchRestTemplate.search(query, BlogModel.class);

        for (SearchHit<BlogModel> hit : searchHits) {
            list.add(hit.getContent());
        }
        return list;
    }

    @GetMapping("/es/test")
    public Map<Object, Object> searchAndSort(@RequestParam("type") String type) {
        // 构建查询条件
        NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("testName", type))
                .build();
        // 添加基本分词查询
        //  queryBuilder.withQuery(QueryBuilders.termQuery("title", "华为"));
        // 排序
        //  queryBuilder.withSorts(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        // 搜索，获取结果
        SearchHits<BlogModel> productSearchHits = elasticsearchRestTemplate.search(queryBuilder, BlogModel.class);
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("product", productSearchHits.getSearchHits());
        return objectObjectHashMap;
    }

    @GetMapping("/es/find")
    public Map<Object, Object> findAll() {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(queryBuilder);
        NativeSearchQuery build = builder.build();
        SearchHits<Product> productSearchHits = elasticsearchRestTemplate.search(build, Product.class);
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("product", productSearchHits.getSearchHits());
        return objectObjectHashMap;
    }

    //删除

    public void delete() {

    }
    //POSTMAN, GET http://localhost:9200/shopping/_doc/2

    //批量新增

    public void saveAll() {

    }

    //分页查询
    public void findByPageable() {
        //设置排序(排序方式，正序还是倒序，排序的 id)
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        int currentPage = 0;//当前页，第一页从 0 开始， 1 表示第二页
        int pageSize = 5;//每页显示多少条
        //设置查询分页
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, sort);
        //分页查询
        Page<Product> productPage = null;//productDao.findAll(pageRequest);
        for (Product Product : productPage.getContent()) {
            System.out.println(Product);
        }
    }
}
