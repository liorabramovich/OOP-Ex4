package gui;

import javax.swing.*;
import java.io.IOException;

public class GUI extends JFrame {


    public GUI() throws IOException {
        setSize(1000, 800);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new GamePanel());
        setLocationRelativeTo(null);
        setVisible(true);
    }


}