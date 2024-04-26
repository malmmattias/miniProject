package Controller;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        Client client = new Client();

        ArrayList<String> list = server.getPurchaseHistory("john");

        for(String s : list){
            System.out.println(s);
        }
    }


}