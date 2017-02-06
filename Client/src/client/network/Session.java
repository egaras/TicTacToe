/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.network;

import assets.*;
import client.*;
import client.controllers.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 *
 * @author Ehab
 */
public class Session {
    public static HashMap<String, Player> allPlayers = new HashMap<String, Player>();
    private Player player;
    private Socket socket;
    private final int portNumber;
    private final String ipAddress;
    private ObjectInputStream downLink;
    private ObjectOutputStream upLink;
    public boolean connected = false;
    private boolean loggedin = false;
    private boolean IAmX=false;
    public static boolean myTurn;
    private Button[][] btns={
                {ClientApp.gameController.b1,ClientApp.gameController.b2,ClientApp.gameController.b3},
                {ClientApp.gameController.b4,ClientApp.gameController.b5,ClientApp.gameController.b6},
                {ClientApp.gameController.b7,ClientApp.gameController.b8,ClientApp.gameController.b9}};
    
    public Session(String ipAddress, int portNumber){
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        //openConnection();
    }
    public void openConnection(){
        try {
            socket = new Socket(ipAddress, portNumber);
            upLink = new ObjectOutputStream(socket.getOutputStream());
            downLink = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Connected to server successfully!");
            //startCommunication();
        } catch (IOException ex) {
            connected = false;
            System.out.println("Connection to server failed!");
        }
    }
    private void startCommunication(){
        new Thread(() -> {
            while(connected){
                try {
                    Message message = (Message) downLink.readObject();
                    MessageHandler(message);
                } catch (IOException ex) {
                    connected = false;
                    System.out.println("input stream failed!");
                    break;
                } catch(ClassNotFoundException cnfex){
                    System.out.println("Class not found exception");
                }
            }
            try{
                socket.close();
                downLink.close();
                upLink.close();
            }catch(IOException ex)
            {
                System.out.println("Client cannot close I/O stream");
            }
            System.out.println("Connection with server closed");
        }).start();
    }
    public boolean loginToServer(String username, String password){
        Message message = new Message(MsgType.LOGIN);
        message.setData("username", username);
        message.setData("password", password);
        if(connected){
            sendMessage(message);
            while(connected){
                try{
                    Message response = (Message)downLink.readObject();
                    if(response.getType() == MsgType.LOGIN){
                        if(response.getData("signal").equals(MsgSignal.SUCCESS)){
                            loggedin = true;
                            player = new Player();
                            player.setUsername(response.getData("username"));
                            player.setFname(response.getData("fname"));
                            player.setLname(response.getData("lname"));
                            player.setPicPath(response.getData("picpath"));
                            player.setScore(Integer.parseInt(response.getData("score")));
                            startCommunication();
                        }
                        break;
                    }else
                        MessageHandler(response);
                }catch(IOException ioex){
                    
                }catch(ClassNotFoundException cnfex){
                    
                }
            }
        }
        return loggedin;
    }
    private void MessageHandler(Message message){
        switch(message.getType()){
            case LOGIN:
                if(message.getData("signal").equals(MsgSignal.SUCCESS)){
                    System.out.println("login to server succedded");
                    System.out.println("ID: "+message.getData("id"));
                    System.out.println("username: "+message.getData("username"));
                    System.out.println("Fullname: "+message.getData("fullname"));
                    loggedin = true;
                }
                else{
                    System.out.println("login to server failed");
                    connected = false;
                }
                break;    
            case REGISTER:
                if(message.getData("signal").equals(MsgSignal.SUCCESS)){
                    System.out.println("register  succedded");
         
                 
                  
                }
                else{
                    System.out.println("register failes to server failed");
                    connected = false;
                }
                break;
            case INIT:
            case NOTIFY:
                updatePlayersList(message);
                break;
            case GAME_REQ:
                respondToRequest(message);
                break;
            case GAME_RES:
                handleResponse(message);
                break;
            case MOVE:
                handleMove(message);
            case GAME_OVER:
                handleGameOver(message);
            default:
                System.out.println("server sent unhandled message");
                break;
        }
    }
    private void sendMessage(Message message){
        try{
            upLink.writeObject(message);
        }catch(IOException ioex){
            ioex.printStackTrace();
            System.out.println("Cannot send message to server");
        }
    }

    public void signUpToServer(String fname, String lname, String username, String password, String picpath) {
     Message message = new Message(MsgType.REGISTER);
        message.setData("username", username);
        message.setData("password", password);
        message.setData("fname",fname);
        message.setData("lname",lname);
        message.setData("picpath",picpath);
       
        sendMessage(message);
    }
    public void requestGame(String secondPlayerName){
        //**ALERT** waiting for other player response with cancel button
        Message message=new Message(MsgType.GAME_REQ,"destination",secondPlayerName);
        sendMessage(message);
    }
    public void respondToRequest(Message incoming){
        //**Alert** with the request from **playerRequestingGame** returns boolean **accept**
        String playerRequestingGame =incoming.getData("source");        
//        Alert alert = new Alert(AlertType.CONFIRMATION);
//                alert.setTitle("Request");
//                alert.setHeaderText("Game request");
//                alert.setContentText(playerRequestingGame+" wants to play with you");
//                alert.showAndWait();
        boolean accept=true;       
        Message outgoing=new Message(MsgType.GAME_RES,"destination",playerRequestingGame);
        if(accept){
            outgoing.setData("response", "accept");
            //set scene to game scene
        }else{
            outgoing.setData("response", "deny");
        }
        sendMessage(outgoing);
    }
    public void handleResponse(Message incoming){
        if(incoming.getData("response").equals("accept")){
            IAmX=true;
            //change scene to gameplay scene
        }else{
            System.out.println("player 2 denied game request");
        }
    }
    public void makeAMove(String x,String y) {
        Message message=new Message(MsgType.MOVE);
        message.setData("x", x);
        message.setData("y", y);
        sendMessage(message);
        myTurn=false;
    }
    private void handleMove(Message message) {
        
        myTurn=true;
        btns[Integer.parseInt(message.getData("x"))][Integer.parseInt(message.getData("y"))].setGraphic(new ImageView(IAmX?"/resources/images/o.png":"/resources/images/x.png"));
    }
    private void handleGameOver(Message message) {
        //**ALERT**win msg **play again(GAME_REQ) **home scene.
        String msg=message.getData("line");
        System.out.println(msg);
    }
    
    public void updatePlayersList(Message message){
        //if(!message.getData("username").equals(this.player.getUsername())){
        if(true){
            if(message.getType() == MsgType.INIT){
                Player newPlayer = new Player();
                newPlayer.setUsername(message.getData("username"));
                newPlayer.setStatus(message.getData("status"));
                newPlayer.setScore(Integer.parseInt(message.getData("score")));
                newPlayer.setPicPath(message.getData("picpath"));
                allPlayers.put(message.getData("username"), newPlayer);
            }else if(message.getType() == MsgType.NOTIFY){
                allPlayers.get(message.getData("username")).setStatus(message.getData("status"));
            }
        }
        System.out.println(message.getType()+" "+message.getData("username")+" "+message.getData("status"));
    }

    
}
