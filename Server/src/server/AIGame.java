package server;

import assets.Message;
import assets.MsgType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import model.Player;
import assets.*;
import assets.MsgType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.management.Notification;
import server.Game;
import server.network.Session;



class Point {

    int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}

class PointAndScore {

    int score;
    Point point;

    PointAndScore(int score, Point point) {
        this.score = score;
        this.point = point;
    }
}

public class AIGame {

    List<Point> availablePoints;
    Scanner scan = new Scanner(System.in);
    int[][] board = new int[3][3];
    String player=null;

    public AIGame() {
    }
    public AIGame(String name) {
        player=name;
    }

    public boolean isGameOver() {
        //Game is over is someone has won, or board is full (draw)
        return (hasXWon() || hasOWon() || getAvailableStates().isEmpty());
    }

    public boolean hasXWon() {
        if ((board[0][0] == board[1][1] && board[0][0] == board[2][2] && board[0][0] == 1) || (board[0][2] == board[1][1] && board[0][2] == board[2][0] && board[0][2] == 1)) {
            //System.out.println("X Diagonal Win");
            return true;
        }
        for (int i = 0; i < 3; ++i) {
            if (((board[i][0] == board[i][1] && board[i][0] == board[i][2] && board[i][0] == 1)
                    || (board[0][i] == board[1][i] && board[0][i] == board[2][i] && board[0][i] == 1))) {
                // System.out.println("X Row or Column win");
                return true;
            }
        }
        return false;
    }

    public boolean hasOWon() {
        if ((board[0][0] == board[1][1] && board[0][0] == board[2][2] && board[0][0] == 2) || (board[0][2] == board[1][1] && board[0][2] == board[2][0] && board[0][2] == 2)) {
            // System.out.println("O Diagonal Win");
            return true;
        }
        for (int i = 0; i < 3; ++i) {
            if ((board[i][0] == board[i][1] && board[i][0] == board[i][2] && board[i][0] == 2)
                    || (board[0][i] == board[1][i] && board[0][i] == board[2][i] && board[0][i] == 2)) {
                //  System.out.println("O Row or Column win");
                return true;
            }
        }

        return false;
    }

    public List<Point> getAvailableStates() {
        availablePoints = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (board[i][j] == 0) {
                    availablePoints.add(new Point(i, j));
                }
            }
        }
        return availablePoints;
    }

    public void placeAMove(Point point, int player) {
        board[point.x][point.y] = player;   //player = 1 for X, 2 for O
    }

    public void takeHumanInput() {
        System.out.println("Your move: ");
        int x = scan.nextInt();
        int y = scan.nextInt();
        Point point = new Point(x, y);
        placeAMove(point, 2);
    }

    public String takeMove(int x, int y) {
        String stats = null;
        Point point = new Point(x, y);
        this.placeAMove(point, 2);

        this.minimax(0, 1); //compter's trn  1 = compter 2 = user

        placeAMove(this.computersMove, 1);
        //b.displayBoard();

        if (this.hasXWon()) {
            stats = "pcWon";
        } else if (this.hasOWon()) {
            stats = "playerWon"; //Can't happen
        } else if (this.isGameOver() && !this.hasXWon()) {
            stats = "draw";

        } else {
            stats = "gameon";

            int x1 = this.computersMove.x;
            int y1 = this.computersMove.y;
            System.out.println(x1 + " " + y1);
            Message message=new Message(MsgType.MOVE);
        message.setData("x", Integer.toString(x1));
        message.setData("y", Integer.toString(y1));
        Session.connectedPlayers.get(player).SendMessage(message); //5ali al sendmessage pblic fel session

        }

        return stats;
    }

    public void displayBoard() {
        System.out.println();

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();

        }
    }

    Point computersMove;

    public int minimax(int depth, int turn) {
        if (hasXWon()) {
            return +1;
        }
        if (hasOWon()) {
            return -1;
        }

        List<Point> pointsAvailable = getAvailableStates();
        if (pointsAvailable.isEmpty()) {
            return 0;  // if list is emty so no available move ..retrn 
        }
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;

        for (int i = 0; i < pointsAvailable.size(); ++i) {
            Point point = pointsAvailable.get(i);
            if (turn == 1) {
                placeAMove(point, 1);
                int currentScore = minimax(depth + 1, 2);
                max = Math.max(currentScore, max);

                if (depth == 0) {
                    System.out.println("Score for position " + (i + 1) + " = " + currentScore);
                }
                if (currentScore >= 0) {
                    if (depth == 0) {
                        computersMove = point;
                    }
                }
                if (currentScore == 1) {
                    board[point.x][point.y] = 0;
                    break;
                }
                if (i == pointsAvailable.size() - 1 && max < 0) {
                    if (depth == 0) {
                        computersMove = point;
                    }
                }
            } else if (turn == 2) {
                placeAMove(point, 2);
                int currentScore = minimax(depth + 1, 1);
                min = Math.min(currentScore, min);
                if (min == -1) {
                    board[point.x][point.y] = 0;
                    break;
                }
            }
            board[point.x][point.y] = 0; //Reset this point
        }
        return turn == 1 ? max : min;
    }
}

//public class AIGam {
//
//    public static void main(String[] args) {
//        AIGame b = new AIGame();
////        Random rand = new Random();
//
//        b.displayBoard();
//
////        System.out.println("Select turn:\n\n1. Computer 2. User: ");
//////        int choice = b.scan.nextInt();
////        if(choice == 1){
////            Point p = new Point(rand.nextInt(3), rand.nextInt(3));
////            b.placeAMove(p, 1);
////            b.displayBoard();
//////        }
//        while (!b.isGameOver()) {
//
//            String stats = null;
//            System.out.println("Your move: ");
//            int x = b.scan.nextInt();
//            int y = b.scan.nextInt();
//            stats = b.takeMove(x, y);
//
//            System.out.println(stats);
//            b.displayBoard();
//            //Point userMove = new Point(b.scan.nextInt(), b.scan.nextInt());
//            //  b.placeAMove(userMove, 2); //2 for O and O is the user
////            b.takeHumanInput();
////            b.displayBoard();
////           
//
//        }
//    }
//}
