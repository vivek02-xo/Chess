package game;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Engine {
    public Board board;
    public boolean whiteToMove;

    public Engine() {
        // board is set up already
        board = new Board();
        whiteToMove = true;
    }

    public void printAllMoves() {
        List<Move> Allmoves = MoveGenerator.Moves(board, whiteToMove);
        List<Move> legalMoves = new ArrayList<>();

        for (Move m : Allmoves) {
            board.makeMove(m);
            if (!board.isInCheck(!board.whiteToMove)) {
                legalMoves.add(m);
            }
            board.undoMove();
        }

        for(Move m : legalMoves){
            System.out.println(m);
        }
            

        System.out.println("Total Moves = " + legalMoves.size());
    }

    // Perft test
    public long perft(Board board, int depth) {
        if (depth == 0) return 1;
        long nodes = 0;
        List<Move> moves = MoveGenerator.Moves(board, board.whiteToMove);
        for (Move m : moves) {
            board.makeMove(m);
            if (!board.isInCheck(!board.whiteToMove)) {
                nodes += perft(board, depth - 1);
            }
            board.undoMove();
        }
        return nodes;
    }
    public long perftDivide(Board board, int depth) {
        long total = 0;
        List<Move> moves = MoveGenerator.Moves(board, board.whiteToMove);
        for (Move m : moves) {
            board.makeMove(m);
            long nodes = 0;
            if (!board.isInCheck(!board.whiteToMove)) {
                nodes = perft(board, depth - 1);
                total += nodes;
                System.out.println(m + ": " + nodes);
            }
            board.undoMove();
        }
        System.out.println("Total: " + total);
        return total;
    }
    public List<Move> generateLegalMoves() {
        List<Move> allMoves = MoveGenerator.Moves(board, whiteToMove);
        List<Move> legalMoves = new ArrayList<>();

        for (Move m : allMoves) {
            board.makeMove(m);
            if (!board.isInCheck(!board.whiteToMove)) {
                legalMoves.add(m);
            }
            board.undoMove();
        }
        return legalMoves;
    }


    // --- Evaluation ---
    public int evaluate(Board board) {
        int score = 0;
        for (int i = 0; i < 64; i++) {
            int sq = board.squares[i];
            switch (sq) {
                case 1: score += 100; break; // BP
                case 2: score += 320; break; // BN
                case 3: score += 330; break; // BB
                case 4: score += 900; break; // BQ
                case 5: score += 20000; break; // BK
                case 6: score += 500; break; // BR
                case 7: score -= 100; break; // WP
                case 8: score -= 320; break; // WN
                case 9: score -= 330; break; // WB
                case 10: score -= 900; break; // WQ
                case 11: score -= 20000; break; // WK
                case 12: score -= 500; break; // WR
                default: break;
            }
        }
        return board.whiteToMove ? score : -score;
    }

    // --- Pick best move (simple) ---
    public Move pickBestMove() {
        List<Move> moves = generateLegalMoves();
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move m : moves) {
            board.makeMove(m);
            int score = evaluate(board);
            board.undoMove();

            if (score > bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }

        return bestMove;
    }

    // --- Tiny CLI loop ---
    // Helper: convert square like "e2" to index 0..63
    private int squareFromString(String s) {
        if (s.length() != 2) return -1;
        int file = s.charAt(0) - 'a'; // 'a'->0, 'b'->1, ...
        int rank = 8 - (s.charAt(1) - '0'); // '1'->7, '2'->6, ... '8'->0
        if (file < 0 || file > 7 || rank < 0 || rank > 7){
            return -1;
        }
        return rank * 8 + file;
    }
    public void runCLI() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            board.printBoard();
            System.out.println("Enter move (e.g., e2e4) or 'quit':");
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("quit")) {
                break;
            }
            if (input.length() < 4) {
                System.out.println("Invalid input, try again.");
                continue;
            }

            int from = squareFromString(input.substring(0,2));
            int to   = squareFromString(input.substring(2,4));
            if (from == -1 || to == -1) {
                System.out.println("Invalid squares, try again.");
                continue;
            }

            // match move in legal moves
            Move selected = null;
            for (Move m : generateLegalMoves()) {
                if (m.from == from && m.to == to) {
                    selected = m;
                    break;
                }
            }

            if (selected != null) {
                board.makeMove(selected);
                whiteToMove = !whiteToMove;
            } else {
                System.out.println("Illegal move, try again.");
            }
        }
        sc.close();
    }


    public static void main(String[] args) {
        Engine e = new Engine();
        e.board.printBoard();


        for (int d = 1; d <= 4; d++) {
            long nodes = e.perft(e.board, d);
            System.out.println("Depth " + d + " = " + nodes);
        }

        // --- Best move demo ---
        Move best = e.pickBestMove();
        System.out.println("Best move at start: " + best);

        // --- Optional CLI ---
        e.runCLI();
    }
}
