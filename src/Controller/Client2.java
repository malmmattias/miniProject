package Controller;

import java.util.Objects;
import java.util.Scanner;

public class Client2 {

    private String username;

    public String askLoginData() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String input = scanner.nextLine();
        if (Objects.equals(input, "no")) {
            System.out.println("Enter username:");
            username = scanner.nextLine(); // Read the username if input is "no"
        } else {
            username = input; // Otherwise, set the username to the input
        }
        return "done";
    }


    public String getUsername() {
        return username;
    }
}

