 package ratrapage;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.AbstractMap;


 public class serveur extends JFrame {
     private ServerSocket serverSocket;
     private List<Socket> clientSockets = new ArrayList<>();
     private List<String> questions = new ArrayList<>();
     private List<String> answers = new ArrayList<>();
     private int currentQuestionIndex = 0;
     private Map<Socket, Integer> responseOrder = new HashMap<>();


     public serveur() {
         initComponents();
         initServer();
         initQuestionsAndAnswers();
     }

     private void initComponents() {
         setTitle("Server");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setSize(300, 200);
         setLocationRelativeTo(null);

         JButton startGameButton = new JButton("Start Game");
         startGameButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 startGame();
             }
         });

         JPanel panel = new JPanel();
         panel.setLayout(new FlowLayout());
         panel.add(startGameButton);

         add(panel);
     }

     private void initServer() {
         try {
             serverSocket = new ServerSocket(8088);
             acceptClients();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

     private void acceptClients() {
         new Thread(() -> {
             try {
                 while (true) {
                     Socket clientSocket = serverSocket.accept();
                     clientSockets.add(clientSocket);
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }).start();
     }

     private void initQuestionsAndAnswers() {
         // Ajouter les questions et les réponses ici
         questions.add("Quelle est la capitale de la France?");
         questions.add("Quelle est la capitale de l'Espagne?");
         questions.add("Quelle est la capitale du Portugal?");
         questions.add("Quelle est la capitale de l'Italie?");
         questions.add("Quelle est la capitale du Mexique?");
         questions.add("Quelle est la capitale de la Russie?");
         questions.add("Quelle est la capitale de l'Allemagne?");
         questions.add("Quelle est la capitale de l'Angleterre?");
         questions.add("Quelle est la capitale de la Suisse?");
         questions.add("Quelle est la capitale de la Belgique?");

         answers.add("Paris");
         answers.add("Madrid");
         answers.add("Lisbonne");
         answers.add("Rome");
         answers.add("Mexico");
         answers.add("Moscou");
         answers.add("Berlin");
         answers.add("Londres");
         answers.add("Berne");
         answers.add("Bruxelles");
     }

     private void startGame() {
    	    Map<String, Integer> scores = new HashMap<>();

    	    for (int i = 0; i < questions.size(); i++) {
    	        sendQuestionToClients(questions.get(i));
    	        waitForAnswers();

    	        // Envoyer les scores aux clients après chaque question
    	        sendScoresToClients(scores);
    	    }
    	}


     private void sendQuestionToClients(String question) {
         sendToAllClients("Question: " + question);
     }

     private void waitForAnswers() {
    	    List<String> responses = new ArrayList<>();
    	    Map<String, Integer> scores = new HashMap<>();

    	    // Initialiser les scores à zéro pour tous les clients
    	    for (Socket clientSocket : clientSockets) {
    	        scores.put(clientSocket.toString(), 0);
    	    }

    	    // Boucle pour chaque question
    	    for (int i = 0; i < questions.size(); i++) {
    	        // Envoyer la question uniquement si ce n'est pas la première itération
    	        if (i > 0) {
    	            sendQuestionToClients(questions.get(i));
    	        }

    	        // Réinitialiser l'ordre de réponse avant chaque question
    	        responseOrder.clear();

    	        // Attendre les réponses pour cette question
    	        while (responses.size() < clientSockets.size()) {
    	            for (Socket clientSocket : clientSockets) {
    	                try {
    	                    Scanner scanner = new Scanner(clientSocket.getInputStream());
    	                    if (scanner.hasNextLine()) {
    	                        String response = scanner.nextLine();
    	                        responses.add(response);

    	                        // Enregistrer l'ordre de réponse pour ce client seulement s'il n'a pas encore répondu
    	                        if (!responseOrder.containsKey(clientSocket)) {
    	                            responseOrder.put(clientSocket, responseOrder.size() + 1);
    	                        }

    	                        // Vérifier la réponse et attribuer des points si correct
    	                        if (response.equalsIgnoreCase(answers.get(i))) {
    	                            int currentScore = scores.get(clientSocket.toString());
    	                            // Utiliser l'ordre inversé pour attribuer le score
    	                            scores.put(clientSocket.toString(), currentScore + (clientSockets.size() - responseOrder.get(clientSocket) + 1));
    	                        }
    	                    }
    	                } catch (IOException e) {
    	                    e.printStackTrace();
    	                }
    	            }
    	        }

    	        // Envoyer les scores aux clients après chaque question
    	        sendScoresToClients(scores);

    	        // Effacer les réponses pour la prochaine question
    	        responses.clear();
    	    }
    	}






     private void sendScoresToClients(Map<String, Integer> scores) {
    	    // Créer une liste triée des clients en fonction de leur score
    	    List<Map.Entry<String, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
    	    sortedScores.sort((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()));

    	    // Envoyer les scores et la position à chaque client
    	    for (Socket clientSocket : clientSockets) {
    	        try {
    	            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
    	            int score = scores.get(clientSocket.toString());
    	            
    	            // Récupérer la position du client dans le tableau des scores
    	            int position = sortedScores.indexOf(new AbstractMap.SimpleEntry<>(clientSocket.toString(), score)) + 1;

    	            writer.println("Votre score : " + score + " | Position dans le tableau des scores : " + position);
    	        } catch (IOException e) {
    	            e.printStackTrace();
    	        }
    	    }
    	}


     private void sendToAllClients(String message) {
         for (Socket clientSocket : clientSockets) {
             try {
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                 writer.println(message);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }

     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
             new serveur().setVisible(true);
         });
     }
 }
