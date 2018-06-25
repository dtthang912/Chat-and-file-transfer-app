/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import model.ClientHeader;
import model.ServerHeader;
import model.Message;
import model.User;
import view.ChatView;
import view.ClientView;
import view.LoginView;

/**
 *
 * @author dang toan thang d13cn5
 */
public class ClientController {
    private String serverHost = "localhost";
    private User user;
    private ClientView clientView;
    
    private Socket chatSocket;
    private int serverChatPort = 1234;
    private ObjectOutputStream cos = null;
    private ObjectInputStream cis = null;
    
    private Socket fileSharingSocket = null;
    private int serverFileSharingPort = 1235;
    private ObjectOutputStream fos = null;
    private ObjectInputStream fis = null;
    private String filePath = "";

    public ClientController(ClientView clientView){
        this.clientView = clientView;
        
        clientView.addLoginListenerFromLoginView(new LoginListener());
        clientView.addSendMessageListenerFromChatView(new SendMessageListener());
        clientView.addFileSharingListenerFromChatView(new FileSharingListener());
        
        clientView.switchToLoginView();
        clientView.setVisible(true);
        
        clientView.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                clientView.console(""+closeChatConnection());
                clientView.console(""+closeFileSharingConnection());
                System.exit(0);
            }
        });
    }
    private boolean login(){
        try{
            chatSocket = new Socket(serverHost,serverChatPort);
            cos = new ObjectOutputStream(new BufferedOutputStream(chatSocket.getOutputStream())); 
            cos.flush(); 
            cis = new ObjectInputStream(new BufferedInputStream(chatSocket.getInputStream()));
            cos.writeObject(user); 
            cos.flush();
            if(((String)cis.readObject()).equals("passed")) return true;
        }
        catch(Exception e){
            
        }
        return false;
    }
    private void connectToFileServer(){
        try {
            fileSharingSocket = new Socket(serverHost,serverFileSharingPort);
            fos = new ObjectOutputStream(new BufferedOutputStream(fileSharingSocket.getOutputStream()));
            fos.flush();
            fis = new ObjectInputStream(new BufferedInputStream(fileSharingSocket.getInputStream()));
            fos.writeUTF(user.getUserName());
            fos.flush();
        } catch (IOException ex) {
            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void updateChat(){
        new Thread(){
            @Override
            public void run(){
                while(true){
                    try { 
                        Message message = (Message)cis.readObject(); 
                        clientView.addMessageFromChatView(message.getUserName()+": "+message.getContent());
                    } catch (IOException ex) {
                        return;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
    }
    private void dispatch(){
        new Thread(){
            @Override
            public void run(){
                while(true){
                    try {
                        ServerHeader serverHeader = (ServerHeader)fis.readObject();
                        if(serverHeader.getValue() == ServerHeader.UPDATE_CHAT) updateFileChat(); 
                        else if(serverHeader.getValue() == ServerHeader.DOWNLOAD) downloadFile();
                    } catch (IOException ex) {
                        return;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
    }
    private void updateFileChat(){
        try {
            Message message = (Message)fis.readObject();
            String fileName = message.getContent().substring(message.getContent().indexOf("_")+1);
            JLabel label = new JLabel("<html><b>"+message.getUserName()+": "+fileName+"</b></html>");
            label.addMouseListener(new DownloadFileListener(message.getContent())); 
            clientView.addFileMessageFromChatView(label);
        } catch (IOException ex) {
            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void downloadFile(){
        try { 
            long size = fis.readLong(); 
            byte[] buffer = new byte[4096];
            int n = 0;
            FileOutputStream out = new FileOutputStream(filePath); 
            while(size > 0 && (n = fis.read(buffer, 0 , (int) Math.min(buffer.length, size))) != -1){
                out.write(buffer, 0 , n);
                size -= n;
            }
            out.close();
            clientView.showMessage("Download thanh cong!");
        } catch (IOException ex) {
            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private boolean closeChatConnection(){
        try {
             if(chatSocket != null) chatSocket.close();
          } catch (IOException ex) {
                    return false;
                }
        return true;
    }
    private boolean closeFileSharingConnection(){
        try {
             if(fileSharingSocket != null) fileSharingSocket.close();
          } catch (IOException ex) {
                    return false;
                }
        return true;
    }
    
    private List<File> removeFolders(List<File> files){
        Iterator<File> i = files.iterator();
        while(i.hasNext()){
            File file = i.next();
            if(file.isDirectory()) i.remove();
        }
        return files;
    }
    private void uploadFiles(List<File> files){
        try { 
            if(files.size()<1) return; 
            fos.writeObject(new ClientHeader(ClientHeader.UPLOAD_FILE));
            fos.writeInt(files.size());
            for(File f : files){
                fos.writeUTF(f.getName());
                fos.writeLong(f.length());
                FileInputStream in = new FileInputStream(f.getAbsolutePath()); 
                int n = 0;
                byte[] buffer = new byte[4096];
                while((n = in.read(buffer)) != -1){
                    fos.write(buffer, 0, n);
                    fos.flush();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class LoginListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            user = clientView.getUserFromLoginView();
            if(login()) {
                clientView.switchToChatView();
                updateChat(); 
                connectToFileServer();
                dispatch();
            }
            else {
                clientView.showMessage("Loi dang nhap!");
                clientView.console(""+closeChatConnection());
            }
        }
        
    }
    private class SendMessageListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if(clientView.getTextFromChatView().trim().equals("")) return;
                cos.writeObject(new Message(user.getUserName(),clientView.getTextFromChatView().trim()));
                cos.flush();
                clientView.resetTextFromChatView();
            } catch (IOException ex) {
                Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    private class FileSharingListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            uploadFiles(removeFolders(clientView.getFileList()));
        }
        
    }
    private class DownloadFileListener extends MouseAdapter{
        private String fileNameInServer;
        public DownloadFileListener(String fileNameInServer){
            this.fileNameInServer = fileNameInServer;
        }
        @Override
        public void mouseClicked(MouseEvent e){
            try {
                File chosenFile = clientView.getDirectory();
                if(chosenFile == null) return;
                filePath = chosenFile.getAbsolutePath()+
                           "\\"+
                           fileNameInServer.substring(fileNameInServer.indexOf("_")+1);
                fos.writeObject(new ClientHeader(ClientHeader.DOWNLOAD_FILE));
                fos.writeUTF(fileNameInServer);
                fos.flush(); 
            } catch (IOException ex) {
                Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
