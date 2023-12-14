package ratrapage;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class client extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8088;
    private PrintWriter writer;
    private JTextArea messageArea;
    

    public client() {
        // Créer la fenêtre du client
        super("client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        
        // Créer un panneau avec un BoxLayout orienté verticalement
        JPanel panneau = new JPanel();
        panneau.setLayout(new BoxLayout(panneau, BoxLayout.Y_AXIS));
        
        JButton boutonconnect = new JButton("connect");
        panneau.add(boutonconnect);
        // Créer une zone de texte pour afficher les messages
        messageArea = new JTextArea(10, 30);
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        panneau.add(scrollPane);

        // Créer un champ de saisie pour les messages
        JTextField champSaisie = new JTextField(20);
        panneau.add(champSaisie);

        // Créer un bouton "Envoyer"
        JButton boutonEnvoyer = new JButton("Envoyer");
        panneau.add(boutonEnvoyer);

        // Ajouter un écouteur d'événements au bouton "Envoyer"
        boutonEnvoyer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(champSaisie.getText());
                champSaisie.setText(""); // Effacer le champ de saisie après l'envoi
            }
        });
        
        boutonconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	connectToServer();    
            }
        });
        getContentPane().add(panneau);

        pack();
        setVisible(true);
    }

    private void connectToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    messageArea.append("Connecté au serveur.\n");

                   
                    client.this.writer = writer;

                    
                    while (true) {
                        String message = reader.readLine();
                        if (message == null) {
                            break;
                        }
                        if (message.startsWith("Question: ")) {
                            // Afficher la question dans la zone de texte
                            messageArea.append(message + "\n");
                            
                        } else if (message.startsWith("Votre score : ")) {
                            
                            messageArea.append(message + "\n");
                        } else {
                            messageArea.append(message + "\n");
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
        messageArea.setText("");
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new client();
            }
        });
    }
}
