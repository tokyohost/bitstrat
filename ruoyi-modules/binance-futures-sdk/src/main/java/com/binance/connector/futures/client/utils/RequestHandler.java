package com.binance.connector.futures.client.utils;

import com.binance.connector.futures.client.enums.HttpMethod;
import com.binance.connector.futures.client.enums.RequestType;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.binance.connector.futures.client.timeSync.ServerTimeSource;
import com.binance.connector.futures.client.timeSync.SyncServerTime;
import lombok.experimental.Helper;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {
    public final static ScheduledExecutorService timeSync = Executors.newScheduledThreadPool(1);
    public static AtomicReference<ServerTimeSource> finalServerTimeSource = new AtomicReference<>(null);
    static SyncServerTime syncServerTime ;

    private final String apiKey;
    private final String secretKey;
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private final ProxyAuth proxy;

    public RequestHandler(String apiKey, ProxyAuth proxy) {
        this.apiKey = apiKey;
        this.secretKey = null;
        this.proxy = proxy;
    }

    public RequestHandler(String apiKey, String secretKey, ProxyAuth proxy) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.proxy = proxy;
    }

    public static long generateTimestamp() {
        ServerTimeSource serverTimeSource = finalServerTimeSource.get();
        if (serverTimeSource != null) {
            return serverTimeSource.currentTime().toTemporalAccessor().toEpochMilli();
        }else{
            synchronized (RequestHandler.class) {
                if(syncServerTime == null) {
                    syncServerTime = new SyncServerTime(timeSync);
                }
            }
        }
        return Instant.now().toEpochMilli();
    }


    /**
     * Build request based on request type and send the requests to server.
     * @param baseUrl base url
     * @param urlPath url path
     * @param signature the signature
     * @param parameters parameters
     * @param httpMethod https method
     * @param requestType request type
     * @return String - response from server
     */

    private String sendApiRequest(String baseUrl, String urlPath, String signature, LinkedHashMap<String, Object> parameters,
                                  HttpMethod httpMethod, RequestType requestType, boolean showLimitUsage) {
        String fullUrl = UrlBuilder.buildFullUrl(baseUrl, urlPath, parameters, signature);
        logger.info("{} {}", httpMethod, fullUrl);
        Request request;
        switch (requestType) {
            case PUBLIC:
                request = RequestBuilder.buildPublicRequest(fullUrl, httpMethod);
                break;
            case WITH_API_KEY:
            case SIGNED:
                request = RequestBuilder.buildApiKeyRequest(fullUrl, httpMethod, apiKey);
                break;
            default:
                throw new BinanceConnectorException("[RequestHandler] Invalid request type: " + requestType);
        }
        return ResponseHandler.handleResponse(request, showLimitUsage, proxy);
    }

    public String sendPublicRequest(String baseUrl, String urlPath, LinkedHashMap<String, Object> parameters,
                                    HttpMethod httpMethod, boolean showLimitUsage) {
        return sendApiRequest(baseUrl, urlPath, null, parameters, httpMethod, RequestType.PUBLIC, showLimitUsage);
    }

    public String sendWithApiKeyRequest(String baseUrl, String urlPath, LinkedHashMap<String, Object> parameters,
                                        HttpMethod httpMethod, boolean showLimitUsage) {
        if (null == apiKey || apiKey.isEmpty()) {
            throw new BinanceConnectorException("[RequestHandler] API key cannot be null or empty!");
        }
        return sendApiRequest(baseUrl, urlPath, null, parameters, httpMethod, RequestType.WITH_API_KEY, showLimitUsage);
    }

    public String sendSignedRequest(String baseUrl, String urlPath, LinkedHashMap<String, Object> parameters,
                                    HttpMethod httpMethod, boolean showLimitUsage) {
        if (null == secretKey || secretKey.isEmpty() || null == apiKey || apiKey.isEmpty()) {
            throw new BinanceConnectorException("[RequestHandler] Secret key/API key cannot be null or empty!");
        }
        parameters.put("timestamp", String.valueOf(generateTimestamp()));
        String queryString = UrlBuilder.joinQueryParameters(parameters);
        String signature = SignatureGenerator.getSignature(queryString, secretKey);
        return sendApiRequest(baseUrl, urlPath, signature, parameters, httpMethod, RequestType.SIGNED, showLimitUsage);
    }
}
