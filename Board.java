import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Board extends JPanel implements ActionListener, KeyListener {

    // controls the delay between each tick in ms
    private final int DELAY = 25;
    // controls the size of the board
    public static final int TILE_SIZE = 50;
    public static final int ROWS = 12;
    public static final int COLUMNS = 18;
    // controls how many coins appear on the board
    public static final int NUM_COINS = 5;
    // suppress serialization warning
    private static final long serialVersionUID = 490905409104883233L;
    
    // keep a reference to the timer object that triggers actionPerformed() in
    // case we need access to it in another method
    private Timer timer;
    // objects that appear on the game board
    private Player player;
    private ArrayList<Coin> coins;

    // Inside the Board class

    private void drawCelebration(Graphics g) {
        // Draw your celebration graphic
        // For simplicity, let's just display a congratulatory message for now
        String celebrationText = "Congratulations! You won!";
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 0, 0)); // Red color
        g2d.setFont(new Font("Lato", Font.BOLD, 40));

        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
        Rectangle rect = new Rectangle(0, 0, TILE_SIZE * COLUMNS, TILE_SIZE * ROWS);

        int x = rect.x + (rect.width - metrics.stringWidth(celebrationText)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();

        g2d.drawString(celebrationText, x, y);
    }


    private void checkGameCompletion() {
        // Check if all coins are collected
        if (coins.isEmpty()) {
            // You can display a celebration graphic or perform other actions here
            // For now, let's restart the game
            drawCelebration(getGraphics());
            restartGame();
        }
    
        // Check if a certain score is reached
        int targetScore = 1000; // Set your target score here
        if (Integer.parseInt(player.getScore()) >= targetScore) {
            // You can display a celebration graphic or perform other actions here
            // For now, let's restart the game
            drawCelebration(getGraphics());
            restartGame();
        }
    }

    private int gameTime; // in seconds

    private void drawClock(Graphics g) {
        String clockText = "Time: " + gameTime + "s";
        // Draw the clock text similar to drawScore method
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(30, 201, 139));
        g2d.setFont(new Font("Lato", Font.BOLD, 25));
    
        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
        Rectangle rect = new Rectangle(0, 0, TILE_SIZE * COLUMNS, TILE_SIZE * ROWS);// change the position
    
        int x = rect.x + (rect.width - metrics.stringWidth(clockText)) / 2;
        int y = rect.y + metrics.getHeight();// change the y-coordinate
    
        g2d.drawString(clockText, x, y);
    }
    
    private void updateClock() {
        gameTime++;
    }
    
    
    private void restartGame() {
        // Reset player position, score, and refill the coins
        player = new Player();
        coins = populateCoins();
    }
    

    public Board() {
        // set the game board size
        setPreferredSize(new Dimension(TILE_SIZE * COLUMNS, TILE_SIZE * ROWS));
        // set the game board background color
        setBackground(new Color(232, 232, 232));

        // initialize the game state
        player = new Player();
        coins = populateCoins();

        // this timer will call the actionPerformed() method every DELAY ms
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // this method is called by the timer every DELAY ms.
        // use this space to update the state of your game or animation
        // before the graphics are redrawn.

        // prevent the player from disappearing off the board
        player.tick();

        // give the player points for collecting coins
        collectCoins();

        checkGameCompletion();
        updateClock();

        // calling repaint() will trigger paintComponent() to run again,
        // which will refresh/redraw the graphics.
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // when calling g.drawImage() we can use "this" for the ImageObserver 
        // because Component implements the ImageObserver interface, and JPanel 
        // extends from Component. So "this" Board instance, as a Component, can 
        // react to imageUpdate() events triggered by g.drawImage()

        // draw our graphics.
        drawBackground(g);
        drawScore(g);
        for (Coin coin : coins) {
            coin.draw(g, this);
        }
        player.draw(g, this);

        // draw our clock
        drawClock(g);

        // this smooths out animations on some systems
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // this is not used but must be defined as part of the KeyListener interface
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // react to key down events
        player.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // react to key up events
    }

    private void drawBackground(Graphics g) {
        // draw a checkered background
        g.setColor(new Color(214, 214, 214));
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                // only color every other tile
                if ((row + col) % 2 == 1) {
                    // draw a square tile at the current row/column position
                    g.fillRect(
                        col * TILE_SIZE, 
                        row * TILE_SIZE, 
                        TILE_SIZE, 
                        TILE_SIZE
                    );
                }
            }    
        }
    }

    private void drawScore(Graphics g) {
        // set the text to be displayed
        String text = "$" + player.getScore();
        // we need to cast the Graphics to Graphics2D to draw nicer text
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        // set the text color and font
        g2d.setColor(new Color(30, 201, 139));
        g2d.setFont(new Font("Lato", Font.BOLD, 25));
        // draw the score in the bottom center of the screen
        // https://stackoverflow.com/a/27740330/4655368
        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
        // the text will be contained within this rectangle.
        // here I've sized it to be the entire bottom row of board tiles
        Rectangle rect = new Rectangle(0, TILE_SIZE * (ROWS - 1), TILE_SIZE * COLUMNS, TILE_SIZE);
        // determine the x coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // determine the y coordinate for the text
        // (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // draw the string
        g2d.drawString(text, x, y);
    }

    private ArrayList<Coin> populateCoins() {
        ArrayList<Coin> coinList = new ArrayList<>();
        Random rand = new Random();

        // create the given number of coins in random positions on the board.
        // note that there is not check here to prevent two coins from occupying the same
        // spot, nor to prevent coins from spawning in the same spot as the player
        for (int i = 0; i < NUM_COINS; i++) {
            int coinX = rand.nextInt(COLUMNS);
            int coinY = rand.nextInt(ROWS);
            coinList.add(new Coin(coinX, coinY));
        }

        return coinList;
    }

    private void collectCoins() {
        // allow player to pickup coins
        ArrayList<Coin> collectedCoins = new ArrayList<>();
        for (Coin coin : coins) {
            // if the player is on the same tile as a coin, collect it
            if (player.getPos().equals(coin.getPos())) {
                // give the player some points for picking this up
                player.addScore(100);
                collectedCoins.add(coin);
            }
        }
        // remove collected coins from the board
        coins.removeAll(collectedCoins);
    }

}
