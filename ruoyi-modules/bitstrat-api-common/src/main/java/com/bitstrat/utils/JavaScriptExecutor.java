package com.bitstrat.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/16 9:45
 * @Content
 */
@Slf4j
public class JavaScriptExecutor {
    public static void main1(String[] args) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String user = "";
        String url = "";
        String data = "";
        String refundingRate = "https://capi.coinglass.com/api/fundingRate/v2/home";

        // 1. 构建 URI 和参数
        URI uri = UriComponentsBuilder.fromHttpUrl(refundingRate)
            .queryParam("key", "123456")
            .queryParam("type", "json")
            .build()
            .encode()
            .toUri();
        URL coinGlassUrl = new URL(refundingRate);
        String path = coinGlassUrl.getPath();
        url = path;
        // 2. 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer your_token_here");
        headers.set("path", path);
        headers.set("cache-ts", System.currentTimeMillis() + "");
        headers.set("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
        // 如果需要接收 JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 3. 构建 HttpEntity，包含 header（GET 不需要 body）
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 4. 使用 exchange 方法发送请求
        ResponseEntity<String> response = restTemplate.exchange(
            uri,
            GET,
            entity,
            String.class
        );
        String body = response.getBody();
        HttpHeaders responseHeaders = response.getHeaders();
        List<String> coinGlassUser = responseHeaders.get("user");
        if(coinGlassUser != null && coinGlassUser.size() > 0) {
            user = coinGlassUser.get(0);
        }
        JSONObject resultBody = JSONObject.parseObject(body);
        if (resultBody.getInteger("code") == 0) {
            String bodyString = resultBody.getString("data");
//            log.info(bodyString);
            data = bodyString;
            //解码
            String decode = decode(user, url, data);
            JSONArray jsonObject = JSONArray.parseArray(decode);

            log.info("解码数据 {}",jsonObject.toJSONString());
            saveJsonFile("E:\\WorkSpace\\dify-extension\\utilsProject\\src\\coinGlass.json", jsonObject.toJSONString());
        }





    }
    public static void main(String[] args) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String user = "";
        String url = "";
        String data = "";
        String refundingRate = "https://capi.coinglass.com/api/fundingRate/v2/history/chart?symbol=MAGIC&type=U&interval=m5";
        // 1. 构建 URI 和参数
        URI uri = UriComponentsBuilder.fromHttpUrl(refundingRate)
//            .queryParam("key", "123456")
//            .queryParam("type", "json")
            .build()
            .encode()
            .toUri();
        URL coinGlassUrl = new URL(refundingRate);
        String path = coinGlassUrl.getPath();
        url = path;
        // 2. 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer your_token_here");
        headers.set("path", path);
        headers.set("cache-ts", System.currentTimeMillis() + "");
        headers.set("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
        // 如果需要接收 JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 3. 构建 HttpEntity，包含 header（GET 不需要 body）
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 4. 使用 exchange 方法发送请求
        ResponseEntity<String> response = restTemplate.exchange(
            uri,
            GET,
            entity,
            String.class
        );
        String body = response.getBody();
        HttpHeaders responseHeaders = response.getHeaders();
        List<String> coinGlassUser = responseHeaders.get("user");
        if(coinGlassUser != null && coinGlassUser.size() > 0) {
            user = coinGlassUser.get(0);
        }
        JSONObject resultBody = JSONObject.parseObject(body);
        if (resultBody.getInteger("code") == 0) {
            String bodyString = resultBody.getString("data");
//            log.info(bodyString);
            data = bodyString;
            //解码
            String decode = decode(user, url, data);
            JSONArray jsonObject = JSONArray.parseArray(decode);

            log.info("解码数据 {}",jsonObject.toJSONString());
            saveJsonFile("E:\\WorkSpace\\dify-extension\\utilsProject\\src\\coinGlass.json", jsonObject.toJSONString());
        }





    }

    public static void saveJsonFile(String fileName, String json) throws IOException {
        String content = json;
        String filePath = fileName;

        Files.write(
            Paths.get(filePath),
            Collections.singleton(content), // 单行写入
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }
    public static String  decode(String user,String url,String data) {
        // 使用类加载器从 resources/js 目录加载 bundle.js 文件
        try (Context context = Context.create()) {
            // 获取类加载器
            ClassLoader classLoader = JavaScriptExecutor.class.getClassLoader();
            // 获取资源路径
            InputStream jsFile = classLoader.getResourceAsStream("js/bundle.js");

            if (jsFile == null) {
                log.error("bundle.js 文件未找到");
                return null;
            }
            String jsscript = readInputStream(jsFile);
            // 在 GraalVM 上下文中加载 JS 文件
            context.eval("js", jsscript);
            Value decodeFunction = context.getBindings("js").getMember("javaDecode");

            // 调用 decode 方法
            if (decodeFunction.canExecute()) {
                //user,url, data
                String result = decodeFunction.execute(user,url,data).asString();
                log.info("invoke 结果: " + result);
                return result;
            } else {
                log.error("javaDecode 函数未找到");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // 读取 InputStream 内容并返回为 String
    private static String readInputStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder scriptBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                scriptBuilder.append(line).append("\n");
            }
            return scriptBuilder.toString();
        }
    }
}
