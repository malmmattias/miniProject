package Model.Requests;

import Model.Request;

public class RegisterInterestRequest extends Request {
    private String interest;

    public RegisterInterestRequest(String interest) {
        this.interest = interest;
    }

    @Override
    public String getInterest() {
        return interest;
    }
}
