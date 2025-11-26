package game;


public class Move {
    public int from;
    public int to;
    public int movedPiece;     
    public int capPiece;  
    public int promoPiece; 
    public Flag flag;

    

    public Move(int from, int to, int movedPiece) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capPiece = -1;
        this.promoPiece = -1;
        this.flag = Flag.NL;
    }

    public Move(int from, int to, int movedPiece, int capPiece, int promoPiece, Flag flag) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capPiece = capPiece;
        this.promoPiece = promoPiece;
        this.flag = flag;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Move from ").append(from).append(" to ").append(to);

        if (capPiece != -1)
            sb.append(" capturing ").append(capPiece);

        if (promoPiece != -1)
            sb.append(" promoting to ").append(promoPiece);

        if (flag != Flag.NL)
            sb.append(" [flag=").append(flag).append("]");

        return sb.toString();
    }

    
}
