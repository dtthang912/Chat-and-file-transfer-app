/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ServerHeader;
import model.ClientHeader;
import model.Message;
import model.User;
import view.ServerView;

/**
 *
 * @author dang toan thang d13cn5
 */
public class ServerFileTransferController {
    private ServerSocket server;
    private ServerView view;
    private List<AcceptFClientThread> clientList;
    
    public ServerFileTransferController(ServerView view){
        this.view = view;
        clientList = new ArrayList<>();
        openServer(1235);
        listen();
    }
    private void openServer(int port){
        try {
            server = new ServerSocket(port);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void listen(){
        while(true){
            try { 
                new Thread(new AcceptFClientThread(server.accept())).start();
            } catch (IOException ex) {
                 view.showMessage(ex.toString());
            }
        }
    }
    private class AcceptFClientThread implements Runnable{
        
        private Socket fsocket;
        private String userName;
        private ObjectOutputStream fos = null;
        private ObjectInputStream fis = null;
        
        public AcceptFClientThread(Socket fsocket){
            this.fsocket = fsocket; 
            clientList.add(this); 
            try {
                fos = new ObjectOutputStream(new BufferedOutputStream(fsocket.getOutputStream()));
                fos.flush();
                fis = new ObjectInputStream(new BufferedInputStream(fsocket.getInputStream()));
                userName = fis.readUTF();
            } catch (IOException ex) {
                Logger.getLogger(ServerFileTransferController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void run() {
            while(true){
                try { 
                    ClientHeader clientHeader = (ClientHeader) fis.readObject();
                    if(clientHeader.getValue() == clientHeader.UPLOAD_FILE) getFiles(); 
                    else if(clientHeader.getValue() == clientHeader.DOWNLOAD_FILE) downloadFile();
                } catch (IOException ex) {
                    try {
                        fsocket.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(ServerFileTransferController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    return;
                } catch (ClassNotFoundException ex) {
                    
                }
                
            }
            }
        private void getFiles(){
                    try {
                        int numberOfFiles = fis.readInt(); 
                        for(int i = 0; i < numberOfFiles; i++){
                            int fileID = new File("c:\\users\\bao_an\\desktop\\server").listFiles().length+1;
                            String fileName =fileID + "_" + fis.readUTF(); 
                            long size = fis.readLong(); 
                            byte[] buffer = new byte[4096];
                            int n = 0;
                            FileOutputStream out = new FileOutputStream("c:\\users\\bao_an\\desktop\\server\\"+fileName);
                            while(size > 0 && (n = fis.read(buffer, 0 , (int) Math.min(buffer.length, size))) != -1){
                                out.write(buffer, 0 , n);
                                size -= n;
                            }
                            out.close();
                            for(AcceptFClientThread client : clientList){ 
                                client.writeServerHeader(new ServerHeader(ServerHeader.UPDATE_CHAT));
                                client.writeMessage(new Message(userName,fileName));
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ServerFileTransferController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
        }
        private void downloadFile(){
            try { 
                    String fileName = fis.readUTF(); 
                    writeServerHeader(new ServerHeader(ServerHeader.DOWNLOAD));
                    fos.flush();
                    File file = new File("c:\\users\\bao_an\\desktop\\server\\"+fileName);
                    fos.writeLong(file.length()); 
                    FileInputStream in = new FileInputStream(file); 
                    int n = 0;
                    byte[] buffer = new byte[4096];
                    while((n = in.read(buffer)) != -1){
                        fos.write(buffer, 0, n);
                        fos.flush();
                    }
                
            } catch (IOException ex) {
                Logger.getLogger(ServerFileTransferController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        private void writeMessage(Message message){
            try {
                fos.writeObject(message);
                fos.flush();
            } catch (IOException ex) {
                
            }
        }
        private void writeServerHeader(ServerHeader serverHeader){
            try {
                fos.writeObject(serverHeader);
                fos.flush();
            } catch (IOException ex) {
                
            }
        }
    }
}
