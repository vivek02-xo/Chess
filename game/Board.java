package game;


import java.util.ArrayDeque;
import java.util.Deque;

public class Board {

    // 0-63 squares
    public int[] squares = new int[64];
    // turn (for each player to move)
    public boolean whiteToMove = true;
    // en-passant target square (-1 if none)
    public int enPassantSquare = -1;

    // WK, WQ, BK, BQ (1 if right available)
    public int castlingRight = 0b1111; 
    // halfmove clock (for 50-move rule)
    public int halfMoveClock = 0;
    // fullmove number (starts at 1, increments after Black)
    public int fullMoveNum = 1;

    // history stack for undo
    private Deque<BoardState> history = new ArrayDeque<>();

    // store pieces in 0-63 squares
    public Board() {
        // ordinal values of Piece enum (ordinal change ENUM to int )
        // initialize empty board
        for (int i = 0; i < 64; i++){
            squares[i] = Piece.EMPTY.ordinal();
        }
        // seting up black pieces
        squares[0] = Piece.BR.ordinal();
        squares[1] = Piece.BN.ordinal();
        squares[2] = Piece.BB.ordinal();
        squares[3] = Piece.BQ.ordinal();
        squares[4] = Piece.BK.ordinal();
        squares[5] = Piece.BB.ordinal();
        squares[6] = Piece.BN.ordinal();
        squares[7] = Piece.BR.ordinal();
        for (int i = 8; i < 16; i++) squares[i] = Piece.BP.ordinal();

        // setting up white pieces
        for (int i = 48; i < 56; i++) squares[i] = Piece.WP.ordinal();
        squares[56] = Piece.WR.ordinal();
        squares[57] = Piece.WN.ordinal();
        squares[58] = Piece.WB.ordinal();
        squares[59] = Piece.WQ.ordinal();
        squares[60] = Piece.WK.ordinal();
        squares[61] = Piece.WB.ordinal();
        squares[62] = Piece.WN.ordinal();
        squares[63] = Piece.WR.ordinal();

        // setting first move to white
        whiteToMove = true;
        // setting en-passant square to none
        enPassantSquare = -1;
        // setting castling rights to all available
        castlingRight = 0b1111;
        // setting 50-move rule count to 0
        halfMoveClock = 0;
        // setting full move number to 1
        fullMoveNum = 1;
    }


    // data to store for undoing a move
    public static class BoardState {
        int from;
        int to;
        int movedPiece;
        int capPiece;
        int promotion;
        int preEnPassant;        
        int prevCastlingRight;
        int prevHalfMove;
        int prevFullMove;
        boolean prevWhiteToMove;
        Flag flag;
    }



    public void makeMove(Move m) {
        BoardState st = new BoardState();
        st.from = m.from;
        st.to = m.to;
        st.movedPiece = squares[m.from];
        st.capPiece = squares[m.to];
        st.promotion = m.promoPiece;
        st.preEnPassant = this.enPassantSquare;
        st.prevCastlingRight = this.castlingRight;
        st.prevHalfMove = this.halfMoveClock;
        st.prevFullMove = this.fullMoveNum;
        st.prevWhiteToMove = this.whiteToMove;
        st.flag = m.flag;

        history.push(st);

        // move
        squares[m.to] = squares[m.from];
        squares[m.from] = Piece.EMPTY.ordinal();

        // en-passant capture (remove the captured pawn behind the target)
        if (m.flag == Flag.EN_PASSANT) {
            int capSq = st.prevWhiteToMove ? m.to + 8 : m.to - 8;
            st.capPiece = squares[capSq];
            squares[capSq] = Piece.EMPTY.ordinal();
        }

        // promotion
        if (m.promoPiece != -1) {
            squares[m.to] = m.promoPiece;
        }

        // double pawn push -> set enPassantSquare (the square behind pawn)
        if (m.flag == Flag.DPP) {
            this.enPassantSquare = (m.from + m.to) / 2;
        } else {
            this.enPassantSquare = -1;
        }

        // castling rook moves
        if (m.flag == Flag.KC) {
            // kingside: move rook next to king
            if (m.to == 62) { // white O-O
                squares[61] = squares[63];
                squares[63] = Piece.EMPTY.ordinal();
            } else if (m.to == 6) { // black O-O
                squares[5] = squares[7];
                squares[7] = Piece.EMPTY.ordinal();
            }
        } else if (m.flag == Flag.QC) {
            if (m.to == 58) { // white O-O-O
                squares[59] = squares[56];
                squares[56] = Piece.EMPTY.ordinal();
            } else if (m.to == 2) { // black O-O-O
                squares[3] = squares[0];
                squares[0] = Piece.EMPTY.ordinal();
            }
        }

        // update castling rights: clear bits when king or rook moves / captured
        // When king moves, clear both bits for that color
        if (st.movedPiece == Piece.WK.ordinal()) {
            castlingRight &= ~((1 << 0) | (1 << 1)); // clear white KQ
        }
        if (st.movedPiece == Piece.BK.ordinal()) {
            castlingRight &= ~((1 << 2) | (1 << 3)); // clear black KQ
        }
        // if rook a1/h1 or a8/h8 moves or is captured, clear corresponding bit
        if (st.from == 56 || st.to == 56) castlingRight &= ~(1 << 1); // white a1 (Q)
        if (st.from == 63 || st.to == 63) castlingRight &= ~(1 << 0); // white h1 (K)
        if (st.from == 0 || st.to == 0) castlingRight &= ~(1 << 3);   // black a8 (Q)
        if (st.from == 7 || st.to == 7) castlingRight &= ~(1 << 2);   // black h8 (K)

        // halfmove clock
        if (st.movedPiece == Piece.WP.ordinal() || st.movedPiece == Piece.BP.ordinal() || st.capPiece != Piece.EMPTY.ordinal()) {
            this.halfMoveClock = 0;
        } else {
            this.halfMoveClock++;
        }

        // fullmove number increments after black moves
        if (!this.whiteToMove) this.fullMoveNum++;

        // flip side
        this.whiteToMove = !this.whiteToMove;
    }

    public void undoMove() {
        if (history.isEmpty()) return;
        BoardState st = history.pop(); // restore real previous state

        // restore meta
        this.whiteToMove = st.prevWhiteToMove;
        this.enPassantSquare = st.preEnPassant;
        this.castlingRight = st.prevCastlingRight;
        this.halfMoveClock = st.prevHalfMove;
        this.fullMoveNum = st.prevFullMove;

        // restore pieces
        squares[st.from] = st.movedPiece;
        squares[st.to] = st.capPiece;

        // undo en-passant (restore captured pawn and clear target)
        if (st.flag == Flag.EN_PASSANT) {
            int capSq = st.prevWhiteToMove ? st.to + 8 : st.to - 8;
            squares[capSq] = st.capPiece;
            squares[st.to] = Piece.EMPTY.ordinal();
        }

        // undo castling rook move
        if (st.flag == Flag.KC) {
            if (st.to == 62) { // white
                squares[63] = squares[61];
                squares[61] = Piece.EMPTY.ordinal();
            } else if (st.to == 6) { // black
                squares[7] = squares[5];
                squares[5] = Piece.EMPTY.ordinal();
            }
        } else if (st.flag == Flag.QC) {
            if (st.to == 58) {
                squares[56] = squares[59];
                squares[59] = Piece.EMPTY.ordinal();
            } else if (st.to == 2) {
                squares[0] = squares[3];
                squares[3] = Piece.EMPTY.ordinal();
            }
        }

        // undo promotion: put pawn back on from square (movedPiece holds pawn ordinal)
        if (st.promotion != -1) {
            if (st.movedPiece == Piece.WP.ordinal()) squares[st.from] = Piece.WP.ordinal();
            else if (st.movedPiece == Piece.BP.ordinal()) squares[st.from] = Piece.BP.ordinal();
        }
    }

    // ------------------ helpers ------------------

    /** find king square for color */
    public int findKingSq(boolean white) {
        int kingPiece = white ? Piece.WK.ordinal() : Piece.BK.ordinal();
        for (int i = 0; i < 64; i++) if (squares[i] == kingPiece) return i;
        return -1;
    }

    /** is color 'white' in check? (uses isUnderAttack, NOT MoveGenerator) */
    public boolean isInCheck(boolean white) {
        int kingSquare = findKingSq(white);
        if (kingSquare == -1) return false;
        return isUnderAttack(kingSquare, !white);
    }

    /**
     * isUnderAttack(square, byWhite) — checks pawns/knights/kings/sliders without using MoveGenerator.
     * returns true if ANY piece of side 'byWhite' attacks 'square'
     */
    public boolean isUnderAttack(int square, boolean byWhite) {
        if (square < 0 || square >= 64) return false;

        int sqRow = square / 8;
        int sqCol = square % 8;

        int pawn = byWhite ? Piece.WP.ordinal() : Piece.BP.ordinal();
        int knight = byWhite ? Piece.WN.ordinal() : Piece.BN.ordinal();
        int bishop = byWhite ? Piece.WB.ordinal() : Piece.BB.ordinal();
        int rook = byWhite ? Piece.WR.ordinal() : Piece.BR.ordinal();
        int queen = byWhite ? Piece.WQ.ordinal() : Piece.BQ.ordinal();
        int king = byWhite ? Piece.WK.ordinal() : Piece.BK.ordinal();

        // pawn attacks (sources)
        int src = square + (byWhite ? 7 : -7);
        if (src >= 0 && src < 64) {
            int srcCol = src % 8;
            if (Math.abs(srcCol - sqCol) == 1 && squares[src] == pawn) return true;
        }
        src = square + (byWhite ? 9 : -9);
        if (src >= 0 && src < 64) {
            int srcCol = src % 8;
            if (Math.abs(srcCol - sqCol) == 1 && squares[src] == pawn) return true;
        }

        // knight attacks
        int[] knightOffsets = {17, 15, 10, 6, -6, -10, -15, -17};
        for (int off : knightOffsets) {
            int s = square + off;
            if (s < 0 || s >= 64) continue;
            int rDiff = Math.abs((s / 8) - sqRow);
            int cDiff = Math.abs((s % 8) - sqCol);
            if (!((rDiff == 2 && cDiff == 1) || (rDiff == 1 && cDiff == 2))) continue;
            if (squares[s] == knight) return true;
        }

        // king adjacency
        int[] kingOffsets = {-9, -8, -7, 1, 9, 8, 7, -1};
        for (int off : kingOffsets) {
            int s = square + off;
            if (s < 0 || s >= 64) continue;
            int rDiff = Math.abs((s / 8) - sqRow);
            int cDiff = Math.abs((s % 8) - sqCol);
            if (rDiff > 1 || cDiff > 1) continue;
            if (squares[s] == king) return true;
        }

        // sliding orthogonals (rook/queen)
        int[] orth = {-8, 8, -1, 1};
        for (int off : orth) {
            int s = square;
            while (true) {
                s += off;
                if (s < 0 || s >= 64) break;
                // horizontal wrap check
                if (off == -1 || off == 1) {
                    int prevCol = (s - off) % 8;
                    if (prevCol < 0) prevCol += 8;
                    int curCol = s % 8;
                    if (Math.abs(curCol - prevCol) != 1) break;
                }
                int piece = squares[s];
                if (piece == Piece.EMPTY.ordinal()) continue;
                if (piece == rook || piece == queen) return true;
                break;
            }
        }

        // diagonal (bishop/queen)
        int[] diag = {-9, -7, 7, 9};
        for (int off : diag) {
            int s = square;
            while (true) {
                s += off;
                if (s < 0 || s >= 64) break;
                int prevCol = (s - off) % 8;
                if (prevCol < 0) prevCol += 8;
                int curCol = s % 8;
                if (Math.abs(curCol - prevCol) != 1) break;
                int piece = squares[s];
                if (piece == Piece.EMPTY.ordinal()) continue;
                if (piece == bishop || piece == queen) return true;
                break;
            }
        }

        return false;
    }

    /** whether both castling bits for that color are cleared => king moved (or rights lost) */
    public boolean kingMoved(boolean white) {
        if (white) return (castlingRight & 0b0011) == 0;
        else return (castlingRight & 0b1100) == 0;
    }

    /** whether rook on given side is gone/moved (true if rook bit cleared) */
    public boolean rookMoved(boolean isWhite, boolean kingSide) {
        if (isWhite) {
            return kingSide ? ((castlingRight & (1 << 0)) == 0) : ((castlingRight & (1 << 1)) == 0);
        } else {
            return kingSide ? ((castlingRight & (1 << 2)) == 0) : ((castlingRight & (1 << 3)) == 0);
        }
    }

    /** returns true if all squares strictly between start and end are EMPTY (used for castling checks) */
    public boolean isEmptyBetween(int start, int end) {
        int step = (end > start) ? 1 : -1;
        for (int s = start + step; s != end; s += step) {
            if (s < 0 || s >= 64) return false;
            if (squares[s] != Piece.EMPTY.ordinal()) return false;
        }
        return true;
    }

    public void printBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int idx = r * 8 + c;
                String piece = Piece.values()[squares[idx]].name();
                System.out.print((piece.equals("EMPTY") ? "--" : piece) + " ");
            }
            System.out.println();
        }
        System.out.println("White to move: " + whiteToMove);
        System.out.println("Castling rights: " + Integer.toBinaryString(castlingRight));
        System.out.println("En-passant square: " + enPassantSquare);
        System.out.println("Halfmove clock: " + halfMoveClock);
        System.out.println("Fullmove number: " + fullMoveNum);
        System.out.println();
    }

    public int squareFromString(String s) {
        if (s.length() != 2) return -1;
        int file = s.charAt(0) - 'a';
        int rank = 8 - (s.charAt(1) - '0');
        if (file < 0 || file > 7 || rank < 0 || rank > 7) return -1;
        return rank * 8 + file;
    }
    public String indexToSquare(int index) {
        int row = index / 8;
        int col = index % 8;
        char file = (char) ('a' + col);
        char rank = (char) ('8' - row);
        return "" + file + rank;
    }


}
