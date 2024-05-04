package Model.Requests;

import Model.Request;

import java.net.Socket;

public class VerifyUserRequest extends Request {
    private String usrName, psWord;
    private Socket notificationSocket;

    public VerifyUserRequest(String usrName, String psWord, String username) {
        super(username);
        this.usrName = usrName;
        this.psWord = psWord;
        this.notificationSocket = null;
    }

    public Socket getNotificationSocket() {
        return notificationSocket;
    }

    public String getUsrName() {
        return usrName;
    }

    public String getPsWord() {
        return psWord;
    }
}
