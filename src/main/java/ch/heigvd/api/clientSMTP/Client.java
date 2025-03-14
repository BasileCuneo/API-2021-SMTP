package ch.heigvd.api.clientSMTP;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;
import static java.lang.Integer.parseInt;

/**
 * Calculator client implementation
 */
public class Client {

    private static final Logger LOG = Logger.getLogger(Client.class.getName());
    private static final String crlf = "\r\n";
    private static String utfEnable = "Content-Type: text/plain; charset=UTF-8";
    /**
     * Main function to run client
     *
     * @param args no args required
     */
    public static void main(String[] args)  {
        int nGroups = 0;
        try{
            if(args.length != 1){
                throw new IllegalArgumentException("Usage: java -jar client <nGroup>");

            }
            nGroups = parseInt(args[0]);
        }catch(NumberFormatException e){
            System.out.println("Usage: java -jar client <nGroup>");
            return;
        }
        ConfigManager config = new ConfigManager("src/configuration",
                                                 nGroups);
        LOG.log(Level.INFO,"Configuration ok");

        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%6$s%n");

        Socket clientSocket = null;
        BufferedWriter clientOut = null;
        BufferedReader clientIn = null;
        try {
            clientSocket = new Socket("127.0.0.1", 2525);//pour mockmock
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
            try{
                if(clientOut != null)
                    clientOut.close();
                if(clientIn != null)
                    clientIn.close();
                if(clientSocket != null)
                    clientSocket.close();
                return;
            }catch (IOException e1){
                e1.printStackTrace();

            }

        }
        try{
            if(clientOut != null)
                clientOut.close();
            if(clientIn != null)
                clientIn.close();
            if (clientSocket != null)
                clientSocket.close();

        }catch (IOException e){
            e.printStackTrace();

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
            clientOut.write(utfEnable+crlf);
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
        //delete the last ", "
        result.deleteCharAt(result.toString().length() - 2);
        result.append(crlf);
        String b64Subject =
                Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8));
        result.append("Subject: =?utf-8?B?")
                .append(b64Subject).append("?=").append(crlf).append(crlf);

        result.append(body);
        result.append(crlf);
        result.append(genQuit());
        return result.toString();
    }

    static private String genQuit() {
        return crlf+"."+crlf;
    }
}
