package client;

import chess.ChessGame;
import model.AuthData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private final Scanner input;
    private final ChessBoardPrinter boardPrinter;
    private AuthData currentUser;
    private List<ServerFacade.GameInfo> displayedGames = new ArrayList<>();

    public ChessClient(ServerFacade server, Scanner input) {
        this.server = server;
        this.input = input;
        this.boardPrinter = new ChessBoardPrinter();
    }

    public void run() {
        System.out.println("Welcome to the Chess Client");
        while (running) {
            if (currentUser == null) preloginLoop();
            else postloginLoop();
        }
        System.out.println("See ya next time!");
    }

    private void preloginLoop() {
        System.out.print("[prelogin] >>> ");
        String command = readLine();
        if (command == null) { running = false; return; }
        switch (command.trim().toLowerCase()) {
            case "help" -> printPreloginHelp();
            case "quite", "exit" -> running = false;
            case "login" -> login();
            case "register" -> register();
            case "" -> { }
            default -> System.out.println("Don't know that command bud. Type help to see available commands.");
        }
    }

    private void postLoginLoop() {
        System.out.print("[" + currentUser.username() + "] [postLogin] >>> ");
        String command = readLine();
        if (command == null) { running = false; return; }
        switch (command.trim().toLowerCase()) {
            case "help" -> printPostloginHelp();
            case "logout" -> logout();
            case "create", "create game" -> createGame();
            case "list", "list games" -> listGames();
            case "play", "play game" -> playGame();
            case "observe", "observe game" -> observeGame();
            case "quit", "exit" -> running = false;
            case "" -> { }
            default -> System.out.println("I don't know that command. Type help to see available commands");
        }
    }

    private void login() {
        String username = prompt("Username: ");
        String password = prompt("Password: ");
        if (username == null || password == null) { running = false; return; }
        try {
            currentUser = server.login(username, password);
            System.out.println("Welcome back, " + currentUser.username());
        } catch (ServerFacadeException | IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void logout() {
        try {
            server.logout(currentUser.authToken());
            currentUser = null;
            displayedGames.clear();
            System.out.println("You're logged out.");
        } catch (ServerFacadeException ex) {
            System.out.println(ex.getMessage());
            currentUser = null;
        }
    }

    private void register() {
        String username = prompt("Username: ");
        String password = prompt("Password: ");
        String email = prompt("Email: ");
        if (username == null || password == null || email == null) { running = false; return; }
        try {
            currentUser = server.register(username, password, email);
            System.out.println("Account created. Welcome, " + currentUser.username());
        } catch (ServerFacadeException | IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void createGame() {
        String gameName = prompt("Game name: ");
        if (gameName == null) { running = false; return; }
        try {
            server.createGame(currentUser.authToken(), gameName);
            System.out.println("Game crated. Use 'list' to see it and choose whether to play or observer.");
        } catch (ServerFacadeException | IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return readLine();
    }

    private String readLine() {
        return input.hasNextLine() ? input.nextLine() : null;
    }

    private void printPreloginHelp() {
        System.out.println("Available commands:");
        System.out.println(" help     - Show this help menu");
        System.out.println(" register - Create an account");
        System.out.println(" login    - Log in to an existing account");
        System.out.println(" quit     - Exit the chess client");
    }

    private void printPostloginHelp() {
        System.out.println("Available commands:");
        System.out.println(" help     - Show this help menu");
        System.out.println(" craete   - Create a game");
        System.out.println(" list     - list games and their players");
        System.out.println(" play     - Join a game as White or Black");
        System.out.println(" observe  - View a game's initial board");
        System.out.println(" logout   - Log out and return to start");
        System.out.println(" quite    - Exit the chess client");
    }
}
