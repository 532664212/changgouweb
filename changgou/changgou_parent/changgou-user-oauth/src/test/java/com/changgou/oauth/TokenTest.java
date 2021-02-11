package com.changgou.oauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TokenTest {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Test
    public void testCreateToken() {
        ServiceInstance serviceInstance = loadBalancerClient.choose("USER-AUTH");
        URI uri = serviceInstance.getUri();
        String url = uri + "/oauth/token";
        //1、header信息，包括了http basic认证信息
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String httpbasic = getHttpBasic(clientId, clientSecret);
        headers.add("Authorization", httpbasic);
        //2、指定认证类型、账号、密码
        MultiValueMap body = new LinkedMultiValueMap();
        body.add("grant_type", "password");
        body.add("username", "itheima");
        body.add("password", "itheima");
        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        ResponseErrorHandler responseErrorHandler = new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常

                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        };
        restTemplate.setErrorHandler(responseErrorHandler);

        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
        //远程调用申请令牌
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
        Map mapBody = exchange.getBody();
        for (Object value : mapBody.values()) {
            System.out.println(value);
        }
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId + ":" + clientSecret;
        byte[] decode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(decode);
    }
}
