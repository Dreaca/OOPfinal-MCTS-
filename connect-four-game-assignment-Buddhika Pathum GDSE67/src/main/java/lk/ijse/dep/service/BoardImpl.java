package lk.ijse.dep.service;

import java.util.Arrays;

public class BoardImpl implements Board{
    int col1,col2,row2,row1;
    private int  spot = -1;
    private  final Piece [][] pieces;
    private final BoardUI boardUI;

    public BoardImpl(BoardUI boardUI) {
        this.pieces = new Piece[NUM_OF_COLS][NUM_OF_ROWS];
        this.boardUI = boardUI;
        for (int i = 0; i < pieces.length; i++) {
            Arrays.fill(pieces[i], Piece.EMPTY);
        }
    }

    @Override
    public BoardUI getBoardUI() {
        return boardUI;
    }

    @Override
    public int findNextAvailableSpot(int col)  {
        for (int i = 0; i < NUM_OF_ROWS; i++) {
                if(pieces[col][i].equals(Piece.EMPTY)){
                    spot =i;
                    break;
            }
        }
        return spot;
    }

    @Override
    public boolean isLegalMove(int col) {
       return  (findNextAvailableSpot(col)!=-1);
    }

    @Override
    public boolean existsLegalMoves() {
        boolean isTrue = false;
        for (int i = 0; i < NUM_OF_COLS; i++) {
            for (int j = 0; j < NUM_OF_ROWS; j++) {
                if (pieces[i][j].equals(Piece.EMPTY)){
                    isTrue = true;
                    break;
                }
            }
        }
    return isTrue;
    }

    public Piece[][] getPieces(){
        return pieces;
    }



    @Override
    public void updateMove(int col, Piece move) {
        for (int i = 0; i < NUM_OF_ROWS; i++) {
            if (pieces[col][i].equals(Piece.EMPTY)){
                pieces[col][i]=move;
                break;
            }
        }
    }
    public void updateMove(int col, int row, Piece move){
        pieces[col][row]=move;
    }
    public Winner findWinner() {
        Piece winningPiece ;

        for (int i = 0; i < NUM_OF_COLS; i++) {
            for (int j = 0; j < 2; j++) {
                if (pieces[i][j] == pieces[i][j + 1] && pieces[i][j + 1] == pieces[i][j + 2] && pieces[i][j + 2] == pieces[i][j + 3]) {
                    if(pieces[i][j] !=Piece.EMPTY) {
                        winningPiece = pieces[i][j];
                        col1 = i;
                        col2 = i;
                        row1 = j;
                        row2 = j + 3;
                        return new Winner(winningPiece, col1, row1, col2, row2);
                    }
                }
            }
        }
        for (int i = 0; i < NUM_OF_ROWS; i++) {
            for (int j = 0; j < 3; j++) {
                if (pieces[j][i] == pieces[j + 1][i] && pieces[j + 1][i] == pieces[j + 2][i] && pieces[j + 2][i] == pieces[j + 3][i]) {
                    if(pieces[j][i] !=Piece.EMPTY) {
                        winningPiece = pieces[j][i];
                        col1 = j;
                        col2 = j + 3;
                        row1 = i;
                        row2 = i;
                        return new Winner(winningPiece, col1, row1, col2, row2);
                    }
                }
            }
        }
        return new Winner(Piece.EMPTY);
    }

}
