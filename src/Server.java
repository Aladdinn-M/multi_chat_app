import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;
    private Map<String, PrintWriter> connectedClients;

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            connectedClients = new HashMap<>();
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);




            out.println(" |start broadcast conversation \n type \"/private\" for private message \n type \"/users\" show connected users   \n type \"/exit\" to LogOut");
            String username = in.readLine();
            connectedClients.put(username, out);

            System.out.println(username + " connected");

            broadcastMessage("SERVER", username + " has joined the chat.");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("/exit")) {
                    break;
                } else if (inputLine.startsWith("/private")) {
                    handlePrivateMessage(username, inputLine);
                } else if (inputLine.equals("/users")) {
                    showConnectedUsers(username);
                }
                else{
                    broadcastMessage(username, inputLine);
                }
            }

            System.out.println(username + " disconnected");

            broadcastMessage("SERVER", username + " has left the chat.");

            connectedClients.remove(username);
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePrivateMessage(String sender, String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            String recipient = parts[1];
            String content = parts[2];

            PrintWriter recipientOut = connectedClients.get(recipient);
            if (recipientOut != null) {
                connectedClients.get(sender).println(sender + " in private :" + content);
                recipientOut.println("Private message from " + sender + ": " + content);
            } else {
                connectedClients.get(sender).println("Recipient '" + recipient + "' not found.");
            }
        }
    }

    private void broadcastMessage(String sender, String message) {
        for (PrintWriter out : connectedClients.values()) {
            out.println(sender + ": " + message);
        }
    }


    private void showConnectedUsers(String requester) {
        StringBuilder userList = new StringBuilder("Connected users:\n");
        for (String username : connectedClients.keySet()) {
            userList.append("- ").append(username);
            if (username.equals(requester)) {
                userList.append(" (You)");
            }
            userList.append("\n");
        }
        connectedClients.get(requester).println(userList.toString());
    }

    public static void main(String[] args) {
        int port = 12345;
        Server server = new Server(port);
        server.start();
    }
}
