package cn.ljpc.electronic.model;

import java.io.Serializable;

public class User implements Serializable {
    private String loginname;

    private String pwd;

    private String salt;

    public User() {
    }

    public User(String loginname, String pwd, String salt) {
        this.loginname = loginname;
        this.pwd = pwd;
        this.salt = salt;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}