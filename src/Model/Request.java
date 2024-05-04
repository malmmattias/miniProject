package Model;

import java.io.Serializable;

public abstract class Request implements Serializable {
    private String username;
    private String passWord;
    private String itemName;

    public Request(String username) {
        this.username = username;
    }

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

    public void setItemName(String itemName){
        this.itemName = itemName;
    }
    public String getInterest(){
        return itemName;
    }
}
