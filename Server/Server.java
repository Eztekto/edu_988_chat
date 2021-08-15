package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        {
            ArrayList<User> users = new ArrayList<>();
            try {
                ServerSocket serverSocket = new ServerSocket(8188);
                System.out.println("Сервер запущен");
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Клиент подключился");
                    User currentUser = new User(socket);
                    users.add(currentUser);
                    DataInputStream in = new DataInputStream(currentUser.getSocket().getInputStream());
                    DataOutputStream out = new DataOutputStream(currentUser.getSocket().getOutputStream());
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                out.writeUTF("Добро пожаловать на сервер");
                                String userName;
                                while (true) {
                                    out.writeUTF("Введите Ваше имя: ");
                                    userName = in.readUTF();
                                    for (User user : users) {
                                        if (user.getUserName() != null && user.getUserName().equals(userName)) {
                                            out.writeUTF("Имя '" + userName + "' занято, введите другое имя.");
                                            System.out.println("Пользователь пытается ввести уже используемое имя: " + userName);
                                            userName = null;
                                            break;//
                                        }
                                    }
                                    if (userName != null)
                                        break;//
                                }
                                currentUser.setUserName(userName);
                                System.out.println(currentUser.getUserName() + " теперь в чате.");
                                serverLogging(currentUser.getUserName() + " теперь в чате.");
                                out.writeUTF("Вы вошли в чат.");
                                while (true) {
                                    String request = in.readUTF();
                                    if (!request.startsWith("/m")) {
                                        serverLogging(currentUser.getUserName() + ": " + request);
                                        System.out.println(currentUser.getUserName() + ": " + request);
                                        continue;
                                    }
                                    String[] command = request.split(" ");
                                    if (command[0].equals("/m") && command.length >2) {
                                        request = "";
                                        for (int i = 2; i < command.length; i++){
                                            request += command[i] + " ";
                                        }
                                        System.out.println(currentUser.getUserName() + " отправил личное сообщение для " + command[1] + ": " + request);
                                        privateMessage(currentUser.getUserName()+ " шепчет: " + request, command[1]);
                                        continue;
                                    }
                                    else {
                                        out.writeUTF("Сообщение должно иметь формат /m [имя] сообщение");
                                        continue;
                                    }


                                }
                            } catch (IOException e) {
                                users.remove(currentUser);
                                System.out.println(currentUser.getUserName() + " покинул(а) чат.");
                                try {
                                    serverLogging(currentUser.getUserName() + " покинул(а) чат.");
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        private void serverLogging(String request) throws IOException {
                            for (User user : users) {
                                if (users.indexOf(currentUser) == users.indexOf(user) || user.getUserName() == null)
                                    continue;
                                DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                out.writeUTF(request);
                            }
                        }
                        private void privateMessage(String request, String userName) throws IOException {
                            for (User user : users) {
                                if (user.getUserName() == null) continue;
                                if (user.getUserName().equals(userName)) {
                                    DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                    out.writeUTF(request);
                                    return;
                                }
                            }
                            out.writeUTF("Пользователь " + userName + " не найден, сообщение не отправлено.");
                        }
                    }
                    );
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
