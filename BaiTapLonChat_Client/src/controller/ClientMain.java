/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import view.ClientView;

/**
 *
 * @author dang toan thang d13cn5
 */
public class ClientMain {
    public static void main(String[] args) {
        ClientView view = new ClientView("Ung dung chat va chia se file");
        ClientController control = new ClientController(view);
    }
}
