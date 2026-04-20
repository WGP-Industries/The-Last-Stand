import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GameWindow extends JFrame
        implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    private JButton startB;
    private JButton exitB;

    private Container c;
    private JPanel mainPanel;
    private GamePanel gamePanel;

    private int mouseX = 400;
    private int mouseY = 375;

    private int playerDirection = 2;

    public GameWindow() {
        setTitle("Treasure Defender");
        setSize(800, 520);
        setLocationRelativeTo(null);

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

        // HUD
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.ORANGE);
        infoPanel.setPreferredSize(new Dimension(800, 30));
        infoPanel.add(gamePanel.getWaveLabel());
        infoPanel.add(Box.createHorizontalStrut(40));
        infoPanel.add(gamePanel.getScoreLabel());
        infoPanel.add(Box.createHorizontalStrut(40));
        infoPanel.add(gamePanel.getBulletLabel());

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(startB);
        buttonPanel.add(exitB);

        // add sub panels with GUI objects to mainPanel and set its colour


        mainPanel.add(infoPanel, BorderLayout.NORTH);
		mainPanel.add(gamePanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);	
        mainPanel.setBackground(Color.GRAY);

        gamePanel.addMouseMotionListener(this);
        gamePanel.addMouseListener(this);
        mainPanel.addKeyListener(this);

        c = getContentPane();
        c.add(mainPanel);

        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        gamePanel.createGameEntities();

        mainPanel.setFocusable(true);
        mainPanel.requestFocusInWindow();
    }


    @Override
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

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {

            // Movement
            case KeyEvent.VK_LEFT:
                playerDirection = 1;
                gamePanel.updatePlayer(playerDirection);
                break;
            case KeyEvent.VK_RIGHT:
                playerDirection = 2;
                gamePanel.updatePlayer(playerDirection);
                break;

            // Fire toward current mouse position
            case KeyEvent.VK_SPACE:
                gamePanel.shootBullet(mouseX, mouseY);
                break;

            // Bullet type selection (1–9)
            case KeyEvent.VK_1: gamePanel.setBulletType(BulletType.BASIC);     break;
            case KeyEvent.VK_2: gamePanel.setBulletType(BulletType.FIRE);      break;
            case KeyEvent.VK_3: gamePanel.setBulletType(BulletType.FREEZE);    break;
            case KeyEvent.VK_4: gamePanel.setBulletType(BulletType.ELECTRIC);  break;
            case KeyEvent.VK_5: gamePanel.setBulletType(BulletType.SPIRIT);    break;
            case KeyEvent.VK_6: gamePanel.setBulletType(BulletType.RAPID);     break;
            case KeyEvent.VK_7: gamePanel.setBulletType(BulletType.PIERCING);  break;
            case KeyEvent.VK_8: gamePanel.setBulletType(BulletType.EXPLOSIVE); break;
            case KeyEvent.VK_9: gamePanel.setBulletType(BulletType.TELEPORT);  break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped   (KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        gamePanel.shootBullet(mouseX, mouseY);
    }


    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        gamePanel.shootBullet(mouseX, mouseY);
        mainPanel.requestFocusInWindow(); // keep keyboard focus after clicking
    }

    @Override public void mouseClicked (MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered (MouseEvent e) {}
    @Override public void mouseExited  (MouseEvent e) {}
}
