package client;

import chess.ChessGame;
import model.AuthData;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private final Scanner input;
    private final ChessBoardPrinter boardPrinter;
    private AuthData currentUser;
    private List<ServerFacade.GameInfo> displayedGames = new ArrayList<>();
}
