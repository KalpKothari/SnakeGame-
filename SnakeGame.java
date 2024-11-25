import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static int INITIAL_DELAY = 150; // Changed to static for modification
    static final int SPEED_INCREMENT = 20; // New constant for speed increment
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX, appleY, greenAppleX, greenAppleY;
    boolean greenAppleVisible = false;
    int greenAppleTimer = 0;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;
    int level = 1;
    boolean levelUp = false; // To control level display
    int levelDisplayTimer = 0;

    public SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        level = 1; // Set initial level
        levelUp = true; // Trigger level display for Level 1
        levelDisplayTimer = 60; // Display Level 1 for 3 seconds (60 ticks at 20 FPS)
        timer = new Timer(INITIAL_DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Draw grid lines (Optional)
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            // Draw the red apple
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw the green apple if visible
            if (greenAppleVisible) {
                g.setColor(Color.green);
                g.fillOval(greenAppleX, greenAppleY, UNIT_SIZE, UNIT_SIZE);
            }

            // Draw the snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green); // Snake's head
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0)); // Snake's body
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            // Draw the score
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());

            // Show level in the middle
            if (levelUp) {
                g.setColor(Color.blue);
                g.setFont(new Font("Ink Free", Font.BOLD, 50));
                metrics = getFontMetrics(g.getFont());
                g.drawString("Level " + level, (SCREEN_WIDTH - metrics.stringWidth("Level " + level)) / 2, SCREEN_HEIGHT / 2);
            }

        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

        // Randomly decide when to show the green apple after some time
        if (random.nextInt(3) == 0 && applesEaten >= 5) { // Green apple appears randomly after score 5
            newGreenApple();
        }
    }

    public void newGreenApple() {
        // Place green apple near the red apple, but not too close
        int offsetX = (random.nextInt(2) == 0) ? UNIT_SIZE : -UNIT_SIZE;
        int offsetY = (random.nextInt(2) == 0) ? UNIT_SIZE : -UNIT_SIZE;

        greenAppleX = Math.min(Math.max(appleX + offsetX, 0), SCREEN_WIDTH - UNIT_SIZE);
        greenAppleY = Math.min(Math.max(appleY + offsetY, 0), SCREEN_HEIGHT - UNIT_SIZE);

        greenAppleVisible = true;
        greenAppleTimer = 100; // Green apple will disappear after 100 game ticks if not eaten
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }

        // Handle wall passing through
        if (x[0] < 0) {
            x[0] = SCREEN_WIDTH - UNIT_SIZE;
        } else if (x[0] >= SCREEN_WIDTH) {
            x[0] = 0;
        }
        if (y[0] < 0) {
            y[0] = SCREEN_HEIGHT - UNIT_SIZE;
        } else if (y[0] >= SCREEN_HEIGHT) {
            y[0] = 0;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            checkLevelUp();
        }

        // Check if the snake eats the green apple
        if (greenAppleVisible && (x[0] == greenAppleX) && (y[0] == greenAppleY)) {
            bodyParts--;
            applesEaten--;
            greenAppleVisible = false;
        }
    }

    public void checkLevelUp() {
        if (applesEaten == 10 || applesEaten == 20 || applesEaten == 30) {
            level++;
            levelUp = true;
            levelDisplayTimer = 60; // Show level for 3 seconds (60 ticks at 20 FPS)

            // Increase speed with level
            if (INITIAL_DELAY > 50) { // Minimum delay
                INITIAL_DELAY -= SPEED_INCREMENT;
                timer.setDelay(INITIAL_DELAY);
            }
        }
    }

    public void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        // Score
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Final Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Final Score: " + applesEaten)) / 2, g.getFont().getSize());

        // Game Over text
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        // Restart instruction
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString("Press Space to Restart", (SCREEN_WIDTH - metrics3.stringWidth("Press Space to Restart")) / 2, SCREEN_HEIGHT / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();

            // Handle green apple visibility timer
            if (greenAppleVisible) {
                greenAppleTimer--;
                if (greenAppleTimer <= 0) {
                    greenAppleVisible = false;
                }
            }

            // Handle level display timer
            if (levelUp) {
                levelDisplayTimer--;
                if (levelDisplayTimer <= 0) {
                    levelUp = false; // Hide level after display time
                }
            }
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if (!running) {
                        restartGame(); // Restart the game when space is pressed after Game Over
                    }
                    break;
            }
        }
    }

    public void restartGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        running = true;
        level = 1;
        INITIAL_DELAY = 150; // Reset delay to initial value
        levelUp = true; // Trigger level display for Level 1
        levelDisplayTimer = 60;
        newApple();
        timer.setDelay(INITIAL_DELAY);
        timer.restart();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        SnakeGame gamePanel = new SnakeGame();
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Snake Game");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}