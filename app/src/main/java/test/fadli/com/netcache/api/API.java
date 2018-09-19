package test.fadli.com.netcache.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class API {
    public static String APIHost = "http://jsonplaceholder.typicode.com/posts/";

    public static void getData(Context context, String param, ResponseCallback onResponse, ErrorCallback onError) {
        String url = "";
        url = APIHost + param;

        /*HashMap<String, String> data = new HashMap<>();
        try {
            data.put("param", param);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        RequestManager.get(context, url, onResponse, onError);
    }

    public static class RequestManager {

        private final static OkHttpClient httpClient = new OkHttpClient();

        private static String stringifyRequestBody(Request request) {
            try {
                okio.Buffer buffer = new okio.Buffer();
                request.newBuilder().build().body().writeTo(buffer);

                return buffer.readUtf8();
            } catch (IOException e) {

            }

            return null;
        }

        private static Request getRequest(String url, RequestBody requestBody) {
            Headers.Builder httpHeadersBuilder = new Headers.Builder();
            httpHeadersBuilder.add("Accept", "application/json");
            httpHeadersBuilder.add("Content-Type", "application/json");


            Headers httpHeaders = httpHeadersBuilder.build();
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .headers(httpHeaders);


            if (requestBody != null) {
                requestBuilder.post(requestBody);
            }

            Request request = requestBuilder.build();

            String requestBodyString = null;
            if (requestBody != null) requestBodyString = stringifyRequestBody(request);

            return request;
        }

        private static void createHttpRequest(Context context, Request request,
                                              final ResponseCallback onSuccess, final ErrorCallback onError) {

            httpClient.setConnectTimeout(7, TimeUnit.SECONDS);
            httpClient.setReadTimeout(60, TimeUnit.SECONDS);
            httpClient.newCall(request).enqueue(new Callback() {
                Handler mainHandler = new Handler(Looper.getMainLooper());

                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    final String responseBody = response.body().string();
                    final int responseCode = response.code();

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (onSuccess != null) {
                                onSuccess.doAction(responseBody, responseCode);
                            }
                        }
                    });
                }
            });
        }

        public static void get(Context context, final String url,
                               final ResponseCallback onSuccess, final ErrorCallback onError) {
            Request request = getRequest(url, null);
            createHttpRequest(context, request, onSuccess, onError);
        }

        public static void post_json(Context context, final String url, String json,
                                     final ResponseCallback onSuccess, final ErrorCallback onError) {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(JSON, json.getBytes());
            Request request = getRequest(url, body);

            createHttpRequest(context, request, onSuccess, onError);
        }

        public static void post(Context context, final String url, final Map<String, String> parameters,
                                final ResponseCallback onSuccess, final ErrorCallback onError) {

            FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();

            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    formEncodingBuilder.add(entry.getKey(), entry.getValue());
                }
            }

            RequestBody requestBody = formEncodingBuilder.build();
            Request request = getRequest(url, requestBody);

            createHttpRequest(context, request, onSuccess, onError);
        }
    }
}
