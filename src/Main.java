import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.TimerTask;
import java.util.Timer;
import java.lang.NumberFormatException;

public class Main {

    private JFrame frame;
    private int batteryLevel = 0;
    private TrayIcon trayIcon = null;
    private JLabel batteryStatusLabel;
    private JLabel totalCapacityLabel;
    private JLabel originalCapacityLabel;
    private JLabel currentCapacityLabel;
    private JLabel manufacturerLabel;

    BufferedImage image = null;

    private static final long DELAY = 1000L;
    private static final long PERIOD = 100000L;

    public Main() {
        setLayout();
        try {
            image = ImageIO.read(Main.class.getResource("/"+batteryLevel+".png"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        trayIcon = new TrayIcon(image, "Demo");
        trayIcon.setImageAutoSize(true);
        //readBatteryInfo();
        setupTrayIcon();
        setTimer();

    }

    public static void main(String[] args) {
        new Main();
    }

    private void setLayout() {
        frame = new JFrame("Battery Level");
        frame.setSize(400, 400);
        frame.setLayout(new GridLayout(5, 2)); // 5 řádků a 2 sloupce

        // Inicializace proměnných
        batteryStatusLabel = new JLabel("unknown %");
        totalCapacityLabel = new JLabel("unknown mAh");
        originalCapacityLabel = new JLabel("unknown mAh");
        currentCapacityLabel = new JLabel("unknown mAh");
        manufacturerLabel = new JLabel("unknown");

        // Přidání komponent do JFrame
        frame.add(new JLabel("Stav baterie:"));
        frame.add(batteryStatusLabel);
        frame.add(new JLabel("Celková kapacita:"));
        frame.add(totalCapacityLabel);
        frame.add(new JLabel("Původní kapacita:"));
        frame.add(originalCapacityLabel);
        frame.add(new JLabel("Aktuální kapacita:"));
        frame.add(currentCapacityLabel);
        frame.add(new JLabel("Výrobce:"));
        frame.add(manufacturerLabel);

        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setVisible(true);
    }

    private void setupTrayIcon() {
        if (SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            SystemTray systemTray = SystemTray.getSystemTray();
            //BufferedImage image = null;
            try {
                image = ImageIO.read(Main.class.getResource("/"+batteryLevel+".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //trayIcon = new TrayIcon(image, "Demo");
            //trayIcon.setImageAutoSize(true);
            trayIcon.setImage(image);

            PopupMenu popupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(e -> frame.setVisible(true));

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));

            popupMenu.add(showItem);
            popupMenu.add(exitItem);
            trayIcon.setPopupMenu(popupMenu);

            try {
                systemTray.remove(trayIcon);
                systemTray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }


    public void readBatteryInfo() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe",
                    "(Get-WmiObject -Query 'Select * from Win32_Battery').EstimatedChargeRemaining");
            Process process = processBuilder.start();
            process.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    batteryLevel = Integer.valueOf(line.trim());
                    String status = "Stav baterie: " + line.trim() + " %";
                    System.out.println(status);
                    trayIcon.setToolTip(status);
                    setupTrayIcon();
                    //trayIcon.displayMessage("Aktualizace stavu baterie", status, TrayIcon.MessageType.INFO);
                    updateFrameValues();
                } else {
                    trayIcon.setToolTip("Není dostupná žádná baterie.");
                }
            }
        } catch (Exception e) {
            trayIcon.setToolTip("Chyba při získávání informací o baterii: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateFrameValues(){
        batteryStatusLabel.setText(batteryLevel + "%");
    }

    private void setTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task performed on: " + new Date() + "n" +
                        "Thread's name: " + Thread.currentThread().getName());
                readBatteryInfo();
            }
        };
        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(task, DELAY, PERIOD);
    }
}
