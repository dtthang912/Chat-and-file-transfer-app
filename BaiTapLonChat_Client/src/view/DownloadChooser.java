/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author dang toan thang d13cn5
 */
public class DownloadChooser extends JFileChooser{
    public DownloadChooser(){
        setCurrentDirectory(new File("c:\\users\\bao_an\\desktop"));
        setDialogTitle("Save as");
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setApproveButtonText("Save");
    }
}
