import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;

import java.util.Random;
import java.util.Arrays;


public class MainAppLayout extends JFrame implements KeyListener, ActionListener {
    
    
    public static final String ZOMBIE_FILE = "images/zombie.png";
    public static final String HUMAN_FILE = "images/human.png";
    public static final String DESTINATION_FILE = "images/destination.png";
    public static final String VACANT_FILE = "images/vacant.png";
    
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 10;
    
    static ImageIcon zombieImage = new ImageIcon(ZOMBIE_FILE);
    static ImageIcon humanImage = new ImageIcon(HUMAN_FILE);
    static ImageIcon destinationImage = new ImageIcon(DESTINATION_FILE);
    static ImageIcon vacantImage = new ImageIcon(VACANT_FILE);
    
    private int[][] gameBoard;
    private JPanel CenterPanel;
    private JPanel topPanelA;
    private JPanel buttomPanel;
    private int humanLocationX = 0;
    private int humanLocationY = 9;
    private int destinationX = 10;
    private int destinationY = 0;
    private int zombieALocationX = 0;
    private int zombieALocationY = 0;
    private int zombieBLocationX = 0;
    private int zombieBLocationY = 0;
    private boolean zombieAAlert = false;
    private boolean zombieBAlert = false;
    private boolean playerWon = false;
    private boolean playerLost = false;
    
    private JButton NewGameButton;
    
    public MainAppLayout(){
        
        // To close the application when clicking the close button of a window.
        // On MS Windows, it's the top right hand corner white on red cross
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Setting up how component will be organised in the main window
        this.setLayout(new BorderLayout(10, 20));
        
        // Setting up the background colour of the main window
        this.getContentPane().setBackground(Color.white);
        
        this.setTitle("Apocalypse - Zombies are coming!");
        
        initialise();
        
        this.addKeyListener(this);
        this.setFocusable(true);
        this.getContentPane().add(topPanelA, BorderLayout.NORTH);
        this.getContentPane().add(CenterPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttomPanel, BorderLayout.SOUTH);
        this.setSize(700, 700);
        
    }
    
    public void initialise(){
        
        //Initialise the interface.
        
        // TOP PANEL, series of button using FlowLayout
        topPanelA = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        topPanelA.setBackground(Color.RED);
        JTextArea TopPanelAMessage = new JTextArea("Zombies are flooding the city, the military has given up retaliation.\nYou must find your way towards the extraction point with arrow keys. Avoid the undead!");
        TopPanelAMessage.setBackground(Color.red);
        topPanelA.add(TopPanelAMessage);
        
        // CENTER PANEL, main game map grid.
        CenterPanel = new JPanel(new GridLayout(10,10,0,0));
        gameBoard = new int[BOARD_WIDTH][BOARD_HEIGHT];
        
        //Gameboard Encoding: 0 = vacant, 1 = zombie, 2 = human, 3 = destination, 4 = stepping in will alert the zombie
        for(int i=0; i<gameBoard.length; i++){
            for(int j=0; j<gameBoard.length; j++){
                gameBoard[i][j] = 0;
            }
        }
        
        //Build the initial board.
        
        Random randomInt = new Random();
        humanLocationX = randomInt.nextInt(BOARD_WIDTH);
        destinationX = randomInt.nextInt(BOARD_WIDTH);
        gameBoard[gameBoard.length-1][humanLocationX] = 2;
        gameBoard[0][destinationX] = 3;
        
        zombieALocationX = randomInt.nextInt(4-0+1)+0;
        zombieALocationY = randomInt.nextInt(7-2+1)+2;
        zombieBLocationX = randomInt.nextInt(9-5+1)+5;
        zombieBLocationY = randomInt.nextInt(7-2+1)+2;
        gameBoard[zombieALocationY][zombieALocationX] = 1;
        gameBoard[zombieBLocationY][zombieBLocationX] = 1;
        updateBoard();
        
        // BOTTOM PANEL, use the default FlowLayout, containing two buttons.
        buttomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        NewGameButton = new JButton("New Game");
        NewGameButton.addActionListener( this );
        buttomPanel.add(NewGameButton);
    }
    
    public void updateBoard() {
        
        //Let the AI move the zombies.
        zombieMove();
        
        //update the board after keyboard move
        CenterPanel.removeAll();
        for(int i=0; i<gameBoard.length; i++){
            for(int j=0; j<gameBoard.length; j++){
                JLabel addedImage = new JLabel(vacantImage);
                if (gameBoard[i][j] == 3) {
                    addedImage = new JLabel(destinationImage);
                }
                else if (gameBoard[i][j] == 2) {
                    addedImage = new JLabel(humanImage);
                }
                else if (gameBoard[i][j] == 1) {
                    addedImage = new JLabel(zombieImage);
                }
                else {
                    addedImage = new JLabel(vacantImage);
                }
                CenterPanel.add(addedImage);
            }
        }
        CenterPanel.repaint();
        CenterPanel.updateUI();
        
        if ((humanLocationX == zombieALocationX) & (humanLocationY == zombieALocationY)) {
            //Losing conditions check for zombie A.
            System.out.println("Player lost to Zombie A.");
            lost();
        }
        
        if ((humanLocationX == zombieBLocationX) & (humanLocationY == zombieBLocationY)) {
            //Losing conditions check for zombie B.
            System.out.println("Player lost to Zombie B.");
            lost();
        }
        
        if ((humanLocationX == destinationX) & (humanLocationY == destinationY)) {
            //Winning conditions check.
            winning();
        }
    }
    
    public void zombieMove(){
        
        boolean zombieAMoved = false;
        boolean zombieBMoved = false;
        
        if (zombieAAlert == false) {
            
            //In case human moved to zombie's old location.
            if (gameBoard[zombieALocationY][zombieALocationX] == 1) {
                gameBoard[zombieALocationY][zombieALocationX] = 0;
            }
            
            boolean[] moveArray = moveValidator(zombieALocationX, zombieALocationY);
            System.out.println("========Zombie Move Starts========");
            System.out.print("Original Y for A:");
            System.out.println(zombieALocationY);
            System.out.print("Original X for A:");
            System.out.println(zombieALocationX);
            System.out.print("MoveArray for A: ");
            System.out.println(Arrays.toString(moveArray));
            
            Random randomMoveInt = new Random();
            while ((zombieAMoved == false) & (!areAllFalse(moveArray))) {
                int zombieAMove = randomMoveInt.nextInt(3-0+1)+0;
                if (moveArray[zombieAMove] == true) {
                    if (zombieAMove == 0) {
                        zombieALocationY -= 1;
                    }
                    else if (zombieAMove == 1){
                        zombieALocationY += 1;
                    }
                    else if (zombieAMove == 2){
                        zombieALocationX -= 1;
                    }
                    else if (zombieAMove == 3){
                        zombieALocationX += 1;
                    }
                    zombieAMoved = true;
                }
            }
            gameBoard[zombieALocationY][zombieALocationX] = 1;
            
        }
        else {
            //TODO: add alerted moving decisions
            System.out.println("A Alerted");
        }
        
        if (zombieBAlert == false) {
            
            //In case human moved to zombie's old location.
            if (gameBoard[zombieBLocationY][zombieBLocationX] == 1) {
                gameBoard[zombieBLocationY][zombieBLocationX] = 0;
            }
            
            boolean[] moveArray = moveValidator(zombieBLocationX, zombieBLocationY);
            System.out.print("Original Y for B:");
            System.out.println(zombieBLocationY);
            System.out.print("Original X for B:");
            System.out.println(zombieBLocationX);
            System.out.print("MoveArray for A: ");
            System.out.print("MoveArray for B: ");
            System.out.println(Arrays.toString(moveArray));
            System.out.print("========Zombie Move Ends========");
            System.out.println();System.out.println();
            Random randomMoveInt = new Random();
            while ((zombieBMoved == false) & (!areAllFalse(moveArray))) {
                int zombieBMove = randomMoveInt.nextInt(3-0+1)+0;
                if (moveArray[zombieBMove] == true) {
                    if (zombieBMove == 0) {
                        zombieBLocationY -= 1;
                    }
                    else if (zombieBMove == 1){
                        zombieBLocationY += 1;
                    }
                    else if (zombieBMove == 2){
                        zombieBLocationX -= 1;
                    }
                    else if (zombieBMove == 3){
                        zombieBLocationX += 1;
                    }
                    zombieBMoved = true;
                }
            }
            gameBoard[zombieBLocationY][zombieBLocationX] = 1;
        }
        else {
            //TODO: add alerted moving decisions
            System.out.println("B Alerted");
        }
    }
    
    public boolean[] moveValidator(int x, int y) {
        //Return boolean values determining whether moving to a location is valid.
        
        boolean[] validMoveArray = new boolean[4];
        
        if (y>0) {
            if ((gameBoard[y-1][x] != 1) & (gameBoard[y-1][x] != 3)) {
                boolean canMoveUp = true;
                validMoveArray[0] = canMoveUp;
            }
        }
        else {
            boolean canMoveUp = false;
            validMoveArray[0] = canMoveUp;
        }
        
        if (y<9) {
            if ((gameBoard[y+1][x] != 1) & (gameBoard[y+1][x] != 3)){
                boolean canMoveDown = true;
                validMoveArray[1] = canMoveDown;
            }
        }
        else {
            boolean canMoveDown = false;
            validMoveArray[1] = canMoveDown;
        }
        
        if (x>0) {
            if ((gameBoard[y][x-1] != 1) & (gameBoard[y][x-1] != 3)){
                boolean canMoveLeft = true;
                validMoveArray[2] = canMoveLeft;
            }
        }
        else {
            boolean canMoveLeft = false;
            validMoveArray[2] = canMoveLeft;
        }
        
        if (x<9) {
            if ((gameBoard[y][x+1] != 1) & (gameBoard[y][x+1] != 3)) {
                boolean canMoveRight = true;
                validMoveArray[3] = canMoveRight;
            }
        }
        else {
            boolean canMoveRight = false;
            validMoveArray[3] = canMoveRight;
        }
        return validMoveArray;
    }
    
    public void winning(){
        //Display player winning message, end the game.
        playerWon = true;
        topPanelA.removeAll();
        JTextArea TopPanelMessage = new JTextArea("Congratulations, you have escaped the undead and reached the extraction point!");
        topPanelA.setBackground(Color.GREEN);
        TopPanelMessage.setBackground(Color.GREEN);
        topPanelA.add(TopPanelMessage);
        topPanelA.repaint();
        topPanelA.updateUI();
    }
    
    public void lost(){
        //Display player losing message, end the game.
        playerLost = true;
        topPanelA.removeAll();
        JTextArea TopPanelMessage = new JTextArea("Unfortunately zombies caught you! You are lost.");
        topPanelA.setBackground(Color.RED);
        TopPanelMessage.setBackground(Color.RED);
        topPanelA.add(TopPanelMessage);
        topPanelA.repaint();
        topPanelA.updateUI();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        
        int keyCode = e.getKeyCode();
        
        if (keyCode == 38) {
            if (humanLocationY > 0 & playerWon == false & playerLost == false) {
                gameBoard[humanLocationY][humanLocationX] = 0;
                humanLocationY -= 1;
                gameBoard[humanLocationY][humanLocationX] = 2;
                updateBoard();
            }
        }
        else if (keyCode == 40) {
            if (humanLocationY < 9 & playerWon == false & playerLost == false) {
                gameBoard[humanLocationY][humanLocationX] = 0;
                humanLocationY += 1;
                gameBoard[humanLocationY][humanLocationX] = 2;
                updateBoard();
            }
        }
        else if (keyCode == 37) {
            if (humanLocationX > 0 & playerWon == false & playerLost == false) {
                gameBoard[humanLocationY][humanLocationX] = 0;
                humanLocationX -= 1;
                gameBoard[humanLocationY][humanLocationX] = 2;
                updateBoard();
            }
        }
        else if (keyCode == 39) {
            if (humanLocationX < 9 & playerWon == false & playerLost == false) {
                gameBoard[humanLocationY][humanLocationX] = 0;
                humanLocationX += 1;
                gameBoard[humanLocationY][humanLocationX] = 2;
                updateBoard();
            }
        }
        
    }
    
    public void actionPerformed(ActionEvent e)
    {
        JButton source = (JButton)e.getSource();
        if (source == NewGameButton) {
            System.out.println("Resetting the game...");
            reset();
        }
    }
    
    
    @Override
    public void keyReleased(KeyEvent e) {
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }
    
    public static boolean areAllFalse(boolean[] array){
        //shortcut to check if all elements in an array is false
        for(boolean b : array) if(b) return false;
        return true;
    }
    
    public void reset(){
        //reset game
        this.getContentPane().removeAll();
        topPanelA.removeAll();
        CenterPanel.removeAll();
        buttomPanel.removeAll();
        humanLocationX = 0;
        humanLocationY = 9;
        destinationX = 10;
        destinationY = 0;
        zombieALocationX = 0;
        zombieALocationY = 0;
        zombieBLocationX = 0;
        zombieBLocationY = 0;
        zombieAAlert = false;
        zombieBAlert = false;
        playerWon = false;
        playerLost = false;
        initialise();
        this.getContentPane().add(topPanelA, BorderLayout.NORTH);
        this.getContentPane().add(CenterPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttomPanel, BorderLayout.SOUTH);
        topPanelA.repaint();
        topPanelA.updateUI();
        CenterPanel.repaint();
        CenterPanel.updateUI();
        buttomPanel.repaint();
        buttomPanel.updateUI();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        MainAppLayout mainWindow = new MainAppLayout();
        mainWindow.setVisible(true);
    }
    
}

