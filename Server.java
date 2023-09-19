import java.net.*;
import java.io.*;

public class Server {
    // เชื่อมต่อ c s
    public static void main(String[] args) { // มองที่เมนก่อน
        final int PORT = 55555;
        System.out.println("Starting server..");
        try (ServerSocket serverSocket = new ServerSocket(PORT);) {
            // ถ้าไม่มีจะแตก
            System.out.println("Listening to port " + PORT);

            while (!serverSocket.isClosed()) { // ถ้ายังไม่ปิด
                Socket clientSocket = serverSocket.accept(); // = คือรับเข้ามา
                System.out.println("New Connection >" + clientSocket); // มีคอนใหม่เข้า
                // มัลติเทรด ต้องดูแลคนนี้
                new ClientHandler(clientSocket).start(); // .start คือของเทรด แตกเทรดใหม่->วิ่งไปอีกทาง เข้าตัวสร้าง
                                                         // run()
                // วิ่งไปหา 555 แล้วแตกเทรดเพิ่ม
            }
        } catch (Exception e) {
            System.out.println("Server failed");
        }
    }

    static class ClientHandler extends Thread { // เรียกใช้ได้เลย ไม่ต้องนิว
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String serverFilePath;

        public ClientHandler(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket; // เก็บการเชื่อมต่อ
            // ส่ง string
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            // รับจาก c
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // ที่เก็บไฟล์
            this.serverFilePath = "C:\\Users\\oneda\\Desktop\\OsProject\\ServerFiles\\";
        }

        public void sendFileList() {
            // ดูไฟล์ทม.
            File files = new File(serverFilePath);
            if (files.list().length == 0) { // ไม่มีไฟล์
                out.println(1);
                out.println("No file available");
            } else { // ส่งจน.ไฟล์ทม.ออก
                out.println(files.list().length);// out ส่งไปให้ in c
                for (String fileName : files.list()) { // ส่งชื่อไฟล์ทีละอัน
                    out.println(fileName);
                }
            }
        }

        public boolean fileChecking(String fileName) {
            // สร้างไฟล์ โดยเข้าถึงไฟล์นั้นเลย
            File file = new File(serverFilePath + fileName); // เติมด้วยไฟล์ลูก
            if (file.exists()) { // มีไฟล์ไหม
                System.out.println(file.length()); // return long
                out.println(file.length()); // ส่งขนาดไฟล์ไปที่ c
                return (file.exists());
            }
            return (file.exists()); // ถ้าไม่เจอจะ return false แล้วจะ break ทิ้ง
        }

        @Override
        public void run() {
            String clientInput;
            try {
                while (((clientInput = in.readLine()) != null)) { // รับจาก in.readLine() ใช้เพราะรับทั้งบรรทัด เก็บใน
                                                                  // cinput
                    switch (clientInput) {
                        case "1":
                            sendFileList();
                            break;
                        case "2":
                            String fileName = in.readLine();// รับชื่อไฟล์จาก c
                            if (fileChecking(fileName)) {
                                System.out.println("TEST");
                                // รับจาก c
                                // รับส่งตามลำดับ
                                for (int i = 0; i < 10; i++) {
                                    long start = Long.parseLong(in.readLine());
                                    long end = Long.parseLong(in.readLine());
                                    //
                                    Thread x = new DownloadManager(clientSocket, serverFilePath + fileName,
                                            start, end);
                                    x.start();
                                    x.sleep(10);
                                }
                            } else {
                                out.println("no");
                                System.out.println("NO file found");
                            }
                            break;
                        default:
                            System.out.println("Wrong input from user");
                            break;
                    }
                }
            } catch (

            Exception e) {
                System.out.println("CLIENT DONE FOR");

            }
        }
    }

    static class DownloadManager extends Thread {
        private long start;
        private long end;
        private RandomAccessFile raf;
        private OutputStream os;

        public DownloadManager(Socket client, String file, long start, long end) throws Exception {
            // ของ outstrem จาก c
            this.os = client.getOutputStream();
            this.start = start;
            this.end = end;
            // เข้าถึงไฟล์ (หาไฟล์)
            raf = new RandomAccessFile(new File(file), "r");
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[10240];
                int bytesRead;
                // อ่านที่ start เลย ไม่ต้องอ่านตั้งแต่ไฟล์แรก แต่ละเทรดเริ่มคนละที่
                raf.seek(start);
                // ดึงค่าทม.มาในbyte มันจะขยับทีละ 10240
                while (start < end && (bytesRead = raf.read(buffer)) != -1) {
                    // ส่งไปให้ in c 10240
                    os.write(buffer);
                    start += bytesRead;// เพิ่มจนกว่าจะหมด
                    // ถ้าเกินที่ที่จะอ่านมันจะ return -1
                    // อ่านทีละบล็อกแล้วส่ง
                }
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}