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


public class Main {

    JFrame frame;
    int batteryLevel = 0;
    TrayIcon trayIcon = null;//new TrayIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("ico.png")));
    //JLabel batteryStatusLabel = new JLabel("status...");

    private JLabel batteryStatusLabel;
    private JLabel totalCapacityLabel;
    private JLabel originalCapacityLabel;
    private JLabel currentCapacityLabel;
    private JLabel manufacturerLabel;

    public Main() {
        setLayout();
        trayIcon();
        setTimer();
    }

    private void setLayout(){
        frame = new JFrame("Battery Level");
        frame.setSize(400, 400);
        //JLabel lBatteryLabel = new JLabel("Battery level:");
        // Nastavení layoutu
        frame.setLayout(new GridLayout(5, 2)); // Použijeme GridLayout s 5 řádky a 2 sloupci

        // Inicializace proměnných
        batteryStatusLabel = new JLabel("unknown %");
        totalCapacityLabel = new JLabel("unknown mAh");
        originalCapacityLabel = new JLabel("unknown mAh");
        currentCapacityLabel = new JLabel("unknown mAh");
        manufacturerLabel = new JLabel("unknown");

        // Přidání proměnných do JFrame
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

    }

    private void trayIcon() {
        if(SystemTray.isSupported() == true){
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }
        SystemTray systemTray = SystemTray.getSystemTray();
        BufferedImage image = null;
        try {
            image = ImageIO.read(Main.class.getResource("/ico.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
        trayIcon = new TrayIcon(image, "Demo");
        trayIcon.setImageAutoSize(true);
        PopupMenu popupMenu = new PopupMenu();
        MenuItem show = new MenuItem("Show");
        show.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(true);
            }
        });
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        popupMenu.add(show);
        popupMenu.add(exit);
        trayIcon.setPopupMenu(popupMenu);
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main();
    }

    public void  readBatteryInfo(){
        // Získání stavu baterie pomocí PowerShell skriptu
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe",
                    "(Get-WmiObject -Query 'Select * from Win32_Battery').EstimatedChargeRemaining");
            Process process = processBuilder.start();
            process.waitFor();

            // Čtení výstupu PowerShell skriptu
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                if ((line = reader.readLine()) != null) {
                    String status = "Stav baterie: " + line.trim() + " %";
                    System.out.println(status);
                    trayIcon.setToolTip(status);
                    trayIcon.displayMessage("Aktualizace stavu baterie", status, TrayIcon.MessageType.INFO);
                } else {
                    trayIcon.setToolTip("Není dostupná žádná baterie.");
                }
            }
        } catch (Exception e) {
            trayIcon.setToolTip("Chyba při získávání informací o baterii: " + e.getMessage());
            e.printStackTrace();
        }
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

        long delay = 1000L;
        long period = 1000L;//300000L;
        timer.scheduleAtFixedRate(task, delay, period);
    }
}