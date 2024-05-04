package Controller;

import Model.*;
import Model.Requests.*;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;


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
    private String nothing;
    Scanner scanner = new Scanner(System.in);
    private ArrayList<Product> cart = new ArrayList<Product>();
    private Request currRequest;
    private Object currResponse;
    private int minPrice = 0;
    private int maxPrice = 1000000;
    private ItemCondition itemCondition = ItemCondition.USED;
    private final int port = 1441;
    private final int nPort = 8000;
    private final String host = "127.0.0.1";

    public Client() {
        try {
            socket = new Socket(host, port);
            printString("Client: connected");
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        askLoginData();



        notificationListener();


        listener();




    }

    private void notificationListener() {
        Thread notificationThread = new Thread(() -> {
            try {
                Socket nSocket = new Socket(host, 8000);
                printString("Client: noti connected");
                ObjectInputStream nois = new ObjectInputStream(nSocket.getInputStream());
                ObjectOutputStream noos = new ObjectOutputStream(nSocket.getOutputStream());

                noos.writeObject(username);

                noos.flush();

                while(true){
                    printString("Waiting for notification");
                    String object = (String) nois.readObject();

                    if (object.startsWith("PERMISSION")) {
                        printString(object);

                        noos.writeObject("n");
                        noos.flush();
                    }
                }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        });
        notificationThread.start();
    }

    private void menu() {
        clearConsole();
        printString("Choose what you want to do!");
        printString("1. Sell product.");
        printString("2. Search for product.");
        printString("3. Register interest in product category.");
        printString("4. Show purchase history.");
        printString("5. Display cart");
        int choice = scanner.nextInt();
        getStringInput();
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
        Product product = new Product.Builder("MacBook", 100, 2024, username)
                .color("Red")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.SOLD)
                .build();
        addToCart(product);
        product = new Product.Builder("Iphone", 100, 2024, username)
                .color("Red")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();
        addToCart(product);
        printString("Products in your cart:");
        for (Product p : cart) {
            printString(p.getName());
        }
        printString("Do you want to check out or continue shopping?");
        printString("1. Check out");
        printString("2. Continue shopping");
        int input = scanner.nextInt();
        getStringInput();


        if (input == 1) {
            sendPurchaseRequest(cart);
            printString("Your purchase request has been sent!");
            clearCart();
        }
    }

    private void sendPurchaseRequest(ArrayList<Product> cart) {
        currRequest = new BuyProductRequest(cart, username);
        currRequest.setUsername(username);
        try {
            oos.writeObject(currRequest);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void purchaseHistory() {
        currRequest = new PurchaseHistoryRequest(username);
        currRequest.setUsername(username);
        currResponse = null;
        try {
            oos.writeObject(currRequest);
            ArrayList<String> history = (ArrayList<String>) ois.readObject();
            if (history == null) {
                printString("null");
            }
            clearConsole();
            if (history == null || history.isEmpty()) {
                printString("Your history is empty");
            } else {
                printString("Your history");
                int counter = 1;
                for (String str : history) {
                    printString(counter++ + ". " + str);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void registerInterest() {
        printString("Select Category to register interest in:");
        getStringInput();
        String interest = getStringInput();
        currRequest = new RegisterInterestRequest(interest, username);
        currRequest.setUsername(username);
        try {
            oos.writeObject(currRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void searchProduct() {
        printString("Enter product you want to search for: ");
        String response = getStringInput(); // To clear the scanner bug.
        String productName = getStringInput();
        printString("Would you like to filter the search? y/n");
        response = getStringInput();

        if (response.equals("y")) {
            printString("Enter minimum price range");
            minPrice = scanner.nextInt();
            getStringInput();

            printString("Enter maximum price range");
            maxPrice = scanner.nextInt();
            getStringInput();

            ItemCondition searchCondition = getItemCondition();
            Boolean filtered = true;
            currRequest = new SearchProductRequest(productName, minPrice, maxPrice, searchCondition, filtered, username);
            printString("Jag når hit");
        } else {
            Boolean filtered = false;
            currRequest = new SearchProductRequest(productName, minPrice, maxPrice, itemCondition, filtered, username);
            printString("Jag nådde elseblocket");
        }


        // currRequest = new SearchProductRequest(productName);
        boolean productFound = false;

        try {
            oos.writeObject(currRequest);
            Object o = ois.readObject();

            if (o instanceof Boolean) {

                productFound = (boolean) o;
                if (productFound) {
                    printString("Product found!" + "\n");
                    clearConsole();
                    o = ois.readObject();
                    if (o instanceof ArrayList) {
                        ArrayList<Product> productsList = (ArrayList) o;
                        int count = 1;
                        for (Product a : productsList) {
                            printString(count++ + ". " + a.toString2());
                        }
                    }

                    addToCartOption();

                } else {
                    printString("Product not found!" + "\n");
                    clearConsole();
                }
            }


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void addToCartOption() throws IOException {
        printString("Do you want to add product to cart? type the number of the product you want to add to cart or 0 to go back");
        String response = getStringInput();
        oos.writeObject(response);
    }

    private ItemCondition getItemCondition() {
        printString("What condition should the product be in?" + "\n" +
                "1 = NEW,\n" +
                "2 = VERY_GOOD,\n" +
                "3 = GOOD,\n" +
                "4 = USED,\n" +
                "5 = NOT_WORKING_PROPERLY");
        int condition = scanner.nextInt();
        getStringInput();

        ItemCondition itemCondition = null;
        switch (condition) {
            case 1:
                itemCondition = ItemCondition.NEW;
                break;
            case 2:
                itemCondition = ItemCondition.VERY_GOOD;
                break;
            case 3:
                itemCondition = ItemCondition.GOOD;
                break;
            case 4:
                itemCondition = ItemCondition.USED;
                break;
            case 5:
                itemCondition = ItemCondition.NOT_WORKING_PROPERLY;
                break;
        }
        return itemCondition;
    }

    private void sellProduct() {
        clearConsole();
        Product product = createProduct();
        currRequest = new SellProductRequest(product, username);
        try {
            printString("Im printing");
            oos.writeObject(currRequest);
            boolean verification = (boolean) ois.readObject();
            if (verification) {
                printString("Your product has been added successfully");
            } else {
                printString("Your product has not been added successfully");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Product createProduct() {
        Product product = new Product.Builder("Sample Product", 100, 2024, username)
                .color("Red")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();

        boolean loop = true;

        while (loop) {
            printString("Your product is: " + product.toString());
            printString("press the letter to change an attribute e.g. 'p' to change price, press r to ready");

            String input = scanner.next();

            switch (input) {
                case "r":
                    loop = false;
                    break;
                case "p":
                    printString("Enter new price: ");
                    int newPrice = scanner.nextInt();
                    getStringInput();

                    product.setPrice(newPrice);

                    break;
                case "y":
                    printString("Enter new year: ");
                    int newYear = scanner.nextInt();
                    getStringInput();

                    product.setYearOfProduction(newYear);
                    break;
                case "i":
                    printString("Enter new item condition" +
                            "    1 = NEW,\n" +
                            "    2 = VERY_GOOD,\n" +
                            "    3 = GOOD,\n" +
                            "    4 = USED,\n" +
                            "    5 = NOT_WORKING_PROPERLY ");
                    int itemCondition = scanner.nextInt();
                    getStringInput();

                    modifyItemCondition(itemCondition, product);
                    break;
                case "c":
                    printString("Enter new color: ");
                    nothing = getStringInput(); //Bug
                    String color = getStringInput();
                    product.setColor(color);
                case "n":
                    printString("Enter new name:");
                    nothing = getStringInput(); //Bug
                    String newName = getStringInput();
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
        printString("Are you a new user? y/n");
        String s = getStringInput();
        if (Objects.equals(s, "y")) {
            printString("Enter firstname: ");
            firstName = getStringInput();
            printString("Enter lastname: ");
            lastName = getStringInput();
            printString("Enter email: ");
            email = getStringInput();
            printString("Enter username: ");
            username = getStringInput();
            printString("Enter password: ");
            password = getStringInput();
            enterBirthDate();
            addUserToServer();
        } else {
            printString("Enter username: ");
            username = getStringInput();
            printString("Enter password: ");
            password = getStringInput();
            verifyLogin(username, password);
        }

    }

    private void addUserToServer() {
        currRequest = new AddUserRequest(username);
        currRequest.setUsername(username);
        currRequest.setPassWord(password);

        try {
            oos.writeObject(currRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        printString("Your login credentials are now saved on the server!");
    }

    private void verifyLogin(String usrName, String psWord) {
        currRequest = new VerifyUserRequest(usrName, psWord, username);
        boolean verification;

        try {
            oos.writeObject(currRequest);
            verification = (boolean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (verification) {
            printString("Welcome");
        } else {
            printString("Wrong!");
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
            printString("Enter birthdate: (yyyy-MM-dd) ");
            String date = getStringInput();
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
                        printString("Birthdate entered in correct format: " + outputString);
                         */
                    } else {
                        printString("Invalid year, month, or day. Please try again.");
                    }
                } catch (ParseException e) {
                    printString("Error parsing date.");
                } catch (NumberFormatException e) {
                    printString("Invalid year, month, or day format.");
                }
            } else {
                printString("Wrong format. Please try again.");
            }
        }
    }

    /**
     * Method to clear the console window by printing a dotted line.
     */
    public void clearConsole() {
        printString("");
        for (int i = 0; i < 100; i++) {
            System.out.print(".");
        }
        printString("");
    }

    public synchronized String getStringInput() {
        return scanner.nextLine();
    }

    public synchronized void printString(String string) {
        System.out.println(string);
    }


}
