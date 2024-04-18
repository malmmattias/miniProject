package Model;

public class VerifyUserRequest extends Request{
    private String usrName, psWord;

    public VerifyUserRequest(String usrName, String psWord) {
        this.usrName = usrName;
        this.psWord = psWord;
    }

    public String getUsrName() {
        return usrName;
    }

    public String getPsWord() {
        return psWord;
    }
}
