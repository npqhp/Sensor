import java.net.Socket;
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
        public boolean running = true;

        public SensorLocation(int location, int type) {
            this.location = location;
            this.type = type;
        }

        public void run(){
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
