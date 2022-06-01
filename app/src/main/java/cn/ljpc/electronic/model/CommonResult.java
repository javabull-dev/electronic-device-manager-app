package cn.ljpc.electronic.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CommonResult implements Serializable {

    private Map<String, Object> data = new HashMap<>();

    private String msg = "";

    @Override
    public String toString() {
        return "CommonResult{" +
                "data=" + data +
                ", msg='" + msg + '\'' +
                ", code=" + code +
                '}';
    }

    private Integer code = 200;

    public Map<String, Object> getData() {
        return data;
    }

    public CommonResult data(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public static CommonResult success() {
        CommonResult commonResult = new CommonResult();
        commonResult.msg = "请求成功";
        return commonResult;
    }

    public static CommonResult error() {
        CommonResult commonResult = new CommonResult();
        commonResult.msg = "请求失败";
        commonResult.code = 300;
        return commonResult;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}