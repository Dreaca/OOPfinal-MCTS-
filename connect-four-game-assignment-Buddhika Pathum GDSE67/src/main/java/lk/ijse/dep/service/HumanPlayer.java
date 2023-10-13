package lk.ijse.dep.service;

import lk.ijse.dep.service.Board;
import lk.ijse.dep.service.Piece;
import lk.ijse.dep.service.Player;
import lk.ijse.dep.service.Winner;

public class HumanPlayer extends Player {
    public HumanPlayer(Board newBoard) {
        super(newBoard);
    }

    @Override
    public void movePiece(int col) {
        if(board.isLegalMove(col)){
            board.updateMove(col, Piece.BLUE);
            board.getBoardUI().update(col,true);
            if(board.findWinner().getWinningPiece().equals(Piece.BLUE)){
                board.getBoardUI().notifyWinner(new Winner(Piece.BLUE));
            }
            else {
                if (!board.existsLegalMoves()){
                    board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
                }
            }
        }
    }
}
