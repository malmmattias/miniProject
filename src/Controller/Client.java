package Controller;

import Model.*;
import Model.Requests.*;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class Client {
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    private String username;
    private String password;
    Scanner scanner = new Scanner(System.in);
    private Request currRequest;
    private int minPrice = 0;
    private int maxPrice = 1000000;
    private final ItemCondition itemCondition = ItemCondition.USED;
    private final String host = "127.0.0.1";

    public Client() {
        try {
            int port = 1441;
            Socket socket = new Socket(host, port);
            System.out.println("Client: connected");
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
                //System.out.println("Client: noti connected");
                ObjectInputStream nois = new ObjectInputStream(nSocket.getInputStream());
                ObjectOutputStream noos = new ObjectOutputStream(nSocket.getOutputStream());

                noos.writeObject(username);
                //noos.flush();

                while(true){
                    //System.out.println("Waiting for notification");
                    String object = (String) nois.readObject();
                    System.out.println(object);

                }

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

        });
        notificationThread.start();
    }

    private void menu() {
        try {
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clearConsole();
        System.out.println("Choose what you want to do!");
        System.out.println("1. Sell product.");
        System.out.println("2. Search for product.");
        System.out.println("3. Register interest in product category.");
        System.out.println("4. Show purchase history.");
        System.out.println("5. Check purchase requests");
        System.out.println("6. Check out cart");
        System.out.println("7. To exit");
        int choice = scanner.nextInt();
        scanner.nextLine();
        try {
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
                    checkPurchaseRequests();
                    break;
                case 6:
                    cart();
                    break;
                case 7:
                    exitClient();
                    //checkNoti();
                    break;

            }
        } catch (Exception e) {
            System.out.println("Weird input!");
        }
    }

    private void cart() throws IOException {
        System.out.println("Your purchase requests are sent, now you will get approval notifications when sellers accept your request");
        oos.writeObject(new BuyCart(username));
    }

    private void checkNoti() {
        try {
            oos.writeObject(new FetchNotifications(username));
            String response = (String) ois.readObject();
            System.out.println(response);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void exitClient() {
        System.out.println("Goodbye "+ username);
        try {
            oos.writeObject(new ExitRequest(username));
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.exit(0);
    }

    private void checkPurchaseRequests() {
        try {
            oos.writeObject(new CheckPurchaseRequests(username));

            ArrayList<Product> buyerRequests = (ArrayList<Product>) ois.readObject();

            if (!buyerRequests.isEmpty()) {

                String permission;

                for (Product p : buyerRequests) {

                    permission = "PERMISSION: Do you, " + p.getSeller() + ", agrre to sell product " + p.getName() + " for " + p.getPrice() + " to " + p.getBuyer() + "? y/n";

                    System.out.println(permission);
                    String inpt =  scanner.nextLine();

                    if(Objects.equals(inpt, "y")) {
                        p.setStatus(Status.PENDING);
                    } else{
                        System.out.println("You rejected the transaction");
                        p.setStatus(Status.REJECTED);
                    }
                }
            } else {
                System.out.println("You have zero purchase requests");
            }

            oos.writeObject(buyerRequests);

        } catch (EOFException e) {
            // Handle EOFException gracefully (e.g., log the error)
            System.err.println("EOFException: Error reading object from input stream.");
        } catch (IOException | ClassNotFoundException e) {
            // Handle other IO or class not found exceptions
            throw new RuntimeException(e);
        }
    }

    private void purchaseHistory() {
        currRequest = new PurchaseHistoryRequest(username);
        currRequest.setUsername(username);
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
                System.out.println("Will you sort your history by date? then type the YYYY – otherwise just type something else");
                String focus = scanner.nextLine();

                List<String> filteredList = getInput(history, focus);

                System.out.println("Your history");

                int counter = 1;
                for (String str : filteredList) {
                    System.out.println(counter++ + ". " + str);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<String> getInput(List<String> inputList, String inputDate) {
        try {
            List<String> filteredList = new ArrayList<>();

            for (String stri : inputList) {
                String str = stri.substring(stri.length() - 4);
                int year = Integer.parseInt(str);
                int input = Integer.parseInt(inputDate);

                if(year>input){
                    filteredList.add(stri);
                }
            }
            return filteredList;

        } catch (Exception e) {
            return inputList;
        }
    }

    private void registerInterest() {
        System.out.println("Select Category to register interest in:");
        String interest = scanner.nextLine();
        currRequest = new RegisterInterestRequest(interest, username);
        currRequest.setUsername(username);
        try {
            oos.writeObject(currRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("You subscribed to an interest");
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
            //System.out.println("Jag når hit");
        } else {
            Boolean filtered = false;
            currRequest = new SearchProductRequest(productName, minPrice, maxPrice, itemCondition, filtered, username);
            //System.out.println("Jag nådde elseblocket");
        }
        boolean productFound;

        try {
            oos.writeObject(currRequest);
            Object o = ois.readObject();

            if (o instanceof Boolean) {

                productFound = (boolean) o;
                if (productFound) {
                    System.out.println("Product found!");
                    o = ois.readObject();
                    ArrayList<Product> productsList = (ArrayList) o;

                    //if (o instanceof ArrayList) {
                    int count = 1;
                    for (Product prod : productsList) {
                        System.out.println(count++ + ". " + prod.toStringHorizontal());
                    }
                    //}

                    addToCartOption(productsList);

                } else {
                    System.out.println("Product not found!");
                }
            }


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void addToCartOption(ArrayList<Product> productsList) throws IOException {
        System.out.println("Do you want to add product to cart? type the number of the product you want to add to cart or 0 to go back");
        String response = scanner.nextLine();
        int purchaseDecision = -1;
        try {
            purchaseDecision = Integer.parseInt(response) -1;
            if(productsList.get(purchaseDecision).getStatus()!=Status.SOLD) {
                System.out.println("Product added to your cart, now you may checkout");
            } else {
                System.out.println("Already sold!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Weird input!");
        }
        oos.writeObject(purchaseDecision);
    }

    private ItemCondition getItemCondition() {
        System.out.println("""
                What condition should the product be in?
                1 = NEW,
                2 = VERY_GOOD,
                3 = GOOD,
                4 = USED,
                5 = NOT_WORKING_PROPERLY""");
        int condition = scanner.nextInt();
        scanner.nextLine();

        return switch (condition) {
            case 1 -> ItemCondition.NEW;
            case 2 -> ItemCondition.VERY_GOOD;
            case 3 -> ItemCondition.GOOD;
            case 4 -> ItemCondition.USED;
            case 5 -> ItemCondition.NOT_WORKING_PROPERLY;
            default -> null;
        };
    }

    private void sellProduct() {
        clearConsole();
        Product product = createProduct();
        if(product!=null) {
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
    }

    private Product createProduct() {
        Product product = new Product.Builder("Sample Product", 100, 2024, username, "none")
                .color("Red")
                .itemCondidtion(ItemCondition.USED)
                .status(Status.AVAILABLE)
                .build();

        boolean loop = true;

        while (loop) {
            System.out.println("Your product is: " + product.toStringVertical());
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
                    String nothing = scanner.nextLine(); //Bug
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

    private void askLoginData() {
        System.out.println("Are you a new user? y/n");
        String s = scanner.nextLine();
        if (Objects.equals(s, "y")) {
            System.out.println("Enter firstname: ");
            String firstName = scanner.nextLine();
            System.out.println("Enter lastname: ");
            String lastName = scanner.nextLine();
            System.out.println("Enter email: ");
            String email = scanner.nextLine();
            System.out.println("Enter username: ");
            username = scanner.nextLine();
            System.out.println("Enter password: ");
            password = scanner.nextLine();
            enterBirthDate();
            addUserToServer();
            askLoginData();
        } else {
            System.out.println("Enter username: ");
            username = scanner.nextLine();
            System.out.println("Enter password: ");
            password = scanner.nextLine();
            if (!verifyLogin(username, password)){
                askLoginData();
            }
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
        System.out.println("Your login credentials are now saved on the server!");
    }

    private boolean verifyLogin(String usrName, String psWord) {
        currRequest = new VerifyUserRequest(usrName, psWord, username);
        boolean verification;

        try {
            oos.writeObject(currRequest);
            verification = (boolean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (verification) {
            System.out.println("Welcome " + username);
            return true;
        } else {
            System.out.println("Wrong!");
            return false;
        }
    }

    private void listener() {
        checkNoti();

        while (true) {
            menu();
        }
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
                    int year = parseInt(date.substring(0, 4));
                    int month = parseInt(date.substring(5, 7));
                    int day = parseInt(date.substring(8, 10));
                    if (year >= 1900 && year <= 2100 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                        Date birthDate = sdf.parse(date);
                        validBirthDate = true;

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
        for (int i = 0; i < 20; i++) {
            System.out.print(".");
        }
        System.out.println();
    }




}
