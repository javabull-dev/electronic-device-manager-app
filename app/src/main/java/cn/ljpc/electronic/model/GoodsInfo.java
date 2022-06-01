package cn.ljpc.electronic.model;

public class GoodsInfo {
    public GoodsInfo(Integer providerid, Integer goodsid, String goodsname, String providername, String uuid) {
        this.providerid = providerid;
        this.goodsid = goodsid;
        this.goodsname = goodsname;
        this.providername = providername;
        this.uuid = uuid;
    }

    public GoodsInfo() {
    }

    public Integer getProviderid() {
        return providerid;
    }

    public void setProviderid(Integer providerid) {
        this.providerid = providerid;
    }

    public Integer getGoodsid() {
        return goodsid;
    }

    public void setGoodsid(Integer goodsid) {
        this.goodsid = goodsid;
    }

    public String getGoodsname() {
        return goodsname;
    }

    public void setGoodsname(String goodsname) {
        this.goodsname = goodsname;
    }

    public String getProvidername() {
        return providername;
    }

    public void setProvidername(String providername) {
        this.providername = providername;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "GoodsInfo{" +
                "providerid=" + providerid +
                ", goodsid=" + goodsid +
                ", goodsname='" + goodsname + '\'' +
                ", providername='" + providername + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }

    private Integer providerid;
    private Integer goodsid;

    private String goodsname;
    private String providername;

    private String uuid;
}
