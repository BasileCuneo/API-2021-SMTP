package ch.heigvd.api.clientSMTP;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

/**
 * Calculator client implementation
 */
public class Client {

    private static final Logger LOG = Logger.getLogger(Client.class.getName());
    private static String crlf = "\r\n";
    /**
     * Main function to run client
     *
     * @param args no args required
     */
    public static void main(String[] args)  {

        ConfigManager config = new ConfigManager("/Users/joris/Documents/école/HEIG-VD/Cours/semester-3/API/labo/API-2021-SMTP/target/classes/ch/heigvd/api/clientSMTP", 4);
        int i =0;
        for(List<String> l : config.getGroupsEmail()) {
            System.out.println("Group : " + ++i + "############################");
            for(String addr : l){
                System.out.println(addr);
            }
        }


        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%6$s%n");

        Socket clientSocket;
        BufferedWriter clientOut;
        BufferedReader clientIn;
        try {
            clientSocket = new Socket("127.0.0.1", 25);
            clientOut = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            System.out.println(clientIn.readLine());
            List<String> to = new ArrayList<>();
            ConfigManager.MailStruct  msg =  config.getRandomMail();
            for(List<String> emails : config.getGroupsEmail()) {
                String fakeFrom = emails.remove(0);
                System.out.println("Connected to server");
                if(sendMail("from.me@heig-vd.ch",emails,fakeFrom, msg.subject, msg.body
                        ,clientIn,clientOut))
                    System.out.println("Mail sent");
                else
                    System.out.println("Mail not sent");
            }



        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while creating socket", e);

        }


    }

    static private boolean sendMail(String from, List<String> to,String fakeFrom,
                                    String subject , String message,
                                    BufferedReader clientIn,  BufferedWriter clientOut) {
        try{
            clientOut.write(ehlo("heig-vd.ch"));
            clientOut.flush();
            if(!checkEHLO(clientIn)) {
                System.out.println("EHLO not accepted");
                return false;
            }
            System.out.println("EHLO accepted");
            clientOut.write(genMailFrom(from));
            clientOut.flush();
            if(checkError(clientIn)) {
                System.out.println("MAIL FROM not accepted");
                return false;
            }
            System.out.println("MAIL FROM accepted");
            for(String addr : to) {
                clientOut.write(genRcptTo(addr));
                clientOut.flush();
                if(checkError(clientIn)) {
                    System.out.println("RCPT TO not accepted");
                    return false;
                }
                System.out.println("RCPT TO accepted");
            }
            clientOut.write("DATA"+crlf);
            clientOut.flush();
            if(!checkResponse(clientIn, "354")){
                System.out.println("DATA not accepted");
                return false;
            }
            System.out.println("DATA accepted");

            clientOut.write(genMessage(fakeFrom,to, subject, message));
            clientOut.flush();
            if(checkError(clientIn)){
                System.out.println("Message not accepted");
                return false;
            }
            System.out.println("Message accepted");

        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while sending mail");
            return false;
        }
        return true;
    }

    static private boolean checkEHLO(BufferedReader client) {

        String response;
        do {
            try {
                response = client.readLine();
                System.out.println(response);
                if (!response.startsWith("250")) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } while (!response.startsWith("250 "));
        return true;
    }

    static private boolean checkError(BufferedReader client) {
        try{
            String response = client.readLine();
            return !response.startsWith("250");
        }catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }
    static private boolean checkResponse(BufferedReader client,String expectedCode) {
        try{
            String response = client.readLine();
            return response.startsWith(expectedCode);
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static private String ehlo(String domaine) {
        return "EHLO " + domaine + "\r\n";
    }

    static private String genMailFrom(String senderAddr) {
        //return "MAIL FROM:<" + senderAddr + ">"+crlf;
        return "MAIL FROM: <" + senderAddr + ">"+crlf;

    }

    static private String genRcptTo(String receiverAddr) {
        return "RCPT TO: <" + receiverAddr + ">"+crlf;
    }



    static private String genMessage(String fakeFrom, List<String> to, String subject, String body){
        //StringBuilder result = new StringBuilder("From: <" + fakeFrom + ">"+crlf+"To:
        // ");
        StringBuilder result = new StringBuilder("From: " + fakeFrom +crlf+"To: ");

        for(String addr : to) {
            //result.append("<").append(addr).append(">, ");
            result.append(addr).append(", ");

        }
        result.deleteCharAt(result.toString().length() - 2);
        result.append(crlf);

        result.append("Subject: ").append(subject).append(crlf).append(crlf);
        result.append(body);
        result.append(crlf);
        result.append(genQuit());
        return result.toString();
    }

    static private String genQuit() {
        return crlf+"."+crlf;
    }
}
