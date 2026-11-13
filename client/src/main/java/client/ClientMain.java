package client;

import chess.*;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 1) {
            System.out.println("Usage: ClientMain [server-port]");
            return;
        }
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("The server port must be a number.");
                return;
            }
        }
        try {
            new ChessClient(new ServerFacade(port), new Scanner(System.in)).run();
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid server port. Choose a number from 1 to 65535");
        }
    }
}
