import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



public class Main {

    JFrame frame;
    int batteryLevel = 0;
    TrayIcon trayIcon = null;//new TrayIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("ico.png")));

    public Main() {
        frame = new JFrame("Battery Level");
        frame.setSize(400, 400);
        trayIcon();
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
}