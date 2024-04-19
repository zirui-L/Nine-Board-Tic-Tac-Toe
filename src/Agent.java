/*********************************************************
 *  Agent.java
 *  Nine-Board Tic-Tac-Toe Agent
 *  COMP3411/9814 Artificial Intelligence
 *  CSE, UNSW
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Agent {

    static int prevMove = 0;
    static int[][] boards = new int[10][10]; // Zero index not used
    /* 0 = Empty
     * 1 = We played here
     * 2 = Opponent played here
     */
    static Random rand = new Random();
    private static int cutoff = 5;

    public static void main(String args[]) throws IOException {

        if(args.length < 2) {
            System.out.println("Usage: java Agent -p (port)");
            return;
        }

        final String host = "localhost";
        final int portNumber = Integer.parseInt(args[1]);

        Socket socket = new Socket(host, portNumber);
        BufferedReader br = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String line;

        while( true ) {

            line = br.readLine();

            int move = parse(line);

            if( move == -1 ) {
                socket.close();
                return;
            }
            else if( move == 0 )
            {
                // TODO
            }
            else {
                out.println( move );
            }
        }
    }

    public static int parse(String line) {
 // init tells us that a new game is about to begin.
 // start(x) or start(o) tell us whether we will be playing first (x)
 // or second (o); we might be able to ignore start if we internally
 // use 'X' for *our* moves and 'O' for *opponent* moves.
        if( line.contains("init")) {
            // no action
        }
        else if( line.contains("start")) {
            // no action
        }
        else if( line.contains("second_move")) {

            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart+1, argsEnd);
            String[] numbers = list.split(",");

            // place the first move (randomly generated for opponent)
            place( Integer.parseInt(numbers[0]),
                   Integer.parseInt(numbers[1]), 2 );
             // choose and return the second move
            return getNextMove(Integer.valueOf(numbers[1]));
        }
        else if( line.contains("third_move")) {

            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart+1, argsEnd);
            String[] numbers = list.split(",");

            // place the first move (randomly generated for us)
            place(Integer.parseInt(numbers[0]),
                  Integer.parseInt(numbers[1]), 1);

            // place the second move (chosen by opponent)
            place(Integer.parseInt(numbers[1]),
                  Integer.parseInt(numbers[2]), 2);

            // choose and return the third move
            return getNextMove(Integer.valueOf(numbers[2]));
        }
        else if (line.contains("next_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart+1, argsEnd);

            // place the previous move (chosen by opponent)
            place(prevMove, Integer.parseInt(list), 2);

            // choose and return the next move
            return getNextMove(prevMove);
        }
        else if( line.contains("last_move")) {
            // no action
        }
        else if( line.contains("win")) {
            // no action
        }
        else if( line.contains("loss")) {
            // no action
        }
        else if( line.contains("end")) {
            return -1;
        }
        return 0;
    }

    public static void place(int board, int num, int player) {
        prevMove = num;
        boards[board][num] = player;
    }

    public static int getNextMove(int prevMove) {
        int result = minimax(cutoff, 1, prevMove);

        // int optimalMove = 0;
        // int max = Integer.MIN_VALUE;
        // for (Integer move : possibleMoves) {
        //     int v = minValue(prevMove, move, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        //     if (v > max) {
        //         max = v;
        //         optimalMove = move;
        //     }
        // }
        // return optimalMove;
        place(prevMove, result, 1);
        return result;
    }

    public static List<Integer> getPossibleMoves(int prevMove) {
        List<Integer> possibleMoves = new ArrayList<>();
        if (IsFullBoard(prevMove) || checkWinner(prevMove) == 1 || checkWinner(prevMove) == 2) {
            return possibleMoves;
        }
        for (int i = 1; i < boards.length; i++) {
            if (boards[prevMove][i] == 0) {
                possibleMoves.add(i);
            }
        }
        return possibleMoves;
    }

    public static int minimax(int depth, int player, int prevMove) {
        List<Integer> possibleMoves = getPossibleMoves(prevMove);
        int bestScore = (player == 1) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int currentScore;
        int bestMove = 0;
        if (possibleMoves.isEmpty() || depth == 0) {
            bestScore = heuristic();
        } else {
            for (Integer nextmove : possibleMoves) {
                boards[prevMove][nextmove] = player;
                if (player == 1) {
                    currentScore = minimax(depth - 1, 2, nextmove);
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestMove = nextmove;
                    }
                } else {
                    currentScore = minimax(depth - 1, 1, nextmove);
                    if (currentScore < bestScore) {
                        bestScore = currentScore;
                        bestMove = nextmove;
                    }
                }
                boards[prevMove][nextmove] = 0;
            }
        }
        return bestMove;
    }

    // public static int minValue(int board, int move, int alpha, int beta, int depth) {
    //     if (isTerminal() || depth >= cutoff) {
    //         return utility(board);
    //     }

    //     int v = Integer.MAX_VALUE;
    //     applyMove(board, move, 2); // Assume 2 is the opponent
    //     List<Integer> moves = getPossibleMoves(move);
    //     for (Integer nextMove : moves) {
    //         v = Math.min(v, maxValue(move, nextMove, alpha, beta, depth + 1));
    //         beta = Math.min(beta, v);
    //         if (beta <= alpha) {
    //             break; // Alpha-beta pruning
    //         }
    //     }
    //     undoMove(board, move); // Undo the move after exploration
    //     return v;
    // }

    // public static int maxValue(int board, int move, int alpha, int beta, int depth) {
    //     if (isTerminal() || depth >= cutoff) {
    //         return utility(board);
    //     }
    //     int v = Integer.MIN_VALUE;
    //     applyMove(board, move, 1);
    //     List<Integer> moves = getPossibleMoves(move);
    //     for (Integer nextMove : moves) {
    //         v = Math.max(v, minValue(move, nextMove, alpha, beta, depth + 1));
    //         alpha = Math.max(alpha, v);
    //         if (alpha >= beta) {
    //             break; // Alpha-beta pruning
    //         }
    //     }
    //     undoMove(board, move); // Undo the move after exploration
    //     return v;
    // }

    // public static int utility(int board) {
    //  //if the state is terminal then it returns -infinity for a loss, zero for a tie
    //  //and positive infinity for a win
 //     if (isTerminal()) {
 //   int winner = checkWinner(board);
 //   if (winner == 1) {
 //       return Integer.MAX_VALUE;
 //   } else if (winner == 2) {
 //       // Lose
 //       return Integer.MIN_VALUE;
 //   } else if (IsFullBoard(board)) {
 //       // Draw
 //       return 0;
 //   }
 //     }
 //     //otherwise delivers a guess on the how good the state is
    //     return heuristic();
    // }

    public static int heuristic() {
        // Evaluate the heuristic for the entire game state by summing up the heuristic values of all nine boards.
        int score = 0;
        for (int board = 1; board <= 9; board++) {
            score += evaluateBoardHeuristic(board);
        }
        return score;
    }

    public static int evaluateBoardHeuristic(int board) {
        // Basic heuristic: count number of potential lines still open for winning
        int boardScore = 0;
        boardScore += evaluateLine(board, 1, 2, 3); // Row 1
        boardScore += evaluateLine(board, 4, 5, 6); // Row 2
        boardScore += evaluateLine(board, 7, 8, 9); // Row 3
        boardScore += evaluateLine(board, 1, 4, 7); // Column 1
        boardScore += evaluateLine(board, 2, 5, 8); // Column 2
        boardScore += evaluateLine(board, 3, 6, 9); // Column 3
        boardScore += evaluateLine(board, 1, 5, 9); // Diagonal
        boardScore += evaluateLine(board, 3, 5, 7); // Anti-Diagonal
        return boardScore;
    }

    private static int evaluateLine(int board, int pos1, int pos2, int pos3) {
        int score = 0;
        // First cell
        if (boards[board][pos1] == 1) {
            score = 1;
        } else if (boards[board][pos1] == 2) {
            score = -1;
        }
        // Second cell
        if (boards[board][pos2] == 1) {
            if (score == 1) { // cell1 is our mark
                score = 10;
            } else if (score == -1) { // cell1 is opponent's mark
                return 0;
            } else { // cell1 is empty
                score = 1;
            }
        } else if (boards[board][pos2] == 2) {
            if (score == -1) { // cell1 is opponent's mark
                score = -10;
            } else if (score == 1) { // cell1 is our mark
                return 0;
            } else { // cell1 is empty
                score = -1;
            }
        }
        // Third cell
        if (boards[board][pos3] == 1) {
            if (score > 0) { // cell1 and/or cell2 is our mark
                score *= 10;
            } else if (score < 0) { // cell1 and/or cell2 is opponent's mark
                return 0;
            } else { // cell1 and cell2 are empty
                score = 1;
            }
        } else if (boards[board][pos3] == 2) {
            if (score < 0) { // cell1 and/or cell2 is opponent's mark
                score *= 10;
            } else if (score > 0) { // cell1 and/or cell2 is our mark
                return 0;
            } else { // cell1 and cell2 are empty
                score = -1;
            }
        }
        return score;
     }

    public static boolean IsFullBoard(int board) {
        for (int i = 1; i <= 9; i++) {
            if (boards[board][i] == 0) {
                return false;
            }
        }
        return true;
    }

    // public static boolean isTerminal() {
    //     // Check all boards for win condition
    //     for (int board = 1; board <= 9; board++) {
    //         if (checkWinner(board) != 0 || IsFullBoard(board)) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    public static void applyMove(int board, int num, int player) {
        boards[board][num] = player;
    }

    public static void undoMove(int board, int num) {
        boards[board][num] = 0;
    }

    private static int checkWinner(int board) {
        int player = checkHorizontals(board);
        if (player != 0) {
            return player;
        }

        player = checkVerticals(board);
        if (player != 0) {
            return player;
        }

        player = checkDiagonals(board);
        return player;
    }

    private static int checkHorizontals(int board) {
        for (int row = 1; row <= 7; row += 3) {
            if (boards[board][row] != 0 &&
                boards[board][row] == boards[board][row + 1] &&
                boards[board][row + 1] == boards[board][row + 2]) {
                return boards[board][row];
            }
        }
        return 0;
    }

    private static int checkVerticals(int board) {
        for (int col = 1; col <= 3; col++) {
            if (boards[board][col] != 0 &&
                boards[board][col] == boards[board][col + 3] &&
                boards[board][col + 3] == boards[board][col + 6]) {
                return boards[board][col];
            }
        }
        return 0;
    }

    private static int checkDiagonals(int board) {
        if (boards[board][1] != 0 &&
            boards[board][1] == boards[board][5] &&
            boards[board][5] == boards[board][9]) {
            return boards[board][1];
        }
        if (boards[board][3] != 0 &&
            boards[board][3] == boards[board][5] &&
            boards[board][5] == boards[board][7]) {
            return boards[board][3];
        }
        return 0;
    }



}