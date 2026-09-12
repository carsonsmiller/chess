package chess;

import java.util.Collection;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor Color;
    private PieceType Type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.Color = pieceColor;
        this.Type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return Color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return Type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int row = myPosition.getRow();
        int column = myPosition.getColumn();

        switch (Type) {
            case KING:
                addKingMoves(moves, board, myPosition, row, column);
                break;

            case QUEEN:
                addSlidingMoves(moves, board, myPosition, row, column, new int[][]{{1, 0},{-1, 0},{0, 1},{0, -1},{1, 1},{1, -1},{-1, 1},{-1, -1}});
                break;

            case BISHOP:
                addSlidingMoves(moves, board, myPosition, row, column, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
                break;

            case ROOK:
                addSlidingMoves(moves, board, myPosition, row, column, new int[][]{{1, 0},{-1, 0},{0, 1},{0, -1}});
                break;

            case KNIGHT:
                addKnightMoves(moves, board, myPosition, row, column);
                break;

            case PAWN:
                addPawnMoves(moves, board, myPosition, row, column);
                break;
        }

        return moves;
    }

    private void addKingMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition start, int row, int column) {
        for (int rowChange = -1; rowChange <= 1; rowChange++) {
            for (int columnChange = -1; columnChange <= 1; columnChange++) {
                if (rowChange == 0 && columnChange == 0) {
                    continue;
                }

                addMoveifValid(moves, board, start, row + rowChange, column + columnChange);
            }
        }
    }

    private void addKnightMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition start, int row, int column) {
        int[][] directions = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};

        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newColumn = column + direction[1];

            addMoveifValid(
                    moves,
                    board,
                    start,
                    newRow,
                    newColumn
            );
            
        }
    }

    private void addSlidingMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition start, int row, int column, int[][] directions) {
        for (int[] direction : directions) {
            int currentRow = row + direction[0];
            int currentColumn = column + direction[1];

            while (currentRow >= 1 && currentRow <= 8 && currentColumn >= 1 && currentColumn <= 8) {
                ChessPosition end = new ChessPosition(currentRow, currentColumn);
                ChessPiece destination = board.getPiece(end);

                if (destination == null) {
                    moves.add(new ChessMove(start, end, null));
                } else {
                    if (destination.getTeamColor() != getTeamColor()) {
                        moves.add(new ChessMove(start, end, null));
                    }

                    break;
                }

                currentRow += direction[0];
                currentColumn += direction[1];
            }
        }
    }

    private void addPawnMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition start, int row, int column) {
        int direction;

        if (getTeamColor() == ChessGame.TeamColor.WHITE) {
            direction = 1;
        } else {
            direction = -1;
        }

        int oneForwardRow = row + direction;

        if (oneForwardRow >= 1 && oneForwardRow <= 8) {
            ChessPosition oneForward = new ChessPosition(oneForwardRow, column);
            if (board.getPiece(oneForward) == null) {
                addPawnMove(moves, start, oneForward);

                boolean onStartingRow = (getTeamColor() == ChessGame.TeamColor.WHITE && row == 2) || (getTeamColor() == ChessGame.TeamColor.BLACK && row == 7);

                int twoForwardRow = row + 2 * direction;

                if (onStartingRow && board.getPiece(new ChessPosition(twoForwardRow, column)) == null) {
                    ChessPosition twoForward = new ChessPosition(twoForwardRow, column);
                    moves.add(new ChessMove(start, twoForward, null));
                }
            }
        }

        int [] captureColumns = {column - 1, column + 1};

        for (int captureColumn : captureColumns) {
            int captureRow = row + direction;
            if (captureRow < 1 || captureRow > 8 || captureColumn < 1 || captureColumn > 8) {
                continue;
            }

            ChessPosition capturePosition = new ChessPosition(captureRow, captureColumn);
            ChessPiece destination = board.getPiece(capturePosition);

            if (destination != null && destination.getTeamColor() != getTeamColor()) {
                addPawnMove(moves, start, capturePosition);
            }
        }
    }

    private void addPawnMove(Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {
        int promotionRow;

        if (getTeamColor() == ChessGame.TeamColor.WHITE) {
            promotionRow = 8;
        } else {
            promotionRow = 1;
        }

        if (end.getRow() == promotionRow) {
            moves.add(new ChessMove(start, end, PieceType.QUEEN));
            moves.add(new ChessMove(start, end, PieceType.KNIGHT));
            moves.add(new ChessMove(start, end, PieceType.ROOK));
            moves.add(new ChessMove(start, end, PieceType.BISHOP));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
    private void addMoveifValid(Collection<ChessMove> moves, ChessBoard board, ChessPosition start, int row, int column) {
        if (row < 1 || row > 8 || column < 1 || column > 8) {
            return;
        }

        ChessPosition end = new ChessPosition(row, column);
        ChessPiece destination = board.getPiece(end);

        if (destination == null || destination.getTeamColor() != getTeamColor()) {
            moves.add(new ChessMove(start, end, null));
        }
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return Color == that.Color && Type == that.Type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Color, Type);
    }
}
