package Controller;

import Model.Product;
import Model.Request;
import Model.VerifyUserRequest;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private Request currentRequest;

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
        askLoginData();
        listener();
    }



    private void askLoginData() {
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
            enterBirthDate();

        } else{
            System.out.println("Enter username: ");
            String usrName = scanner.nextLine();
            System.out.println("Enter password: ");
            String psWord = scanner.nextLine();
            verifyLogin(usrName, psWord);
        }

    }

    private void enterBirthDate() {
        boolean validBirthDate = false;
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}"); // Regular expression for yyyy-MM-dd format

        while(!validBirthDate) {
            System.out.println("Enter birthdate: (yyyy-MM-dd) ");
            String date = scanner.nextLine();
            Matcher matcher = pattern.matcher(date);

            if (matcher.matches()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    // Check if year, month, and day are within valid ranges
                    int year = Integer.parseInt(date.substring(0, 4));
                    int month = Integer.parseInt(date.substring(5, 7));
                    int day = Integer.parseInt(date.substring(8, 10));
                    if (year >= 1900 && year <= 2100 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                        birthDate = sdf.parse(date);
                        validBirthDate = true;
                        /*SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                        String outputString = outputFormat.format(birthDate);
                        System.out.println("Birthdate entered in correct format: " + outputString);
                         */
                    } else {
                        System.out.println("Invalid year, month, or day. Please try again.");
                    }
                } catch (ParseException e) {
                    System.out.println("Error parsing date.");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid year, month, or day format.");
                }
            } else {
                System.out.println("Wrong format. Please try again.");
            }
        }
    }

    private void verifyLogin(String usrName, String psWord) {
        VerifyUserRequest currentRequestv = new VerifyUserRequest(usrName, psWord);
        boolean verification;

        try {
            oos.writeObject(currentRequestv);
            verification = (boolean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if(verification){
            System.out.println("Welcome");
        } else{
            System.out.println("Wrong!");
            askLoginData();
        }
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
