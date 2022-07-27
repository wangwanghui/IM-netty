/*
package com.octv.im.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.time.Duration;

*/
/**
 * @author : wh
 * @date : 2020/7/6 18:46
 *//*

@Configuration
public class ElasticConfig extends AbstractElasticsearchConfiguration {

    @Value("${elasticSearch.host.port}")
    private String hostAndPort;
    @Value("${elasticSearch.user}")
    private String user;
    @Value("${elasticSearch.password}")
    private String password;
    @Value("${elasticSearch.socketTimeout}")
    private long socketTimeout;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .withBasicAuth(user, password)
                .withSocketTimeout(Duration.ofSeconds(socketTimeout))
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchRestTemplate restTemplate() throws Exception {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }


}*/
