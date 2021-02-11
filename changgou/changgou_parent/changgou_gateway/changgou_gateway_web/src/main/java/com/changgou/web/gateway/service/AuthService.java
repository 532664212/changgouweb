package com.changgou.web.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    public String getJtiFromCookie(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst("uid");
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    public String getJwtFromRedis(String jti) {
        String token = stringRedisTemplate.boundValueOps(jti).get();
//        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTYxMjgzMjkxMiwiYXV0aG9yaXRpZXMiOlsic2Vja2lsbF9saXN0IiwiZ29vZHNfbGlzdCJdLCJqdGkiOiJjODFiNjUzZC03OTk3LTRhMWItYjNjZi0xYWUwZmQ0OTM0NGUiLCJjbGllbnRfaWQiOiJjaGFuZ2dvdSIsInVzZXJuYW1lIjoiaGVpbWEifQ.kOGrpzUd52b8e1xam6oWabAMZp6mukgqi6-GNHfB6u9iYTKeLZ65TJmjqdowk-sHpvp8bCH36lM4mXHflBnsdY_jfU6jgvGG4jP2DIUQzSg5gfZqS7YfiG6-UWKz2OBxh48LXYhuZz1GHHNxgNAb98__B4DB8N9G6cGJqbc6Lmh9Oqgq6al8_85ctUOVMvI_6I7EhPAuAO9WnKQXHerJzJqHkZxrM6CfQLHn4YmoHMncNRNXQupw7DTbsRv-tYjknmCyNudQucumhoTf8dVC_wtjcIGOdjGE1Wp-Iz5K2bWK3SBqldlfV8dHUfaFvUmkBOz3TtB8-LpQA5y-1F9f3w";
        return token;
    }
}
