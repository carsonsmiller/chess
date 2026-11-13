package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.EscapeSequences;

public class ChessBoardPrinter {
    private static final String LIGHT = EscapeSequences.SET_BG_COLOR_WHITE;
    private static final String DARK = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
    private static final String RESET = EscapeSequences.RESET_BG_COLOR;

    private String pieceSymbol(ChessPiece piece) {
        if (piece == null) { return "   "; }
        String foreground = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                ? EscapeSequences.SET_TEXT_COLOR_RED : EscapeSequences.SET_TEXT_COLOR_BLUE;
        String symbol = switch (piece.getTeamColor()) {
            case WHITE -> switch (piece.getPieceType()) {
                case KING -> EscapeSequences.WHITE_KING;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case PAWN -> EscapeSequences.WHITE_PAWN;
            };
            case BLACK -> switch (piece.getPieceType()) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case PAWN -> EscapeSequences.BLACK_PAWN;
            };
        };
        return foreground + symbol + EscapeSequences.RESET_TEXT_COLOR;
    }

    public void print(ChessGame game, ChessGame.TeamColor perspective) {
        if (game == null || perspective == null) {
            throw new IllegalArgumentException("A game and perspective are required");
        }
        ChessBoard board = game.getBoard();
        boolean blackPerspective = perspective == ChessGame.TeamColor.BLACK;
        System.out.println();
        System.out.println("      " + (blackPerspective ? "h  g  f  e  d  c  b  a" : "a  b  c  d  e  f  g  h"));
        for (int displayRow = 0; displayRow < 8; displayRow++) {
            int row = blackPerspective ? displayRow + 1 : 8 - displayRow;
            System.out.print("   " + row + " ");
            for (int displayColumn = 0; displayColumn < 8; displayColumn++) {
                int column = blackPerspective ? 8 - displayColumn : displayColumn + 1;
                ChessPiece piece = board.getPiece(new ChessPosition(row, column));
                String background = (row + column) % 2 == 1 ? LIGHT : DARK;
                System.out.print(background + pieceSymbol(piece) + RESET);
            }
            System.out.println(" " + row);
        }
        System.out.println("      " + (blackPerspective ? "h  g  f  e  d  c  b  a" : "a  b  c  d  e  f  g  h"));
        System.out.println();
    }
}