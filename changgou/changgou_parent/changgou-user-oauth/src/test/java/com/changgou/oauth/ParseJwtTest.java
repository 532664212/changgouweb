package com.changgou.oauth;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {

    /***
     * 校验令牌
     */

    @Test
    public void testParseToken(){
        String token="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTYxMjg3OTkwMywiYXV0aG9yaXRpZXMiOlsic2Vja2lsbF9saXN0IiwiZ29vZHNfbGlzdCJdLCJqdGkiOiIxNjVlYWJiYS0wN2IwLTRmZTAtOGFiZC03YTAwYjg3OWNlNjYiLCJjbGllbnRfaWQiOiJjaGFuZ2dvdSIsInVzZXJuYW1lIjoiaGVpbWEifQ.tXeObv5Eei8-VJixSAMumxjk6YxFzjujhlhLSt5GsP9ip5Yy3PCOv8gEZpENB2380J8_jAn6blNtVPFB1IZlXjerDHjaDQzxSgreqKOyQgXeVXWXkEOnDz2kc3snBxe-WPMb929hAMXRHwLFgRJEkhLkm8Itna-WLa7DZpB_zkWOUN14WyD0QEp-yyLKrxSdc5ZwbyIRAgd2MiX4XAwOlec0IIB2MRQlkql_oqO4lKtlu7CK4lFrvE-6plZ3UbeSIdBgBz8JHz32vrRDOW8UUt6z0ER3M-OVUXzx1w8aHzxtm8onYIfIZ0p1hli4c6b9SyQr35ttK5QcUx1TPtHxmg";
        String publicKey="-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFsEiaLvij9C1Mz+oyAmt47whAaRkRu/8kePM+X8760UGU0RMwGti6Z9y3LQ0RvK6I0brXmbGB/RsN38PVnhcP8ZfxGUH26kX0RK+tlrxcrG+HkPYOH4XPAL8Q1lu1n9x3tLcIPxq8ZZtuIyKYEmoLKyMsvTviG5flTpDprT25unWgE4md1kthRWXOnfWHATVY7Y/r4obiOL1mS5bEa/iNKotQNnvIAKtjBM4RlIDWMa6dmz+lHtLtqDD2LF1qwoiSIHI75LQZ/CNYaHCfZSxtOydpNKq8eb1/PGiLNolD4La2zf0/1dlcr5mkesV570NxRmU1tFm8Zd3MZlZmyv9QIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));
        //获取Jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
