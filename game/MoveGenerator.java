package game;

import java.util.ArrayList;
import java.util.List;


public class MoveGenerator {
    // Generate all possible moves for the current player
    public static List<Move> Moves(Board board , boolean whiteToMove){
        List<Move> moves = new ArrayList<>();

        for(int i = 0;i<64;i++){
            Piece p = Piece.values()[board.squares[i]];
            if(p == Piece.EMPTY){
                continue;
            }
            boolean isWhitePiece = p.name().startsWith("W");
            // checking which color to move
            if(isWhitePiece != whiteToMove){
                continue;
            }
            // Get piece-specific moves
            moves.addAll(PieceMoves(board, p, i, isWhitePiece));

        }
        return moves;
    }
    // Generate piece specific moves
    public static List<Move> PieceMoves(Board board, Piece p , int pos, boolean isWhite){
        if(p == Piece.WP || p == Piece.BP){
            return PawnMoves(board,p, pos, isWhite);
        }else if(p == Piece.WK || p == Piece.BK){
            return KingMoves(board,p, pos, isWhite);
        }else if(p == Piece.WQ || p == Piece.BQ){
            return QueenMoves(board, p, pos, isWhite);
        }else if(p == Piece.WR || p == Piece.BR){
            return RookMoves(board,p, pos, isWhite);
        }else if(p == Piece.WB || p == Piece.BB){
            return BishopMoves(board ,p, pos, isWhite);
        }else{
            return KnightMoves(board ,p, pos , isWhite);
        }
    }

    // Kings move
    public static List<Move> KingMoves(Board board ,Piece p, int pos , boolean isWhite){
        List<Move> moves = new ArrayList<>();

        // direction where king can move,I collecting idx of board array where king can move in dir array...
        int[] dir = {-9 , -8 , -7 , 1, 9 , 8, 7 , -1};

        // curr row and col of king
        int row = pos/8;
        int col = pos % 8;

        for(int x : dir){
            int tar = pos + x;
            int tarRow = tar / 8;
            int tarCol = tar % 8;

            if(tar < 0 || tar >= 64){
                continue;
            }
            // this check the king doesn’t teleport horizontally across the board.
            if(Math.abs(tarCol - col) > 1){
                continue;
            }


            Piece tarPiece = Piece.values()[board.squares[tar]];


            if(tarPiece == Piece.EMPTY){
                moves.add(new Move(pos, tar, p.ordinal(), -1, -1, Flag.NL));
            }else{
                // if NOt EMPTY then we check piece is diff color if color is diff then we can move our king...

                boolean tarIsWhite = tarPiece.name().startsWith("W");

                if(tarIsWhite != isWhite){
                    moves.add(new Move(pos, tar, p.ordinal(), tarPiece.ordinal(), -1, Flag.CAP));
                }
            }
        }

        // castling
        if (!board.kingMoved(isWhite) && !board.isInCheck(isWhite)) {
            // Kingside castle
            if (!board.rookMoved(isWhite, true) && board.isEmptyBetween(pos, pos + 3)) {
                if (!board.isUnderAttack(pos + 1, isWhite) && !board.isUnderAttack(pos + 2, isWhite)) {
                    moves.add(new Move(pos, pos + 2, p.ordinal(), -1, -1, Flag.KC));
                }
            }
            // Queenside castle
            if (!board.rookMoved(isWhite, false) && board.isEmptyBetween(pos, pos - 4)) {
                if (!board.isUnderAttack(pos - 1, isWhite) && !board.isUnderAttack(pos - 2, isWhite)) {
                    moves.add(new Move(pos, pos - 2, p.ordinal(), -1, -1, Flag.QC));
                }
            }
        }
        return moves;

    }
    // Queen Move
    public static List<Move> QueenMoves(Board board ,Piece p, int pos , boolean isWhite){
        List<Move> moves = new ArrayList<>();

        // direction where Queen can move,I collecting idx of board array where king can move in dir array...
        int dir[] = {-9 , -8 , -7 , 1, 9 , 8, 7 , -1};

        for(int x: dir){
            int tar = pos;
            while (true) { 
                tar += x;

                int tarRow = tar / 8;
                int tarCol = tar % 8;
                // early position on queen.
                int posRow = pos / 8;
                int posCol = pos % 8;

                if(Math.abs(tarCol - posCol) > 1 && (x == -1 || x == 1)){
                    break;
                }
                if(Math.abs(tarCol - posCol) != Math.abs(tarRow - posRow) && (x == -9 || x == 9 || x == -7 || x == 7)){
                    break;
                }
                if (tar < 0 || tar >= 64){
                    break;
                }

                Piece tarPiece = Piece.values()[board.squares[tar]];

                if(tarPiece == Piece.EMPTY){
                    moves.add(new Move(pos, tar, p.ordinal(), -1, -1, Flag.NL));
                }else{
                    boolean tarIsWhite =tarPiece.name().startsWith("W");
                    if(tarIsWhite != isWhite){
                        moves.add(new Move(pos, tar, p.ordinal(), tarPiece.ordinal(), -1, Flag.CAP));
                    }
                    break;
                }

                if(x == -1 || x == 1){
                    if(tarCol == 0 || x == 1){
                        break;
                    }
                    if(tarCol == 7 || x == -1){
                        break;
                    }
                }
            }
        }
        return moves;
    }

    // Rook Moves
    public static List<Move> RookMoves(Board board ,Piece p, int pos , boolean isWhite){
        List<Move> moves = new ArrayList<>();

        int dir[] = {-8 , 8 , -1 , 1};

        for(int x : dir){
            int tar = pos;
            while (true) { 
                tar += x;

                int tarRow = tar / 8;
                int tarCol = tar % 8;
                int posRow = pos/8;
                int posCol = pos % 8;

                if(Math.abs(tarCol -posCol) > Math.abs(tar - pos) % 8){
                    break;
                }
                if(tar < 0 || tar >= 64) break;

                if (x == -1 || x == 1) {
                    if(tarRow != posRow){
                        break;
                    }
                }

                Piece tarPiece = Piece.values()[board.squares[tar]];
                
                if(tarPiece == Piece.EMPTY){
                    moves.add(new Move(pos, tar , p.ordinal() , -1, -1 , Flag.NL));
                }else{
                    boolean isWhitePiece = tarPiece.name().startsWith("W");

                    if(isWhitePiece != isWhite){
                        moves.add(new Move(pos, tar, p.ordinal(), tarPiece.ordinal(), -1, Flag.CAP));
                        
                    }
                    break;
                }
                
            }
        }
        return moves;
    }

    // Bishop Moves
    public static List<Move> BishopMoves(Board board ,Piece p, int pos , boolean isWhite){
        List<Move> moves = new ArrayList<>();

        int dir[] = {-9 , 9 , -7, 7};

        for(int x: dir){
            int tar = pos;
            while(true){
                tar +=  x;

                int tarRow = tar /8;
                int tarCol = tar %8 ;
                int posRow = pos /8 ;
                int posCol = pos % 8;

                if(tar < 0 || tar >= 64){
                    break;
                }
                // When we move col then col and row must change if diff in col and row change is not same thats mean bishop jumped over the edge...
                if (Math.abs(tarCol - posCol) != Math.abs(tarRow - posRow)){
                    break;
                }

                Piece tarPiece = Piece.values()[board.squares[tar]];

                if(tarPiece == Piece.EMPTY){
                    moves.add(new Move(pos, tar , p.ordinal() ,-1 , -1, Flag.NL));
                }else{
                    boolean isWhitePiece = tarPiece.name().startsWith("W");

                    if(isWhitePiece != isWhite){
                        moves.add(new Move(pos , tar , p.ordinal() , tarPiece.ordinal(), -1 , Flag.CAP));
                    }
                    break;
                }
            }
        }
        return moves;
    }
    // Knight Moves
    public static List<Move> KnightMoves(Board board ,Piece p, int pos , boolean isWhite){
        List<Move> moves = new ArrayList<>();

        int dir[] = {10, -10, -6 , 6, -15, 15, -17, 17};

        int posRow = pos/ 8;
        int posCol = pos % 8;

        
        for(int x: dir){
            int tar = pos + x;

            int tarRow = tar / 8;
            int tarCol = tar % 8;

            if(tar < 0 || tar >= 64){
                continue;
            }
            if (!((Math.abs(tarRow - posRow) == 2 && Math.abs(tarCol - posCol) == 1) ||
            (Math.abs(tarRow - posRow) == 1 && Math.abs(tarCol - posCol) == 2))) {
                continue;
            }


            Piece tarPiece = Piece.values()[board.squares[tar]];

            if(tarPiece  == Piece.EMPTY){
                moves.add(new Move(pos, tar , p.ordinal(), -1 , -1 , Flag.NL));
            }else{
                boolean isWhitePiece = tarPiece.name().startsWith("W");
                if(isWhitePiece != isWhite){
                    moves.add(new Move(pos , tar , p.ordinal(), tarPiece.ordinal(), -1 , Flag.CAP));
                }
            }
            
        }
        return moves;
    }

    // Pawn Moves 

    public static List<Move> PawnMoves(Board board ,Piece p, int pos , boolean isWhite){
        List<Move> moves = new ArrayList<>();

        int  dir = (isWhite) ? -8 : 8;

        int posRow = pos/8;
        int posCol = pos%8;
        // one step forword
        int tar = pos + dir;
        if(tar >= 0 && tar < 64 && Piece.values()[board.squares[tar]] == Piece.EMPTY){
            
            if ((isWhite && tar / 8 == 0) || (!isWhite && tar / 8 == 7)) {
                // Promotion
                for (Piece promo : new Piece[]{isWhite ? Piece.WQ : Piece.BQ, isWhite ? Piece.WR : Piece.BR,
                        isWhite ? Piece.WB : Piece.BB, isWhite ? Piece.WN : Piece.BN}) {
                    moves.add(new Move(pos, tar, p.ordinal(), -1, promo.ordinal(), Flag.PROMO));
                }
            } else {
                moves.add(new Move(pos, tar, p.ordinal(), -1, -1, Flag.NL));
            }

            // 2 steps 
            boolean stRow = (isWhite && posRow == 6) || (!isWhite && posRow == 1);
            int twoStTar = pos + 2*dir;
            if(stRow && Piece.values()[board.squares[twoStTar]] == Piece.EMPTY){
                moves.add(new Move(pos , twoStTar , p.ordinal(), -1 , -1 , Flag.DPP));
            }
        }
        // capturn dia opposite side pown
        int cap[] = {dir -1 , dir +1};
        for(int x: cap){
            int target = pos + x;

            if(target < 0 || target >=64){
                continue;
            }

            int tarRow = target/8;
            int tarCol = target % 8;

            if(Math.abs(tarCol - posCol) != 1){
                continue;
            }

            Piece tarPiece = Piece.values()[board.squares[target]];
            if(tarPiece != Piece.EMPTY){
                boolean isWhitePiece = tarPiece.name().startsWith("W");
                if(isWhitePiece != isWhite){
                    if ((isWhite && target / 8 == 0) || (!isWhite && target / 8 == 7)) {
                        // Promotion capture
                        for (Piece promo : new Piece[]{isWhite ? Piece.WQ : Piece.BQ, isWhite ? Piece.WR : Piece.BR,
                                isWhite ? Piece.WB : Piece.BB, isWhite ? Piece.WN : Piece.BN}) {
                                    moves.add(new Move(pos, target, p.ordinal(), tarPiece.ordinal(), promo.ordinal(), Flag.PROMO));
                        }
                    } else {
                        moves.add(new Move(pos, target, p.ordinal(), tarPiece.ordinal(), -1, Flag.CAP));
                    }
                }
            }else if(target == board.enPassantSquare){
                moves.add(new Move(pos, target, p.ordinal(), (isWhite ? Piece.BP.ordinal() : Piece.WP.ordinal()), -1, Flag.EN_PASSANT));
            }
        }
        return moves;
    }

}
