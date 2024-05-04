package Model.Requests;

import Model.Request;

public class RegisterInterestRequest extends Request {
    private String interest;

    public RegisterInterestRequest(String interest, String username) {
        super(username);
        this.interest = interest;
    }

    @Override
    public String getInterest() {
        return interest;
    }
}
