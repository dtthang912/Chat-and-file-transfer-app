/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Message;
import model.User;
import view.ServerView;

/**
 *
 * @author dang toan thang d13cn5
 */
public class ServerChatController {
    private ServerSocket server;
    private ServerView view;
    private List<AcceptClientThread> clientList;

    public ServerChatController(ServerView view){
        this.view = view;
        clientList = new ArrayList<>();
        openServer(1234);
        listen();
    }
    private void openServer(int port){
        try {
            server = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(ServerChatController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void listen(){
        try{
            while(true){
                new Thread(new AcceptClientThread(server.accept())).start();
                        }
                    }
        catch(Exception e){
                       view.showMessage(e.toString());
                    }
    }
    
    private class AcceptClientThread implements Runnable{
        private Socket chatSocket;
        private User user;
        private DAO dao = new DAO();
        private ObjectOutputStream cos = null;
        private ObjectInputStream cis = null;

        public AcceptClientThread(Socket chatSocket){
            this.chatSocket = chatSocket;
        }

        @Override
        public void run() {
            if(!checkLogin()) try {
                chatSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            else{
                try {
                    clientList.add(this); 
                    while(true){ 
                        Message message = (Message)cis.readObject();   
                        for(AcceptClientThread client : clientList){
                            client.write(message);
                            }
                    }
                } catch (IOException ex) {
                    try {
                        chatSocket.close();
                    } catch (IOException ex1) {
                        
                        }
                    return;
                } catch (ClassNotFoundException ex) {
                    
                }
            }
        }
        private void write(Message message){
            try {
                cos.writeObject(message);
                cos.flush();
            } catch (IOException ex) {
                
            }
        }
        private boolean checkLogin(){
            try{ 
                cos = new ObjectOutputStream(chatSocket.getOutputStream());
                cos.flush();
                cis = new ObjectInputStream(chatSocket.getInputStream()); 
                if(dao.checkUser(user = (User)cis.readObject())){
                    cos.writeObject("passed");
                    cos.flush();
                    return true;
                }
                else{
                    cos.writeObject("failed");
                    cos.flush();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return false;
        }
    }
}

