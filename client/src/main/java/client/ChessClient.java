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
    private boolean running = true;

    public ChessClient(ServerFacade server, Scanner input) {
        this.server = server;
        this.input = input;
        this.boardPrinter = new ChessBoardPrinter();
    }

    public void run() {
        System.out.println("Welcome to the Chess Client");
        while (running) {
            if (currentUser == null) { preloginLoop(); }
            else { postloginLoop(); }
        }
        System.out.println("See ya next time!");
    }

    private void preloginLoop() {
        System.out.print("[prelogin] >>> ");
        String command = readLine();
        if (command == null) { running = false; return; }
        switch (command.trim().toLowerCase(Locale.ROOT)) {
            case "help" -> printPreloginHelp();
            case "quit", "exit" -> running = false;
            case "login" -> login();
            case "register" -> register();
            case "" -> { }
            default -> System.out.println("Don't know that command bud. Type help to see available commands.");
        }
    }

    private void postloginLoop() {
        System.out.print("[" + currentUser.username() + "] [postLogin] >>> ");
        String command = readLine();
        if (command == null) { running = false; return; }
        switch (command.trim().toLowerCase(Locale.ROOT)) {
            case "help" -> printPostloginHelp();
            case "logout" -> logout();
            case "create", "create game" -> createGame();
            case "list", "list games" -> listGames();
            case "play", "play game", "join", "join game" -> playGame();
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
            System.out.println("Game created. Use 'list' to see it and choose whether to play or observe.");
        } catch (ServerFacadeException | IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void listGames() {
        try {
            displayedGames = server.listGames(currentUser.authToken());
            if (displayedGames.isEmpty()) {
                System.out.println("There are no games yet. Use 'create' to make one.");
                return;
            }
            System.out.println("Games:");
            for (int i = 0; i < displayedGames.size(); i++) {
                ServerFacade.GameInfo game = displayedGames.get(i);
                String white = game.whiteUsername() == null ? "(open)" : game.whiteUsername();
                String black = game.blackUsername() == null ? "(open)" : game.blackUsername();
                System.out.printf("%d. %s | White: %s | Black: %s%n", i + 1, game.gameName(), white, black);
            }
        } catch (ServerFacadeException ex) {
            displayedGames.clear();
            System.out.println(ex.getMessage());
        }
    }

    private void playGame() {
        ServerFacade.GameInfo game = chooseGame();
        if (game == null) { return; }
        String colorInput = prompt("Play as White or Black? ");
        if (colorInput == null) {running = false; return; }
        String color = colorInput.trim().toUpperCase(Locale.ROOT);
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            System.out.println("You have to pick either black or white, red is for checkers");
            return;
        }
        try {
            server.joinGame(currentUser.authToken(), color, game.gameID());
            System.out.println("You joined '" + game.gameName() + "' as " + color + ".");
            boardPrinter.print(new ChessGame(), ChessGame.TeamColor.valueOf(color));
            System.out.println("Gameplay awaiting implementation");
        } catch (ServerFacadeException | IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void observeGame() {
        ServerFacade.GameInfo game = chooseGame();
        if (game == null) { return; }
        System.out.println("Observing '" + game.gameName() + "'.");
        boardPrinter.print(new ChessGame(), ChessGame.TeamColor.WHITE);
        System.out.println("Full observer function awaiting implementation");
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
        System.out.println(" create   - Create a game");
        System.out.println(" list     - list games and their players");
        System.out.println(" join     - Join a game as White or Black");
        System.out.println(" observe  - View a game's initial board");
        System.out.println(" logout   - Log out and return to start");
        System.out.println(" quit    - Exit the chess client");
    }

    private ServerFacade.GameInfo chooseGame() {
        if (displayedGames.isEmpty()) {
            System.out.println("Use 'list' first so you can choose a game by number");
            return null;
        }
        String selection = prompt("Game number: ");
        if (selection == null) { running = false; return null; }
        try {
            int number = Integer.parseInt(selection.trim());
            if (number < 1 || number > displayedGames.size()) {
                System.out.println("You must choose a number from 1 to " + displayedGames.size() + ".");
                return null;
            }
            return displayedGames.get(number - 1);
        } catch (NumberFormatException ex) {
            System.out.println("Please enter a whole number from the list");
            return null;
        }
    }
}
