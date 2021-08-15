package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8188);
            System.out.println("Успешно подключен");
            DataInputStream in = new DataInputStream(socket.getInputStream()); // Поток ввода
            DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // Поток вывода
            String response = in.readUTF(); // Читаем ответ от сервера
            System.out.println(response);
            response = in.readUTF();
            System.out.println(response);
            Scanner scanner = new Scanner(System.in);
            out.writeUTF(scanner.nextLine()); // Ожидаем ввод имени
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String response;
                    while (true){ // Бесконечно ожидаем сообщение от сервера
                        try {
                            response = in.readUTF(); // Читаем ответ от сервера
                            System.out.println(response); // Печатаем ответ от сервера на экран
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
            while (true){
                String consoleText = scanner.nextLine(); // Ждём сообщение от пользователя
                out.writeUTF(consoleText); // Отправляем сообщение на сервер

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
