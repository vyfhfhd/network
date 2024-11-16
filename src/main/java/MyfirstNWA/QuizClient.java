package MyfirstNWA;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class QuizClient {

    public static void main(String[] args) {
        final String SERVER_ADDRESS = "localhost"; // Address of the server
        final int PORT = 1234; // Port number to connect to the server

        try (
                // Create a socket to connect to the server
                Socket socket = new Socket(SERVER_ADDRESS, PORT);

                // Reader for incoming messages from the server
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Writer to send messages to the server
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Scanner to read user input from the console
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to the quiz server!"); // Notify the client that the connection is successful

            // Loop to handle communication with the server
            while (true) {
                // Read a message from the server
                String serverMessage = in.readLine();

                // If the server sends a null message, the connection is closed
                if (serverMessage == null) {
                    System.out.println("Connection closed by the server.");
                    break; // Exit the loop
                }

                // If the server sends a quiz question
                if (serverMessage.startsWith("Question:")) {
                    System.out.println(serverMessage); // Display the question
                    System.out.print("Your answer: ");
                    String answer = scanner.nextLine(); // Read the user's answer
                    out.println(answer); // Send the user's answer to the server
                } 
                // If the server sends a quiz completion message
                else if (serverMessage.equalsIgnoreCase("Quiz over!") || serverMessage.startsWith("Quiz ended.")) {
                    System.out.println(serverMessage); // Display the completion message
                    break; // Exit the loop
                } 
                // Handle any other messages from the server
                else {
                    System.out.println(serverMessage); // Display the message
                }
            }
        } 
        // Handle exceptions related to I/O or socket issues
        catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
        }
    }
}
