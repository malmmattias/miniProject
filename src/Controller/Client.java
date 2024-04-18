package Controller;

import Model.Product;

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String email;
    private String username;
    private String password;
    private ArrayList<Product> cart = new ArrayList<Product>();

    public Client() {
        listener();
    }

    public Client(String firstName, String lastName, Date birthDate, String email, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.email = email;
        this.username = username;
        this.password = password;
        listener();
    }

    private void listener() {
        Scanner scanner = new Scanner(System.in);
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
