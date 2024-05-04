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
    private final String username;
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
            System.out.println("Client: connected");
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        username = askLoginData();



        notificationListener();


        listener();




    }

    private void notificationListener() {
        Thread notificationThread = new Thread(() -> {
            try {
                Socket nSocket = new Socket(host, 8000);
                //System.out.println("Client: noti connected");
                ObjectInputStream nois = new ObjectInputStream(nSocket.getInputStream());
                ObjectOutputStream noos = new ObjectOutputStream(nSocket.getOutputStream());

                noos.writeObject(username);

                noos.flush();

                while(true){
                    //System.out.println("Waiting for notification");
                    String object = (String) nois.readObject();
                    System.out.println(object);

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
        System.out.println("Choose what you want to do!");
        System.out.println("1. Sell product.");
        System.out.println("2. Search for product.");
        System.out.println("3. Register interest in product category.");
        System.out.println("4. Show purchase history.");
        //System.out.println("5. Display cart");
        //System.out.println("6. Check purchase requests");
        System.out.println("5. Check purchase requests");
        int choice = scanner.nextInt();
        scanner.nextLine();
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
                //showCart();
                checkPurchaseRequests();
                break;
            case 6:
                //checkPurchaseRequests();
                break;
        }
    }

    private void checkPurchaseRequests() {
        try {
            oos.writeObject(new CheckPurchaseRequests(username));
            oos.flush();

            ArrayList<Product> buyerRequests = (ArrayList<Product>) ois.readObject();

            if (!buyerRequests.isEmpty()) {

                String permission;

                for (Product p : buyerRequests) {

                    permission = STR."PERMISSION: Do you, \{p.getSeller()}, agrre to sell product \{p.getName()} for \{p.getPrice()}to \{p.getBuyer()}? y/n";

                    System.out.println(permission);
                    String inpt =  scanner.nextLine();

                    if(Objects.equals(inpt, "y")) {
                        p.setStatus(Status.PENDING);
                    } else{
                        System.out.println("You rejected the transaction");
                    }
                    //System.out.println(p.toString2());
                }
            } else {
                System.out.println("You have zero purchase requests");
            }

            oos.writeObject(buyerRequests);
            oos.flush();

            /*
            ConfirmationRequest()



            if (buyerRequests == null || buyerRequests.isEmpty()) {
                System.out.println("You have zero requests");
            } else {
                for (Product p : buyerRequests) {
                    // Process each request, e.g., set status, add to seller response
                    p.setStatus(Status.SOLD);
                    sellerResponse.add(p);
                }
                cpr.setBuyerRequests(sellerResponse);
                cpr.setVerified(true);

                // Send seller response back to server
                oos.writeObject(cpr);
                oos.flush();
            }*/

        } catch (EOFException e) {
            // Handle EOFException gracefully (e.g., log the error)
            System.err.println("EOFException: Error reading object from input stream.");
            e.printStackTrace();
        } catch (IOException | ClassNotFoundException e) {
            // Handle other IO or class not found exceptions
            throw new RuntimeException(e);
        }
    }


    private void showCart() {
        Product product = new Product.Builder("MacBook", 100, 2024, username, "none")
                .color("Red")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.SOLD)
                .build();
        addToCart(product);
        product = new Product.Builder("Iphone", 100, 2024, username, "none")
                .color("Red")
                .itemCondidtion(ItemCondition.USED)
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
        scanner.nextLine();


        if (input == 1) {
            sendPurchaseRequest(cart);
            System.out.println("Your purchase request has been sent!");
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
        scanner.nextLine();
        String interest = scanner.nextLine();
        currRequest = new RegisterInterestRequest(interest, username);
        currRequest.setUsername(username);
        try {
            oos.writeObject(currRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void searchProduct() {
        System.out.println("Enter product you want to search for: ");
        //String response = scanner.nextLine(); // To clear the scanner bug.
        String productName = scanner.nextLine();
        System.out.println("Would you like to filter the search? y/n");
        String response = scanner.nextLine();

        if (response.equals("y")) {
            System.out.println("Enter minimum price range");
            minPrice = scanner.nextInt();
            scanner.nextLine();

            System.out.println("Enter maximum price range");
            maxPrice = scanner.nextInt();
            scanner.nextLine();

            ItemCondition searchCondition = getItemCondition();
            Boolean filtered = true;
            currRequest = new SearchProductRequest(productName, minPrice, maxPrice, searchCondition, filtered, username);
            System.out.println("Jag når hit");
        } else {
            Boolean filtered = false;
            currRequest = new SearchProductRequest(productName, minPrice, maxPrice, itemCondition, filtered, username);
            System.out.println("Jag nådde elseblocket");
        }


        // currRequest = new SearchProductRequest(productName);
        boolean productFound = false;

        try {
            oos.writeObject(currRequest);
            Object o = ois.readObject();

            if (o instanceof Boolean) {

                productFound = (boolean) o;
                if (productFound) {
                    System.out.println("Product found!" + "\n");
                    clearConsole();
                    o = ois.readObject();
                    ArrayList<Product> productsList = (ArrayList) o;

                    //if (o instanceof ArrayList) {
                        int count = 1;
                        for (Product a : productsList) {
                            System.out.println(count++ + ". " + a.toString2());
                        }
                    //}

                    addToCartOption(productsList);

                } else {
                    System.out.println("Product not found!" + "\n");
                    clearConsole();
                }
            }


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void addToCartOption(ArrayList<Product> products) throws IOException {
        System.out.println("Do you want to add product to cart? type the number of the product you want to add to cart or 0 to go back");
        String response = scanner.nextLine();
        int response2 = Integer.parseInt(response) -1;
        oos.writeObject(response2);
    }

    private ItemCondition getItemCondition() {
        System.out.println("What condition should the product be in?" + "\n" +
                "1 = NEW,\n" +
                "2 = VERY_GOOD,\n" +
                "3 = GOOD,\n" +
                "4 = USED,\n" +
                "5 = NOT_WORKING_PROPERLY");
        int condition = scanner.nextInt();
        scanner.nextLine();

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
            //System.out.println("Im printing");
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
        Product product = new Product.Builder("Sample Product", 100, 2024, username, "none")
                .color("Red")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();

        boolean loop = true;

        while (loop) {
            System.out.println("Your product is: " + product.toString());
            System.out.println("press the letter to change an attribute e.g. 'p' to change price, press r to ready");

            String input = scanner.next();

            switch (input) {
                case "r":
                    loop = false;
                    break;
                case "p":
                    System.out.println("Enter new price: ");
                    int newPrice = scanner.nextInt();
                    scanner.nextLine();

                    product.setPrice(newPrice);

                    break;
                case "y":
                    System.out.println("Enter new year: ");
                    int newYear = scanner.nextInt();
                    scanner.nextLine();

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
                    scanner.nextLine();

                    modifyItemCondition(itemCondition, product);
                    break;
                case "c":
                    System.out.println("Enter new color: ");
                    nothing = scanner.nextLine(); //Bug
                    String color = scanner.nextLine();
                    product.setColor(color);
                case "n":
                    System.out.println("Enter new name:");
                    nothing = scanner.nextLine(); //Bug
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

    private String askLoginData() {
        System.out.println("Are you a new user? y/n");
        String localized;
        String s = scanner.nextLine();
        if (Objects.equals(s, "y")) {
            System.out.println("Enter firstname: ");
            firstName = scanner.nextLine();
            System.out.println("Enter lastname: ");
            lastName = scanner.nextLine();
            System.out.println("Enter email: ");
            email = scanner.nextLine();
            System.out.println("Enter username: ");
            localized = scanner.nextLine();
            System.out.println("Enter password: ");
            password = scanner.nextLine();
            enterBirthDate();
            addUserToServer();
        } else {
            System.out.println("Enter username: ");
            localized = scanner.nextLine();
            System.out.println("Enter password: ");
            password = scanner.nextLine();
            localized = verifyLogin(localized, password);
        }

        return localized;

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
        System.out.println("Your login credentials are now saved on the server!");
    }

    private String verifyLogin(String usrName, String psWord) {
        currRequest = new VerifyUserRequest(usrName, psWord, username);
        boolean verification;

        try {
            oos.writeObject(currRequest);
            verification = (boolean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (verification) {
            System.out.println("Welcome");
            return usrName;
        } else {
            System.out.println("Wrong!");
            return "";
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
        System.out.println();
        for (int i = 0; i < 100; i++) {
            System.out.print(".");
        }
        System.out.println();
    }




}
