import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * HorseCustomizer provides a graphical interface for customizing horses.
 * It allows users to set breed, color, equipment, and other attributes.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class HorseCustomizer extends JDialog {
    // Horse being customized
    private Horse horse;
    
    // UI components
    private JTextField nameField;
    private JTextField symbolField;
    private JTextField displaySymbolField;
    private JSlider confidenceSlider;
    private JComboBox<HorseBreed> breedCombo;
    private JComboBox<CoatColor> colorCombo;
    private JComboBox<HorseEquipment.SaddleType> saddleCombo;
    private JComboBox<HorseEquipment.HorseshoeType> horseshoeCombo;
    private JComboBox<HorseEquipment.AccessoryType> accessoryCombo;
    private JPanel colorPreviewPanel;
    private JPanel statsPanel;
    
    // Emoji map for quick selection
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    static {
        EMOJI_MAP.put("Horse", "🐎");
        EMOJI_MAP.put("Racing Horse", "🏇");
        EMOJI_MAP.put("Unicorn", "🦄");
        EMOJI_MAP.put("Zebra", "🦓");
        EMOJI_MAP.put("Pegasus", "⏃");
        EMOJI_MAP.put("Chess Knight", "♞");
        EMOJI_MAP.put("Star", "★");
        EMOJI_MAP.put("Crown", "👑");
        EMOJI_MAP.put("Lightning", "⚡");
        EMOJI_MAP.put("Fire", "🔥");
    }
    
    /**
     * Constructor for creating a new horse
     * 
     * @param parent The parent frame
     */
    public HorseCustomizer(JFrame parent) {
        super(parent, "Create New Horse", true);
        
        // Create a default horse to customize
        this.horse = new Horse('H', "New Horse", 0.7);
        this.horse.setDisplaySymbol("🐎");
        
        setupUI();
    }
    
    /**
     * Constructor for editing an existing horse
     * 
     * @param parent The parent frame
     * @param existingHorse The horse to edit
     */
    public HorseCustomizer(JFrame parent, Horse existingHorse) {
        super(parent, "Edit Horse", true);
        
        this.horse = existingHorse;
        
        setupUI();
        
        // Load the existing horse's data into the UI
        loadHorseData();
    }
    
    /**
     * Set up the user interface
     */
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Main panel with sections
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Basic info panel
        JPanel basicPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        basicPanel.setBorder(BorderFactory.createTitledBorder("Basic Information"));
        
        // Name
        basicPanel.add(new JLabel("Name:"));
        nameField = new JTextField(20);
        basicPanel.add(nameField);
        
        // Symbol
        basicPanel.add(new JLabel("Symbol (single character):"));
        symbolField = new JTextField(1);
        basicPanel.add(symbolField);
        
        // Display symbol
        basicPanel.add(new JLabel("Display Symbol:"));
        JPanel symbolPanel = new JPanel(new BorderLayout());
        displaySymbolField = new JTextField(4);
        symbolPanel.add(displaySymbolField, BorderLayout.CENTER);
        
        // Emoji selector button
        JButton emojiButton = new JButton("📋");
        emojiButton.setToolTipText("Select emoji");
        emojiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEmojiSelector();
            }
        });
        symbolPanel.add(emojiButton, BorderLayout.EAST);
        basicPanel.add(symbolPanel);
        
        // Confidence
        basicPanel.add(new JLabel("Confidence (0.0-1.0):"));
        confidenceSlider = new JSlider(0, 100, 70);
        confidenceSlider.setPaintTicks(true);
        confidenceSlider.setPaintLabels(true);
        confidenceSlider.setMajorTickSpacing(20);
        confidenceSlider.addChangeListener(e -> updateStats());
        basicPanel.add(confidenceSlider);
        
        // Physical attributes panel
        JPanel physicalPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        physicalPanel.setBorder(BorderFactory.createTitledBorder("Physical Attributes"));
        
        // Breed
        physicalPanel.add(new JLabel("Breed:"));
        breedCombo = new JComboBox<>(HorseBreed.ALL_BREEDS);
        breedCombo.addActionListener(e -> updateStats());
        physicalPanel.add(breedCombo);
        
        // Coat color
        physicalPanel.add(new JLabel("Coat Color:"));
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorCombo = new JComboBox<>(CoatColor.ALL_COLORS);
        colorCombo.addActionListener(e -> {
            updateColorPreview();
            updateStats();
        });
        colorPanel.add(colorCombo, BorderLayout.CENTER);
        
        // Color preview
        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setPreferredSize(new Dimension(30, 20));
        colorPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        colorPanel.add(colorPreviewPanel, BorderLayout.EAST);
        physicalPanel.add(colorPanel);
        
        // Equipment panel
        JPanel equipmentPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        equipmentPanel.setBorder(BorderFactory.createTitledBorder("Equipment"));
        
        // Saddle
        equipmentPanel.add(new JLabel("Saddle:"));
        saddleCombo = new JComboBox<>(HorseEquipment.SaddleType.values());
        saddleCombo.addActionListener(e -> updateStats());
        equipmentPanel.add(saddleCombo);
        
        // Horseshoes
        equipmentPanel.add(new JLabel("Horseshoes:"));
        horseshoeCombo = new JComboBox<>(HorseEquipment.HorseshoeType.values());
        horseshoeCombo.addActionListener(e -> updateStats());
        equipmentPanel.add(horseshoeCombo);
        
        // Accessory
        equipmentPanel.add(new JLabel("Accessory:"));
        accessoryCombo = new JComboBox<>(HorseEquipment.AccessoryType.values());
        accessoryCombo.addActionListener(e -> updateStats());
        equipmentPanel.add(accessoryCombo);
        
        // Stats panel
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Calculated Stats"));
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveHorse();
                dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add all panels to main panel
        mainPanel.add(basicPanel);
        mainPanel.add(physicalPanel);
        mainPanel.add(equipmentPanel);
        mainPanel.add(statsPanel);
        
        // Add panels to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize color preview and stats
        updateColorPreview();
        updateStats();
        
        // Pack and center
        pack();
        setLocationRelativeTo(getParent());
    }
    
    /**
     * Load the existing horse's data into the UI
     */
    private void loadHorseData() {
        nameField.setText(horse.getName());
        symbolField.setText(String.valueOf(horse.getSymbol()));
        displaySymbolField.setText(horse.getDisplaySymbol());
        confidenceSlider.setValue((int)(horse.getConfidence() * 100));
        
        // Select breed
        for (int i = 0; i < breedCombo.getItemCount(); i++) {
            if (breedCombo.getItemAt(i).getName().equals(horse.getBreed().getName())) {
                breedCombo.setSelectedIndex(i);
                break;
            }
        }
        
        // Select coat color
        for (int i = 0; i < colorCombo.getItemCount(); i++) {
            if (colorCombo.getItemAt(i).getName().equals(horse.getCoatColor().getName())) {
                colorCombo.setSelectedIndex(i);
                break;
            }
        }
        
        // Select equipment
        saddleCombo.setSelectedItem(horse.getEquipment().getSaddle());
        horseshoeCombo.setSelectedItem(horse.getEquipment().getHorseshoes());
        accessoryCombo.setSelectedItem(horse.getEquipment().getAccessory());
    }
    
    /**
     * Save the custom horse data
     */
    private void saveHorse() {
        // Basic info
        String name = nameField.getText();
        char symbol = symbolField.getText().isEmpty() ? 'H' : symbolField.getText().charAt(0);
        String displaySymbol = displaySymbolField.getText().isEmpty() ? 
                               String.valueOf(symbol) : displaySymbolField.getText();
        double confidence = confidenceSlider.getValue() / 100.0;
        
        // Attributes
        HorseBreed breed = (HorseBreed)breedCombo.getSelectedItem();
        CoatColor coatColor = (CoatColor)colorCombo.getSelectedItem();
        
        // Equipment
        HorseEquipment equipment = new HorseEquipment(
            (HorseEquipment.SaddleType)saddleCombo.getSelectedItem(),
            (HorseEquipment.HorseshoeType)horseshoeCombo.getSelectedItem(),
            (HorseEquipment.AccessoryType)accessoryCombo.getSelectedItem()
        );
        
        // Update the horse
        horse.setSymbol(symbol);
        horse.setDisplaySymbol(displaySymbol);
        horse.setConfidence(confidence);
        horse.setBreed(breed);
        horse.setCoatColor(coatColor);
        horse.setEquipment(equipment);
        
        // If it's a new horse, we need to set the name
        if (!name.equals(horse.getName())) {
            // Since Horse doesn't have a setName method, we'll create a new horse
            // This is only used when creating a brand new horse
            if (horse.getName().equals("New Horse")) {
                horse = new Horse(symbol, name, displaySymbol, confidence, breed, coatColor, equipment);
            }
        }
    }
    
    /**
     * Update the color preview based on the selected coat color
     */
    private void updateColorPreview() {
        CoatColor selectedColor = (CoatColor)colorCombo.getSelectedItem();
        if (selectedColor != null) {
            colorPreviewPanel.setBackground(selectedColor.getColor());
        }
    }
    
    /**
     * Update the stats display based on current selections
     */
    private void updateStats() {
        statsPanel.removeAll();
        
        // Get current selections
        HorseBreed selectedBreed = (HorseBreed)breedCombo.getSelectedItem();
        HorseEquipment.SaddleType selectedSaddle = (HorseEquipment.SaddleType)saddleCombo.getSelectedItem();
        HorseEquipment.HorseshoeType selectedHorseshoes = (HorseEquipment.HorseshoeType)horseshoeCombo.getSelectedItem();
        HorseEquipment.AccessoryType selectedAccessory = (HorseEquipment.AccessoryType)accessoryCombo.getSelectedItem();
        double confidence = confidenceSlider.getValue() / 100.0;
        
        // Calculate stats
        double speed = (0.5 + (confidence * 0.5)) * selectedBreed.getSpeedFactor() * 
                     selectedSaddle.getSpeedFactor() * selectedHorseshoes.getSpeedFactor() + 
                     selectedAccessory.getSpeedBoost();
        
        double handling = 0.6 * selectedBreed.getAgilityFactor() * 
                         selectedSaddle.getStabilityFactor() * selectedHorseshoes.getGripFactor();
        
        double stamina = 0.7 * selectedBreed.getStaminaFactor() * selectedHorseshoes.getEnduranceFactor();
        
        // Add stat labels
        JLabel speedLabel = new JLabel(String.format("Speed: %.2f", speed));
        JLabel handlingLabel = new JLabel(String.format("Turn Handling: %.2f", handling));
        JLabel staminaLabel = new JLabel(String.format("Stamina: %.2f", stamina));
        
        // Add breed description
        JTextArea breedDesc = new JTextArea(selectedBreed.getDescription());
        breedDesc.setEditable(false);
        breedDesc.setLineWrap(true);
        breedDesc.setWrapStyleWord(true);
        breedDesc.setOpaque(false);
        breedDesc.setFont(new Font("Dialog", Font.ITALIC, 12));
        
        // Add equipment descriptions
        JLabel saddleLabel = new JLabel("Saddle: " + selectedSaddle.getDescription());
        JLabel horseshoesLabel = new JLabel("Horseshoes: " + selectedHorseshoes.getDescription());
        JLabel accessoryLabel = new JLabel("Accessory: " + selectedAccessory.getDescription());
        
        // Add all to panel
        statsPanel.add(Box.createVerticalStrut(5));
        statsPanel.add(speedLabel);
        statsPanel.add(handlingLabel);
        statsPanel.add(staminaLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(breedDesc);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(saddleLabel);
        statsPanel.add(horseshoesLabel);
        statsPanel.add(accessoryLabel);
        
        statsPanel.revalidate();
        statsPanel.repaint();
    }
    
    /**
     * Show the emoji selector dialog
     */
    private void showEmojiSelector() {
        JDialog emojiDialog = new JDialog(this, "Select Symbol", true);
        emojiDialog.setLayout(new BorderLayout());
        
        JPanel emojiPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            JButton button = new JButton(entry.getValue());
            button.setToolTipText(entry.getKey());
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    displaySymbolField.setText(entry.getValue());
                    emojiDialog.dispose();
                }
            });
            emojiPanel.add(button);
        }
        
        JPanel customPanel = new JPanel(new FlowLayout());
        JTextField customField = new JTextField(4);
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String custom = customField.getText();
                if (!custom.isEmpty()) {
                    displaySymbolField.setText(custom);
                    emojiDialog.dispose();
                }
            }
        });
        customPanel.add(new JLabel("Custom:"));
        customPanel.add(customField);
        customPanel.add(addButton);
        
        emojiDialog.add(emojiPanel, BorderLayout.CENTER);
        emojiDialog.add(customPanel, BorderLayout.SOUTH);
        
        emojiDialog.pack();
        emojiDialog.setLocationRelativeTo(this);
        emojiDialog.setVisible(true);
    }
    
    /**
     * Get the customized horse
     * 
     * @return The customized horse
     */
    public Horse getHorse() {
        return horse;
    }
    
    /**
     * Static method to create and show a dialog for horse customization
     * 
     * @param parent The parent frame
     * @return The customized horse, or null if canceled
     */
    public static Horse showDialog(JFrame parent) {
        HorseCustomizer customizer = new HorseCustomizer(parent);
        customizer.setVisible(true);
        return customizer.getHorse();
    }
    
    /**
     * Static method to edit an existing horse
     * 
     * @param parent The parent frame
     * @param horse The horse to edit
     * @return The edited horse, or null if canceled
     */
    public static Horse showDialog(JFrame parent, Horse horse) {
        HorseCustomizer customizer = new HorseCustomizer(parent, horse);
        customizer.setVisible(true);
        return customizer.getHorse();
    }
    
    /**
     * Simple test method
     */
    public static void main(String[] args) {
        // Set up look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JFrame frame = new JFrame("Horse Customizer Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setVisible(true);
        
        Horse horse = showDialog(frame);
        if (horse != null) {
            System.out.println(horse.getStats());
        }
        
        System.exit(0);
    }
}