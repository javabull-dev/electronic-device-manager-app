package cn.ljpc.electronic.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @param <T> 返回结果的类型
 * @param <R> 传输参数的类型
 */
public class GsonRequest<T, R> extends Request<T> {
    private final Gson gson = new Gson();
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;
    private R sendData;

    public GsonRequest(int method, String url, Class<T> clazz, Map<String, String> headers, R sendData,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
        this.sendData = sendData;
    }

    /**
     * 默认是post方式
     *
     * @param url
     * @param clazz
     * @param headers
     * @param sendData
     * @param listener
     * @param errorListener
     */
    public GsonRequest(String url, Class<T> clazz, Map<String, String> headers, R sendData,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(Method.POST, url, clazz, headers, sendData, listener, errorListener);
    }

    public void setSendData(R sendData) {
        this.sendData = sendData;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        Gson gson = new Gson();
        String str = gson.toJson(sendData);
        Log.d("lan", "待发送的数据："+str);
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
