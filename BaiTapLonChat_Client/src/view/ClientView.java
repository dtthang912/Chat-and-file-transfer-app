/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import model.User;

/**
 *
 * @author dang toan thang d13cn5
 */
public class ClientView extends JFrame{
    private ChatView chatView;
    private LoginView loginView;
    private UploadChooser uploadChooser;
    private DownloadChooser downloadChooser;
    public ClientView(String title){
        super(title);
        setResizable(false);
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        loginView = new LoginView();
        chatView = new ChatView();
        uploadChooser = new UploadChooser();
        downloadChooser = new DownloadChooser();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void showMessage(String message){
        JOptionPane.showMessageDialog(null, message);
    }
    public void console(String message){
        System.out.println(message);
    }
    
    //switch view
    
    public void switchToLoginView(){
        setContentPane(loginView);
        pack();
    }
    public void switchToChatView(){
        setContentPane(chatView);
        pack();
    }
    
    //from login view
    
    public void addLoginListenerFromLoginView(ActionListener log){
        loginView.addLoginListener(log);
    }
    
    public User getUserFromLoginView(){
        return loginView.getUser();
    }
    
    //from chat view
    
    public void addMessageFromChatView(String message){ 
        chatView.addMessage(message);
        pack();
    }
    public void addFileMessageFromChatView(JLabel label){
        chatView.addFileMessage(label); 
        System.out.println(label.getFont());
        pack();
    }
    public String getTextFromChatView(){
        return chatView.getText();
    }
    public void resetTextFromChatView(){
        chatView.resetText();
    }
    public void addSendMessageListenerFromChatView(ActionListener send){
        chatView.addSendMessageListener(send);
    }
    public void addFileSharingListenerFromChatView(ActionListener share){
        chatView.addFileSharingListener(share);
    }
    public List<File> getFileList(){
        uploadChooser.showOpenDialog(this);
        return new ArrayList<>(Arrays.asList(uploadChooser.getSelectedFiles()));
    }
    public File getDirectory(){
        downloadChooser.showSaveDialog(this);
        return downloadChooser.getSelectedFile();
    }
}
