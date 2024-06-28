import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private JFrame frame;
    private int batteryLevel = 0;
    //private String batteryTime;
    private String batteryStatus;
    private String batteryChemistry;
    private String batteryRunTime;
    private TrayIcon trayIcon = null;
    private JLabel batteryLevelLabel;
    private JLabel totalStatusLabel;
    private JLabel originalRunTimeLabel;
    private JLabel currentChemistryLabel;
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
        batteryLevelLabel = new JLabel("unknown %");
        totalStatusLabel = new JLabel("unknown mAh");
        originalRunTimeLabel = new JLabel("unknown mAh");
        currentChemistryLabel = new JLabel("unknown mAh");
        manufacturerLabel = new JLabel("unknown");

        // Přidání komponent do JFrame
        frame.add(new JLabel("Úroveň baterie:"));
        frame.add(batteryLevelLabel);
        frame.add(new JLabel("Status baterie:"));
        frame.add(totalStatusLabel);
        frame.add(new JLabel("Čas na baterii:"));
        frame.add(originalRunTimeLabel);
        frame.add(new JLabel("Složení baterie:"));
        frame.add(currentChemistryLabel);
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
        ExecutorService executor = Executors.newFixedThreadPool(4);

        CompletableFuture<Integer> batteryLevelFuture = CompletableFuture.supplyAsync(() -> {
            return executePowerShellCommandAsInteger("(Get-WmiObject -Query 'Select * from Win32_Battery').EstimatedChargeRemaining");
        }, executor);

        CompletableFuture<String> batteryRunTimeFuture = CompletableFuture.supplyAsync(() -> {
            return executePowerShellCommandAsString("(Get-WmiObject -Query 'Select * from Win32_Battery').EstimatedRunTime");
        }, executor);

        CompletableFuture<String> batteryStatusFuture = CompletableFuture.supplyAsync(() -> {
            return executePowerShellCommandAsString("(Get-WmiObject -Query 'Select * from Win32_Battery').BatteryStatus");
        }, executor);

        CompletableFuture<String> batteryChemistryFuture = CompletableFuture.supplyAsync(() -> {
            return executePowerShellCommandAsString("(Get-WmiObject -Query 'Select * from Win32_Battery').Chemistry");
        }, executor);

        try {
            this.batteryLevel = batteryLevelFuture.get();
            this.batteryRunTime = batteryRunTimeFuture.get();
            this.batteryStatus = batteryStatusFuture.get();
            this.batteryChemistry = batteryChemistryFuture.get();

            //updateTrayIconAndFrame();
            updateFrameValues();
            setupTrayIcon();
        } catch (Exception e) {
            trayIcon.setToolTip("Chyba při získávání informací o baterii: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }



        /**

        try {
            ProcessBuilder batteryLevel = new ProcessBuilder("powershell.exe",
                    "(Get-WmiObject -Query 'Select * from Win32_Battery').EstimatedChargeRemaining");
            ProcessBuilder batteryRunTime = new ProcessBuilder("powershell.exe",
                    "(Get-WmiObject -Query 'Select * from Win32_Battery').EstimatedRunTime");
            ProcessBuilder batteryStatus = new ProcessBuilder("powershell.exe",
                    "(Get-WmiObject -Query 'Select * from Win32_Battery').BatteryStatus");
            ProcessBuilder batteryChemistry = new ProcessBuilder("powershell.exe",
                    "(Get-WmiObject -Query 'Select * from Win32_Battery').Chemistry");
            Process process = batteryLevel.start();
            process.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    this.batteryLevel = Integer.valueOf(line.trim());
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
        }**/
    }


    private Integer executePowerShellCommandAsInteger(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", command);
            Process process = processBuilder.start();
            process.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return Integer.valueOf(line.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String executePowerShellCommandAsString(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", command);
            Process process = processBuilder.start();
            process.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return line.trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateFrameValues(){
        batteryLevelLabel.setText(batteryLevel + "%");
        batte

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
