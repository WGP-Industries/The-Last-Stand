
// import statements
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GameWindow extends JFrame
implements ActionListener,
KeyListener,
MouseListener {
    // declare instance variables for user interface objects

    // declare text fields




    // declare buttons

    private JButton startB;
    private JButton exitB;

    private Container c;

    private JPanel mainPanel;
    private GamePanel gamePanel;
    private int playerDirection = 2;

    @SuppressWarnings({
        "unchecked"
    })
    public GameWindow() {

        setTitle("Treasure Defender");
        setSize(800, 520);

        setLocationRelativeTo(null);

        // create user interface objects





        // create buttons

        startB = new JButton("Start Game");

        exitB = new JButton("Exit");

        // add listener to each button (same as the current object)

        startB.addActionListener(this);


        exitB.addActionListener(this);


        // create mainPanel

        mainPanel = new JPanel(new BorderLayout());

        GridLayout gridLayout;


        // create the gamePanel for game entities

        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(800, 400));



        // create infoPanel

		JPanel infoPanel = new JPanel();
		infoPanel.setBackground(Color.ORANGE);
		infoPanel.setPreferredSize(new Dimension(800, 30));

		infoPanel.add(gamePanel.getWaveLabel());
		infoPanel.add(Box.createHorizontalStrut(100));
		infoPanel.add(gamePanel.getScoreLabel());


        // create buttonPanel

        JPanel buttonPanel = new JPanel();
        gridLayout = new GridLayout(1, 4);
        buttonPanel.setLayout(gridLayout);

        // add buttons to buttonPanel

        buttonPanel.add(startB);


        buttonPanel.add(exitB);

        // add sub panels with GUI objects to mainPanel and set its colour


        mainPanel.add(infoPanel, BorderLayout.NORTH);
		mainPanel.add(gamePanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);	
        mainPanel.setBackground(Color.GRAY);

        // set up mainPanel to respond to keyboard and mouse

        gamePanel.addMouseListener(this);
        mainPanel.addKeyListener(this);



        // add mainPanel to window surface

        c = getContentPane();
        c.add(mainPanel);

        // set properties of window

        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);

        // create game entities

        gamePanel.createGameEntities();



        // ensure keyboard focus
        mainPanel.setFocusable(true);
        mainPanel.requestFocusInWindow();
    }


    // implement single method in ActionListener interface

    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();


        if (command.equals(startB.getText())) {
            gamePanel.startGame();
            mainPanel.requestFocusInWindow();
        }

        if (command.equals(exitB.getText()))
            System.exit(0);

        mainPanel.requestFocusInWindow();
    }

    // implement methods in KeyListener interface

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();


        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                playerDirection = 1;
                gamePanel.updatePlayer(playerDirection);
                break;

            case KeyEvent.VK_RIGHT:
                playerDirection = 2;
                gamePanel.updatePlayer(playerDirection);
                break;

            case KeyEvent.VK_SPACE:
                gamePanel.shootBullet(playerDirection);
                break;
        }
    }


    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    // implement methods in MouseListener interface

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}



}