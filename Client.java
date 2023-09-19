import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try {
            System.out.println("Connecting to server...");
            // เชื่อมในเครื่องตัวเอง
            Socket clientSocket = new Socket("localhost", 55555);
            System.out.println("Connect successful");

            Scanner sc = new Scanner(System.in);
            // ส่งให้ s
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            // รับจาก s
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // เก็บ input
            String line;
            while (!clientSocket.isClosed()) { // ทำจนกว่าจะปิดตัวเอง พิมexit
                printChoice(); // ทุกครั้งก่อนรับข้อมูลใหม่
                line = sc.nextLine(); // ค้างบรรทัดนี้ถ้าไม่มี input
                switch (line) {
                    case "exit": // ปิดตัวเอง
                        System.out.println("Bye bye!");
                        clientSocket.close();
                        break;
                    case "1": // อ่านไฟล์ทม.ใน s
                        out.println(line); // ส่ง 1 ไปที่ s แล้วไปดูที่ server
                        // ต้องแปลงก่อน เพราะมันเป็น string
                        int size = Integer.parseInt(in.readLine());
                        System.out.println(size);
                        for (int i = 0; i < size; i++) {
                            System.out.println(in.readLine());// c ดูทุกไฟล์
                        }
                        break; // รออ่านใหม่
                    case "2":
                        out.println(line);// รับมาที่cinput
                        System.out.println("Please input file name :");
                        // ชื่อไฟล์ที่ผู้ใช้ส่งมา
                        String filename = sc.nextLine();
                        out.println(filename);

                        if (in.readLine().equalsIgnoreCase("yes")) {
                            // long ใหญ่กว่า int
                            long fileSize = Long.parseLong(in.readLine());
                            // print file size
                            System.out.println("Size = " + fileSize);

                            for (int i = 0; i < 10; i++) { // มี 10 thread
                                long start = (i * fileSize) / 10;
                                long end = ((i + 1) * fileSize) / 10;
                                out.println(start); // s รอรับ
                                out.println(end);
                                //
                                Thread x = new DownloadHandler(clientSocket, start, end, filename, i);
                                x.start();
                                Thread.sleep(100);
                            }
                        } else {
                            System.out.println("No file found");
                        }
                        break;
                    default:
                        System.out.println("Wrong input");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Connection error");
        }
    }

    public static void printChoice() {
        System.out.println("Type 1 to see all files");
        System.out.println("Type 2 to choose a file to download");
        System.out.println("Type exit to exit the program");
    }

    public static class DownloadHandler extends Thread {
        private long start;
        private long end;
        private Socket client;
        private String file;

        // รับclient ว่ายังใช้ของคนนั้นอยู่ไหม
        public DownloadHandler(Socket client, long start, long end, String file, int index)
                throws IOException {
            this.client = client;
            this.start = start;
            this.end = end;
            this.file = fileName;
        }

        public void run() {
            try {
                byte[] buffer = new byte[10240];
                // จบด้วยชื่อไฟล์ โหมดคือ อ่านไฟล์แล้วเตรียมเขียน
                RandomAccessFile raf = new RandomAccessFile(
                        "C:\\Users\\oneda\\Desktop\\OsProject\\ThreadOs\\" + file, "rw");
                int bytesRead;

                //
                InputStream in2 = client.getInputStream();
                // ชี้ที่จุดเริ่มต้น
                raf.seek(start);
                // รับจากos.write(buffer);
                while (start < end && (bytesRead = in2.read(buffer)) != -1) {
                    // เขียนไว้ในไฟล์ใหม่เรา
                    raf.write(buffer);
                    start += bytesRead;
                }

                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
