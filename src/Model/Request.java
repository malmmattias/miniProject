package Model;

import java.io.Serializable;

public abstract class Request implements Serializable {
    private String username;
    private String passWord;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
