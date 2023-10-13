package lk.ijse.dep.service;

import static lk.ijse.dep.service.Board.NUM_OF_COLS;
import static lk.ijse.dep.service.Board.NUM_OF_ROWS;
import java.util.*;

public class AiPlayer extends Player {

    static class Move {
        private final int column;
        private final int row;

        public Move(int column, int row) {
            this.column = column;
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }
    }
    public class Node {
        private int column; // The column where this move was made.
        private final Piece[][] state; // The game state represented as a 2D array.
        private int visits;
        private double totalReward;
        private List<Node> children;
        private  Node parent;

        public Node(Piece[][] state) {
            this.column = column;
            this.state = copyCurrentState(state);
            this.visits = 0;
            this.totalReward = 0.0;
            this.children = new ArrayList<>();
        }

        public int getColumn() {
            return column;
        }

        public Piece[][] getState() {
            return state;
        }

        public Node getParent() {
            return parent;
        }

        public int getVisits() {
            return visits;
        }

        public double getTotalReward() {
            return totalReward;
        }

        public List<Node> getChildren() {
            return children;
        }

        public void incrementVisits() {
            visits++;
        }

        public void addToTotalReward(double reward) {
            totalReward += reward;
        }

        public void addChild(Node child) {
            children.add(child);
        }
        private Piece[][] copyCurrentState(Piece[][] originalPiece) {
            Piece[][] temp = new Piece[NUM_OF_COLS][NUM_OF_ROWS];
            for (int i = 0; i < NUM_OF_COLS; i++) {
                for (int j = 0; j < NUM_OF_ROWS; j++) {
                    temp[i][j] = originalPiece[i][j];
                }
            }
            return temp;
        }
    }


    int randomMove;

    public AiPlayer(Board newBoard) {
        super(newBoard);
    }

    public int getRandomMove() {
        do randomMove = generateRandomMove();
        while (!isValidMove(randomMove));
        return randomMove;
    }


    private int generateRandomMove() {
        return (int) (Math.random() * 6);
    }

    private boolean isValidMove(int randomMove) {
        return randomMove >= 0 && randomMove < 6 && board.isLegalMove(randomMove);
    }

    @Override
    public void movePiece(int col) {
        Move random = makeMoveWithMCTS();
        board.updateMove(random.column, random.row, Piece.GREEN);
        board.getBoardUI().update(random.column, false);
        if (board.findWinner().getWinningPiece().equals(Piece.GREEN)) {
            board.getBoardUI().notifyWinner(new Winner(Piece.GREEN));
        } else {
            if (!board.existsLegalMoves()) {
                board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
            }
        }

    }
    private Piece[][] copyCurrentState(Piece[][] originalPiece) {
        Piece[][] temp = new Piece[NUM_OF_COLS][NUM_OF_ROWS];
        for (int i = 0; i < NUM_OF_COLS; i++) {
            for (int j = 0; j < NUM_OF_ROWS; j++) {
                temp[i][j] = originalPiece[i][j];
            }
        }
        return temp;
    }



    public int selectBestMove(Node rootNode) {
        List<Node> children = rootNode.getChildren();
        int bestMove = -1;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Node child : children) {
            double value = child.getTotalReward() / child.getVisits();

            if (value > bestValue) {
                bestValue = value;
                bestMove = child.getColumn();
            }
        }

        if (bestMove == -1) {
            // If no best move is found, you can select a move randomly as a fallback.
            return getRandomMove();
        }

        return bestMove;
    }

    public Move makeMoveWithMCTS() {
        int numSimulations = 30; // You can adjust this based on your needs.
        Node rootNode = new Node(copyCurrentState(board.getPieces())); // Create the root node.

        for (int i = 0; i < numSimulations; i++) {
            Node selectedNode = select(rootNode);
            double simulationResult = simulate(selectedNode);
            backpropagate(selectedNode, simulationResult);
        }

        int bestMove = selectBestMove(rootNode);

        return new Move(bestMove,board.findNextAvailableSpot(bestMove));
    }

    private Node select(Node node) {
        if (node.getChildren().isEmpty() || Math.random() < 0.2) {
            // If the node is a leaf node or with a small probability, choose to explore.
            return node;
        } else {
            // Select the child node with the highest UCB1 value.
            double explorationFactor = Math.sqrt(2); // You can adjust this parameter.
            double bestUCB1 = -1;
            Node selectedChild = null;

            for (Node child : node.getChildren()) {
                double exploitation = child.getTotalReward() / child.getVisits();
                double exploration = explorationFactor * Math.sqrt(Math.log(node.getVisits()) / child.getVisits());
                double ucb1Value = exploitation + exploration;

                if (ucb1Value > bestUCB1) {
                    bestUCB1 = ucb1Value;
                    selectedChild = child;
                }
            }

            if (selectedChild == null) {
                // Fallback to random selection if no child has been selected.
                return node;
            }

            return select(selectedChild); // Recursively select a child node.
        }
    }


    public double simulate(Node node) {
        Piece[][] simulatedState = copyCurrentState(node.getState());

        // Simulate a game from the current state.
        while (true) {
            // Check if the game has reached a terminal state or a predefined depth.
            if (isGameTerminal(simulatedState)) {
                return calculateReward(simulatedState);
            }

            // Implement game-specific logic for making moves.
            Move randomMove = new Move(getRandomMove(),board.findNextAvailableSpot(generateRandomMove()));
            if (board.isLegalMove(randomMove.column)) {
                board.updateMove(randomMove.column, randomMove.row, Piece.BLUE);
                if(board.findWinner().getWinningPiece()==Piece.BLUE){
                    board.updateMove(randomMove.column, randomMove.row, Piece.EMPTY);//undo the move
                    return calculateReward(randomMove,Piece.BLUE);// Simulate the opponent's move.
                }
            }

            // Check if the opponent has won.
            if (isGameTerminal(simulatedState)) {
                return calculateReward(simulatedState);
            }

            // Now, make the AI's move. You can adjust your logic for AI moves here.
            Move aiMove = new Move(getRandomMove(),board.findNextAvailableSpot(generateRandomMove()));
            if (board.isLegalMove(aiMove.column)) {
                board.updateMove(aiMove.column,aiMove.row, Piece.GREEN);
                if (board.findWinner().getWinningPiece()==Piece.GREEN){
                    board.updateMove(aiMove.column,aiMove.row,Piece.EMPTY);//undo the move
                    return calculateReward(aiMove,Piece.GREEN);// Simulate the AI's move.
                }
            }
        }
    }
    private boolean isGameTerminal(Piece[][] state) {
        for (int i = 0; i < NUM_OF_COLS; i++) {
            for (int j = 0; j < 2; j++) {
                if (state[i][j] == state[i][j + 1] && state[i][j + 1] == state[i][j + 2] && state[i][j + 2] == state[i][j + 3]) {
                    if(state[i][j] !=Piece.EMPTY) {
                        return  true;
                    }
                }
            }
        }
        for (int i = 0; i < NUM_OF_ROWS; i++) {
            for (int j = 0; j < 3; j++) {
                if (state[j][i] == state[j + 1][i] && state[j + 1][i] == state[j + 2][i] && state[j + 2][i] == state[j + 3][i]) {
                    if(state[j][i] !=Piece.EMPTY) {
                        return  true;
                    }
                }
            }
        }
        return false;
    }

    private double calculateReward(Piece[][] state) {
        for (int i = 0; i < NUM_OF_COLS; i++) {
            for (int j = 0; j < 2; j++) {
                if (state[i][j] == state[i][j + 1] && state[i][j + 1] == state[i][j + 2] && state[i][j + 2] == state[i][j + 3]) {
                    if (state[i][j] != Piece.EMPTY) {
                        return 1;
                    }
                    else return 0;
                }
            }
        }
        for (int i = 0; i < NUM_OF_ROWS; i++) {
            for (int j = 0; j < 3; j++) {
                if (state[j][i] == state[j + 1][i] && state[j + 1][i] == state[j + 2][i] && state[j + 2][i] == state[j + 3][i]) {
                    if (state[j][i] != Piece.EMPTY) {

                        return 1;
                    }
                    else return 0;
                }
            }
        }
        return -1;
    }

    private double calculateReward(Move move,Piece piece){
        if(board.isLegalMove(move.column)){
            board.updateMove(move.column,move.row,piece);
            if(board.findWinner().getWinningPiece().equals(piece)){
                board.updateMove(move.column,move.row,Piece.EMPTY);
                return 1;
            }
            else {
                return 0;
            }
        }

//        int k = move.column;
//        int l = move.row;
//        while (k < NUM_OF_COLS && l < NUM_OF_ROWS) {
//            for (int i = 0; i < NUM_OF_COLS; i++) {
//                for (int j = 0; j < 2; j++) {
//                    if (state[i][j] == state[i][j + 1] && state[i][j + 1] == state[i][j + 2] && state[i][j + 2] == state[i][j + 3]) {
//                        if (state[k][l] != Piece.EMPTY && state[k][l]==state[i][j]) {
//                            return 1;
//                        } else return 0;
//                    }
//                }
//            }
//            for (int i = 0; i < NUM_OF_ROWS; i++) {
//                for (int j = 0; j < 3; j++) {
//                    if(state[l][k]==state[j][i]){
//                    if (state[i][j] == state[j + 1][i] && state[j + 1][i] == state[j + 2][i] && state[j + 2][i] == state[j + 3][i]) {
//                        if (state[l][k] != Piece.EMPTY) {
//                            return 1;
//                            } else return 0;
//                        }
//                    }
//                }
//            }
//
//        }
        return -1;
    }

    public void backpropagate(Node node, double result) {
        // Update node statistics along the path from the simulated node to the root.
        while (node != null) {
            node.incrementVisits(); // Increment the visit count of the node.
            node.addToTotalReward(result); // Add the simulation result to the total reward of the node.
            result = 1 - result; // Flip the result for the parent node (opponent's perspective).

            // Move to the parent node in the tree.
            node = node.getParent();
        }
    }




}
