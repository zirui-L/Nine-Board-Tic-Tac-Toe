
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
     * 1 = We played hereC:\MinGW\bin
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

    public static int parse(String line)
    {
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
        else if(line.contains("next_move")) {
                        
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

    public static int makeRandomMove() {
        int n = rand.nextInt(9) + 1;

        while( boards[prevMove][n] != 0 ) {
            n = rand.nextInt(9) + 1;
        }

        place(prevMove, n, 1);
        return n;
    }

    public static int getNextMove(int prevMove) {
        List<Integer> possibleMoves = getPossibleMoves(prevMove);
        int optimalMove = 0;
        int max = Integer.MIN_VALUE;
        for (Integer move : possibleMoves) {
            int v = minValue(prevMove, move, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
            if (v > max) {
                max = v;
                optimalMove = move;
            }
        }
        return optimalMove;
    }

    public static List<Integer> getPossibleMoves(int prevMove) {
        List<Integer> possibleMoves = new ArrayList<>();
        for (int i = 1; i < boards.length; i++) {
            if (boards[prevMove][i] == 0) {
                possibleMoves.add(i);
            }
        }
        return possibleMoves;
    }

    public static int minValue(int board, int move, int alpha, int beta, int depth) {
        if (isTerminal() || depth >= cutoff) {
            return utility(board);
        }

        int v = Integer.MAX_VALUE;
        applyMove(board, move, 2); // Assume 2 is the opponent
        List<Integer> moves = getPossibleMoves(board);
        for (Integer nextMove : moves) {
            v = Math.min(v, maxValue(board, nextMove, alpha, beta, depth + 1));
            beta = Math.max(beta, v);
            if (beta <= alpha) {
                break; // Alpha-beta pruning
            }
        }
        undoMove(board, move); // Undo the move after exploration
        return v;
    }

    public static int maxValue(int board, int move, int alpha, int beta, int depth) {
        if (isTerminal() || depth >= cutoff) {
            return utility(board);
        }

        int v = Integer.MIN_VALUE;
        applyMove(board, move, 1); // Assume 1 is us
        List<Integer> moves = getPossibleMoves(board);
        for (Integer nextMove : moves) {
            v = Math.max(v, minValue(board, nextMove, alpha, beta, depth + 1));
            alpha = Math.max(alpha, v);
            if (alpha >= beta) {
                break; // Alpha-beta pruning
            }
        }
        undoMove(board, move); // Undo the move after exploration
        return v;
    }

    public static int utility(int board) {
    	//if the state is terminal thenit returns -infinity for a loss, zero for a tie
    	//and positive infinity for a win
	    if(isTerminal()) {
			int winner = checkWinner(board);
			if (winner == 1) {
			    return Integer.MAX_VALUE;
			} else if (winner == 2) {
			    // Lose
			    return Integer.MIN_VALUE;
			} else if (IsFullBoard(board)) {
			    // Draw
			    return 0;
			}
	    }
	    //otherwise delivers a guess on the how good the state is
            return heuristic(board);
    }

    public static int heuristic(int board) {
        // Basic heuristic: count number of potential lines still open for winning
        int score = 0;
        // Count for AI (1) and Opponent (2)
        int[] counts = new int[3]; // Index 0 unused
        for (int i = 1; i <= 9; i++) {
            if (boards[board][i] == 0) {
                // Increment potential lines if empty
                counts[1] += 1; // Simplified example
                counts[2] += 1; // Simplified example
            }
        }
        score = counts[1] - counts[2];
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

    public static boolean isTerminal() {
        // Check all boards for win condition
        for (int board = 1; board <= 9; board++) {
            if (checkWinner(board) != 0 || IsFullBoard(board)) {
                return true;
            }
        }
        return false;
    }

    public static void applyMove(int board, int num, int player) {
        boards[board][num] = player;
    }

    public static void undoMove(int board, int num) {
        boards[board][num] = 0;
    }

    private static int checkWinner(int board) {
        int player = checkHorizontals(board);
        if (player != 0) return player;

        player = checkVerticals(board);
        if (player != 0) return player;

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