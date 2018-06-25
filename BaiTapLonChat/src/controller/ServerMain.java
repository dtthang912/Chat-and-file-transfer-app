/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import view.ServerView;//sua nut X

/**
 *
 * @author dang toan thang d13cn5
 */
public class ServerMain {
    public static void main(String[] args) {
        ServerView view = new ServerView();
        new Thread(){
            @Override
            public void run(){
                new ServerChatController(view);
            }
        }.start();
        new Thread(){
            @Override
            public void run(){
                new ServerFileTransferController(view);
            }
        }.start();

        
    }
}
