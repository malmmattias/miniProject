package Controller;

import Model.*;
import Model.Requests.*;

import java.io.DataOutput;
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
    private Request currRequest;
    private Object currResponse;

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

    private void menu() {
        clearConsole();
        System.out.println("Choose what you want to do!");
        System.out.println("1. Sell product.");
        System.out.println("2. Search for product.");
        System.out.println("3. Register interest in product category.");
        System.out.println("4. Show purchase history.");
        System.out.println("5. Display cart");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                sellProduct();
                break;
            case 2:
                searchProduct();
                break;
            case 3:
                registerInterest();
                break;
            case 4:
                purchaseHistory();
                break;
            case 5:
                showCart();
                break;
        }
    }

    private void showCart() {
        Product product = new Product.Builder("MacBook", 100, 2024)
                .color("Red")
                .condition(ItemCondition.USED)
                .status(Status.SOLD)
                .build();
        addToCart(product);
        product = new Product.Builder("Iphone", 100, 2024)
                .color("Red")
                .condition(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();
        addToCart(product);
        System.out.println("Products in your cart:");
        for (Product p : cart) {
            System.out.println(p.getName());
        }
        System.out.println("Do you want to check out or continue shopping?");
        System.out.println("1. Check out");
        System.out.println("2. Continue shopping");
        int input = scanner.nextInt();

        if(input == 1){
            sendPurchaseRequest(cart);
            System.out.println("Your purchase request has been sent!");
            clearCart();
        }
    }

    private void sendPurchaseRequest(ArrayList<Product> cart) {
        currRequest = new BuyProductRequest(cart);
        currRequest.setUsername(username);
        try {
            oos.writeObject(currRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void purchaseHistory() {
        currRequest = new PurchaseHistoryRequest();
        currRequest.setUsername(username);
        currResponse = null;
        try {
            oos.writeObject(currRequest);
            ArrayList<String> history = (ArrayList<String>) ois.readObject();
            if (history == null) {
                System.out.println("null");
            }
            clearConsole();
            if (history == null || history.isEmpty()) {
                System.out.println("Your history is empty");
            } else {
                System.out.println("Your history");
                int counter = 1;
                for (String str : history) {
                    System.out.println(counter++ + ". " + str);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void registerInterest() {
        System.out.println("Select Category to register interest in:");
    }

    private void searchProduct() {
        System.out.println("Enter product you want to search for: ");
        String nothing = scanner.nextLine(); // To clear the scanner bug.
        String productName = scanner.nextLine();
        currRequest = new SearchProductRequest(productName);
        boolean productFound = false;

        try {
            oos.writeObject(currRequest);
            productFound = (boolean) ois.readObject();

            if (productFound) {
                System.out.println("Product found!");
            } else {
                System.out.println("Product not found.");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void sellProduct() {
        clearConsole();
        Product product = createProduct();
        currRequest = new SellProductRequest(product);
        try {
            oos.writeObject(currRequest);
            boolean verification = (boolean) ois.readObject();
            if (verification) {
                System.out.println("Your product has been added successfully");
            } else {
                System.out.println("Your product has not been added successfully");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Product createProduct() {
        Product product = new Product.Builder("Sample Product", 100, 2024)
                .color("Red")
                .condition(ItemCondition.USED)
                .status(Status.SOLD)
                .build();

        boolean loop = true;

        while (loop) {
            System.out.println("Your product is: " + product.toString());
            System.out.println("press the letter to change an attribute e.g. 'p' to change price, press r to continue");

            String input = scanner.next();

            switch (input) {
                case "r":
                    loop = false;
                    break;
                case "p":
                    System.out.println("Enter new price: ");
                    int newPrice = scanner.nextInt();
                    product.setPrice(newPrice);
                    break;
                case "y":
                    System.out.println("Enter new year: ");
                    int newYear = scanner.nextInt();
                    product.setYearOfProduction(newYear);
                    break;
                case "i":
                    System.out.println("Enter new item condition" +
                            "    1 = NEW,\n" +
                            "    2 = VERY_GOOD,\n" +
                            "    3 = GOOD,\n" +
                            "    4 = USED,\n" +
                            "    5 = NOT_WORKING_PROPERLY ");
                    int itemCondition = scanner.nextInt();
                    modifyItemCondition(itemCondition, product);
                    break;
                case "c":
                    System.out.println("Enter new color: ");
                    String color = scanner.nextLine();
                    product.setColor(color);
                case "n":
                    System.out.println("Enter new name:");
                    String newName = scanner.nextLine();
                    product.setName(newName);
            }

        }

        return product;
    }

    private void modifyItemCondition(int itemCondition, Product product) {
        if (itemCondition == 1) {
            product.setItemCondition(ItemCondition.NEW);
        }
        if (itemCondition == 2) {
            product.setItemCondition(ItemCondition.VERY_GOOD);
        }
        if (itemCondition == 3) {
            product.setItemCondition(ItemCondition.GOOD);
        }
        if (itemCondition == 4) {
            product.setItemCondition(ItemCondition.USED);
        }
        if (itemCondition == 5) {
            product.setItemCondition(ItemCondition.NOT_WORKING_PROPERLY);
        }
    }

    private void askLoginData() {
        System.out.println("Are you a new user? y/n");
        String s = scanner.nextLine();
        if (Objects.equals(s, "y")) {
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
            addUserToServer();
        } else {
            System.out.println("Enter username: ");
            username = scanner.nextLine();
            System.out.println("Enter password: ");
            password = scanner.nextLine();
            verifyLogin(username, password);
        }

    }

    private void addUserToServer() {
        currRequest = new AddUserRequest();
        currRequest.setUsername(username);
        currRequest.setPassWord(password);

        try {
            oos.writeObject(currRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Your login credentials are now saved on the server!");
    }

    private void verifyLogin(String usrName, String psWord) {
        currRequest = new VerifyUserRequest(usrName, psWord);
        boolean verification;

        try {
            oos.writeObject(currRequest);
            verification = (boolean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (verification) {
            System.out.println("Welcome");
        } else {
            System.out.println("Wrong!");
            askLoginData();
        }
    }

    private void listener() {
        while (true) {
            menu();
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

    private void enterBirthDate() {
        boolean validBirthDate = false;
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}"); // Regular expression for yyyy-MM-dd format

        while (!validBirthDate) {
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

    /**
     * Method to clear the console window by printing a dotted line.
     */
    public void clearConsole() {
        for (int i = 0; i < 75; i++) {
            System.out.print(".");
        }
        System.out.println();
    }


}
