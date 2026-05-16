import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;

public class AdvancedAdventureGameGUI {

    private JFrame frame;
    private JTextArea storyArea;
    private JButton choice1Button, choice2Button, choice3Button, saveButton, loadButton;
    private String currentScene;
    private String characterClass;
    private int health, maxHealth, xp, level;
    private HashMap<String, Integer> inventory;

    public AdvancedAdventureGameGUI() {
        // Setup the main frame
        frame = new JFrame("Advanced Adventure Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        // Setup the story display
        storyArea = new JTextArea();
        storyArea.setEditable(false);
        storyArea.setLineWrap(true);
        storyArea.setWrapStyleWord(true);
        storyArea.setFont(new Font("Serif", Font.PLAIN, 16));

        // Setup buttons for user interaction
        choice1Button = new JButton();
        choice2Button = new JButton();
        choice3Button = new JButton("Inventory");
        saveButton = new JButton("Save Game");
        loadButton = new JButton("Load Game");

        choice1Button.addActionListener(new ChoiceHandler(1));
        choice2Button.addActionListener(new ChoiceHandler(2));
        choice3Button.addActionListener(e -> displayInventory());
        saveButton.addActionListener(e -> saveGame());
        loadButton.addActionListener(e -> loadGame());

        // Layout components
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(storyArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 5));
        buttonPanel.add(choice1Button);
        buttonPanel.add(choice2Button);
        buttonPanel.add(choice3Button);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Initialize game
        initializeGame();
        frame.setVisible(true);
    }

    public void initializeGame() {
        inventory = new HashMap<>();
        level = 1;
        xp = 0;
        maxHealth = 100;
        health = maxHealth;
        currentScene = "chooseCharacter";
        updateScene();
    }

    public void updateScene() {
        switch (currentScene) {
            case "chooseCharacter":
                storyArea.setText("Choose your character class:\n\n1. Warrior (High Health, Low Magic)\n2. Mage (Low Health, High Magic)\n3. Thief (Balanced Stats)");
                choice1Button.setText("Warrior");
                choice2Button.setText("Mage");
                choice3Button.setText("Thief");
                break;

            case "start":
                storyArea.setText("You are standing at the entrance of a mysterious dungeon.\n\nHealth: " + health + "\nLevel: " + level + " XP: " + xp +
                        "\n\nWhat will you do?");
                choice1Button.setText("Enter the dungeon");
                choice2Button.setText("Walk away");
                break;

            case "puzzleRoom":
                storyArea.setText("You enter a room with a puzzle on the wall.\n\nSolve this: What has to be broken before you can use it?\n");
                choice1Button.setText("Egg");
                choice2Button.setText("Door");
                break;

            case "combatRoom":
                storyArea.setText("A wild Goblin appears! Prepare for combat!\n\nHealth: " + health + "\nGoblin's Health: 50\n\nWhat will you do?");
                choice1Button.setText("Attack");
                choice2Button.setText("Defend");
                choice3Button.setText("Use Item");
                break;

            case "victory":
                storyArea.setText("You defeated the Goblin! You gained 50 XP.\n\nWhat will you do next?");
                xp += 50;
                checkLevelUp();
                choice1Button.setText("Explore further");
                choice2Button.setText("Exit dungeon");
                break;

            case "gameOver":
                storyArea.setText("Your health has reached zero. You didn't survive the adventure.\n\nGAME OVER.");
                choice1Button.setText("Restart");
                choice2Button.setText("Exit");
                break;

            case "winGame":
                storyArea.setText("Congratulations! You escaped the dungeon with treasures and survived the adventure!");
                choice1Button.setText("Play again");
                choice2Button.setText("Exit");
                break;

            default:
                storyArea.setText("An unexpected error occurred. Restarting...");
                initializeGame();
                break;
        }
    }

    private void checkLevelUp() {
        if (xp >= level * 100) {
            level++;
            maxHealth += 20;
            health = maxHealth;
            storyArea.append("\n\nYou leveled up! Level: " + level + ", Max Health: " + maxHealth);
        }
    }

    private void displayInventory() {
        StringBuilder inventoryDisplay = new StringBuilder("Inventory:\n");
        inventory.forEach((item, count) -> inventoryDisplay.append(item).append(": ").append(count).append("\n"));
        JOptionPane.showMessageDialog(frame, inventoryDisplay.toString(), "Inventory", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveGame() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
            oos.writeObject(currentScene);
            oos.writeObject(characterClass);
            oos.writeInt(health);
            oos.writeInt(maxHealth);
            oos.writeInt(xp);
            oos.writeInt(level);
            oos.writeObject(inventory);
            JOptionPane.showMessageDialog(frame, "Game progress saved!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to save game!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("savegame.dat"))) {
            currentScene = (String) ois.readObject();
            characterClass = (String) ois.readObject();
            health = ois.readInt();
            maxHealth = ois.readInt();
            xp = ois.readInt();
            level = ois.readInt();
            inventory = (HashMap<String, Integer>) ois.readObject();
            JOptionPane.showMessageDialog(frame, "Game progress loaded!");
            updateScene();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(frame, "Failed to load game!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ChoiceHandler implements ActionListener {
        private int choice;

        public ChoiceHandler(int choice) {
            this.choice = choice;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (currentScene) {
                case "chooseCharacter":
                    if (choice == 1) characterClass = "Warrior";
                    else if (choice == 2) characterClass = "Mage";
                    else characterClass = "Thief";
                    currentScene = "start";
                    break;

                case "start":
                    currentScene = (choice == 1) ? "puzzleRoom" : "gameOver";
                    break;

                case "puzzleRoom":
                    if (choice == 1) {
                        currentScene = "combatRoom";
                        inventory.put("Potion", inventory.getOrDefault("Potion", 0) + 1);
                        storyArea.append("\n\nCorrect! You found a Potion.");
                    } else {
                        health -= 20;
                        if (health <= 0) currentScene = "gameOver";
                    }
                    break;

                case "combatRoom":
                    if (choice == 1) {
                        currentScene = "victory";
                    } else if (choice == 2) {
                        health -= 10;
                        if (health <= 0) currentScene = "gameOver";
                    } else {
                        inventory.put("Potion", inventory.getOrDefault("Potion", 0) - 1);
                        health = Math.min(maxHealth, health + 30);
                    }
                    break;

                case "gameOver":
                case "winGame":
                    initializeGame();
                    break;

                default:
                    initializeGame();
                    break;
            }
            updateScene();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdvancedAdventureGameGUI::new);
    }
}