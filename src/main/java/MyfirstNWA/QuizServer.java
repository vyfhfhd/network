package MyfirstNWA;

import java.io.*;
import java.net.*;
import java.util.*;

public class QuizServer {

    // Port number for the server to listen on
    private static final int PORT = 1234;

    // List of quiz questions
    private static final List<Question> questions = Arrays.asList(
            new Question("What is the capital of France?", "Paris"),
            new Question("What is 5 + 7?", "12"),
            new Question("What is the capital of Japan?", "Tokyo"),
            new Question("What is the capital of South Korea?", "Seoul"),
            new Question("What is the capital of America?", "Washington D.C.")
    );

    // Flag to control server shutdown
    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("Quiz Server is starting...");

        // Thread to monitor shutdown command from server console
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.println("Type 'shutdown' to stop the server:");
                    String command = scanner.nextLine();
                    if ("shutdown".equalsIgnoreCase(command)) {
                        running = false; // Set the shutdown flag
                        System.out.println("Server shutting down...");
                        break;
                    }
                }
            }
        }).start();

        // Main server logic
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            // Continuously listen for client connections while running
            while (running) {
                try {
                    serverSocket.setSoTimeout(1000); // 1-second timeout for accepting connections
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    new ClientHandler(clientSocket).start(); // Start a new thread to handle the client
                } catch (SocketTimeoutException e) {
                    // Timeout occurs, check if the server is still running
                    if (!running) break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }

        System.out.println("Server has stopped."); // Server shutdown message
    }

    // Thread to handle individual client connections
    static class ClientHandler extends Thread {
        private final Socket socket;
        private int score = 0;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    // Output stream to send messages to the client
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    // Input stream to read messages from the client
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {             
                // Iterate through the list of questions
                for (Question question : questions) {
                    out.println("Question: " + question.getQuestion()); // Send question to the client
                    String answer = in.readLine(); // Read the client's answer

                    // If client disconnects or sends 'bye', terminate the quiz
                    if (answer == null || answer.equalsIgnoreCase("bye")) {
                        out.println("Quiz ended. Your final score is: " + score);
                        break; // End the connection
                    }

                    // Check if the answer is correct
                    if (question.getAnswer().equalsIgnoreCase(answer)) {
                        score++;
                        out.println("Correct!"); // Notify the client of the correct answer
                    } else {
                        out.println("Incorrect! The correct answer is: " + question.getAnswer());
                    }
                }

                // Notify the client that the quiz is over and send the final score
                out.println("Quiz over! Your final score is: " + score);

            } catch (IOException e) {
                System.err.println("Error with client connection: " + e.getMessage());
            } finally {
                // Ensure the socket is closed after the client disconnects
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Failed to close client socket: " + e.getMessage());
                }
            }
        }
    }

    // Class to represent a quiz question
    static class Question {
        private final String question; // The quiz question
        private final String answer;   // The correct answer to the question

        public Question(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question; // Return the question text
        }

        public String getAnswer() {
            return answer; // Return the correct answer
        }
    }
}
