import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Sensor {
    public static final int SERVER_PORT = 8086;
    public static final String SERVER_IP = "127.0.0.1";

    public static class SensorLocation extends Thread{
        private final int location,type;
        private Socket socket;
        private final static int BUFFER_SIZE = 1024;
        private final static byte[] BUFFER = new byte[BUFFER_SIZE];
        private final JSONObject jsonObject = new JSONObject();
        private double value = -100000;
        public boolean running = true;
        private boolean disconnect = false;

        public SensorLocation(int location, int type) {
            this.location = location;
            this.type = type;
        }

        public void run(){
            try (Socket sk = new Socket(SERVER_IP, SERVER_PORT)) {
                this.socket = sk;
                BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                DataInputStream recvFile = new DataInputStream(socket.getInputStream());
                sendMessage("SENSOR HELO Broker",os);
                readMessage(recvFile);
                jsonObject.put("TypeID",String.valueOf(type));
                jsonObject.put("LocationID",String.valueOf(location));

                while (running && !disconnect) {
                    sendMessage("INFO",os);
                    if (disconnect || !running) break;
                    readMessage(recvFile);
                    if (disconnect || !running) break;

                    switch (jsonObject.get("TypeID").toString()) {
                        case "1" -> jsonObject.put("Value", randNum(0, 40));
                        case "2" -> jsonObject.put("Value", randNum(40, 80));
                        case "3" -> jsonObject.put("Value", randNum(0, 60));
                        case "4" -> jsonObject.put("Value", randNum(20, 100));
                        case "5" -> jsonObject.put("Value", randNum(0, 50));
                        case "6" -> jsonObject.put("Value", randNum(0.5, 0.7));
                        case "7" -> jsonObject.put("Value", randNum(0, 100));
                        case "8" -> jsonObject.put("Value", randNum(0, 55));
                    }

                    if (disconnect || !running) break;
                    sendMessage(jsonObject.toString(),os);
                    if (disconnect || !running) break;
                    readMessage(recvFile);
                    if (disconnect || !running) break;
                    TimeUnit.SECONDS.sleep(5);
                }
                sendMessage("QUIT",os);
                readMessage(recvFile);
            } catch (IOException ie) {
                System.out.println("Không thể kết nối đến Server");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private String randNum(double min, double max){
            if (value == -100000) {
                Random random = new Random();
                value = min + random.nextDouble()*(max-min);
            }
            Random random = new Random();
            double t = (max-min)/80;
            boolean c = random.nextBoolean();
            double change = random.nextDouble();
            if (c) value += change*t;
            else value -= change*t;
            if (value > max) value = max;
            if (value < min) value = min;
            value = (double) ((int) (value*10)) / 10;
            return String.format("%f",value);
        }

        private void readMessage(DataInputStream recvFile){
            try {
                socket.setSoTimeout(3000);
                int n = recvFile.read(BUFFER,0,BUFFER_SIZE);
                if (n < 1) disconnect = true;
            } catch (IOException ignored) {
            }
        }

        private void sendMessage(String message, BufferedWriter os){
            char[] chars = message.toCharArray();
            try {
                os.write(chars,0,chars.length);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void pause(){
            running = false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SensorLocation[][] sensorLocations = new SensorLocation[64][9];
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("Nhập lựa chọn của bạn(Start = 0 | Stop = 1 | Exit = -1): ");
            int select = scanner.nextInt();

            if (select == -1) break;

            if (select == 0) {
                System.out.print("Nhập địa điểm bạn muốn bắt đầu(All = 0 | Exit = -1 | LocationId): ");
                int locationId = scanner.nextInt();
                if (locationId == -1) break;

                if (locationId == 0){
                    for (int i=1; i<=63; i++)
                        for (int j=1; j<=8; j++)
                            if (sensorLocations[i][j] == null || !sensorLocations[i][j].running) {
                                sensorLocations[i][j] = new SensorLocation(i,j);
                                TimeUnit.MILLISECONDS.sleep(500);
                                sensorLocations[i][j].start();
                            }
                    System.out.println();
                    continue;
                }

                System.out.print("Nhập cảm biến bạn muốn bắt đầu(All = 0 | Exit = -1 | SensorId): ");
                int sensorId = scanner.nextInt();

                if (sensorId == 0) {
                    for (int i=1; i<=8; i++)
                        if (sensorLocations[locationId][i] == null || !sensorLocations[locationId][i].running) {
                            sensorLocations[locationId][i] = new SensorLocation(locationId,i);
                            TimeUnit.MILLISECONDS.sleep(500);
                            sensorLocations[locationId][i].start();
                        }
                    System.out.println();
                    continue;
                }

                if (sensorLocations[locationId][sensorId] == null || !sensorLocations[locationId][sensorId].running) {
                    sensorLocations[locationId][sensorId] = new SensorLocation(locationId,sensorId);
                    TimeUnit.MILLISECONDS.sleep(500);
                    sensorLocations[locationId][sensorId].start();
                }
            }

            if (select == 1){
                System.out.print("Nhập địa điểm bạn muốn kết thúc(All = 0 | Exit = -1 | LocationId): ");
                int locationId = scanner.nextInt();
                if (locationId == -1) break;

                if (locationId == 0){
                    for (int i=1; i<=63; i++)
                        for (int j=1; j<=8; j++)
                            if (sensorLocations[i][j] != null && sensorLocations[i][j].running) {
                                sensorLocations[i][j].pause();
                            }
                    System.out.println();
                    continue;
                }

                System.out.print("Nhập cảm biến bạn muốn kết thúc(All = 0 | Exit = -1 | SensorId): ");
                int sensorId = scanner.nextInt();

                if (sensorId == 0) {
                    for (int i=1; i<=8; i++)
                        if (sensorLocations[locationId][i] != null && sensorLocations[locationId][i].running) {
                            sensorLocations[locationId][i].pause();
                        }
                    System.out.println();
                    continue;
                }

                if (sensorLocations[locationId][sensorId] != null && sensorLocations[locationId][sensorId].running) {
                    sensorLocations[locationId][sensorId].pause();
                }
            }
            System.out.println();
        }
        for (int i=1; i<=63; i++)
            for (int j=1; j<=8; j++)
                if (sensorLocations[i][j] != null && sensorLocations[i][j].running) {
                    sensorLocations[i][j].pause();
                }
    }

}
