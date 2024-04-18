package Controller;

import Model.Product;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;


public class Client {
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private final Socket socket;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String email;
    private String username;
    private String password;
    Scanner scanner = new Scanner(System.in);
    private ArrayList<Product> cart = new ArrayList<Product>();

    public Client() {
        try {
            int port = 1441;
            socket = new Socket("127.0.0.1", port);
            System.out.println("Client: connected");
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        login();
        listener();
    }



    private void login() {
        System.out.println("Are you a new user? y/n");
        String s = scanner.nextLine();
        if(Objects.equals(s, "y")){
            System.out.println("Enter firstname: ");
            firstName = scanner.nextLine();
            System.out.println("Enter lastname: ");
            lastName = scanner.nextLine();
            System.out.println("Enter email: ");
            email = scanner.nextLine();
            System.out.println("Enter username: ");
            username = scanner.nextLine();
            System.out.println("Enter password: ");
            password = scanner.nextLine();
            System.out.println("Enter birthdate: (yyyy-MM-dd) ");
            String date = scanner.nextLine();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                birthDate = sdf.parse(date);
            } catch (ParseException e){
                System.err.println("Error parsing date: " + e.getMessage());
            }
        } else{
            System.out.println("Enter username: ");
            String usrName = scanner.nextLine();
            System.out.println("Enter password: ");
            String psWord = scanner.nextLine();
            verifyLogin(usrName, psWord);
        }

    }

    private void verifyLogin(String usrName, String psWord) {
    }

    private void listener() {
        while(true){
            System.out.println("What do you want to do?");
            System.out.println("Type 1. to add product");
            int input = scanner.nextInt();
            if (input == 1){
                addToCart(new Product());
            }
        }
    }

    public void addToCart(Product product) {
        cart.add(product);
    }

    public void removeFromCart(Product product) {
        cart.remove(product);
    }

    public void clearCart() {
        cart.clear();
    }


}
