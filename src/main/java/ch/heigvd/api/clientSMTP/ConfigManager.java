package ch.heigvd.api.clientSMTP;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

/**
 * ConfigManager read the different config from the config files and store them.
 * a Client can then ask for random message to the config manager, get the list of
 * victim mails, the subject and everything in the config files.
 *
 * The config files are stored like that:
 *  rootFolder/
 *    - messages.txt
 *    - victims.txt
 *
 * messages.txt is the body of the message, each line is a new message.
 *
 */
public class ConfigManager {
    public class MailStruct{
        public String body;
        public String subject;
    }
    private ArrayList<String> addresses;
    private ArrayList<MailStruct> messages;

    /**
     * The list of groups, each group is a list of addresses.
     *
     */
    private ArrayList<ArrayList<String>> groupsEmail;
    private final String victimsPath = "/victims.txt";
    private final String messagesPath = "/messages";
    private int nGroups;
    /**
     * The list of victim mails
     * @param rootPath the root folder for the config files
     * @param n the number of groups
     */
    public ConfigManager(String rootPath, int n) {
        nGroups = n;
        addresses = new ArrayList<>();
        messages = new ArrayList<>();

        // read the victims file
        String pathV = rootPath + victimsPath;
        String pathM = rootPath + messagesPath;
        if( !(new File(pathV).exists() && new File(pathM).exists() && new File(pathM).isDirectory())){
            System.out.println("Problem in the files configuration");
            return;
        }
        BufferedReader readerVictims;
        BufferedReader readerMessages;
        try{
            readerVictims =
                    new BufferedReader(new InputStreamReader(new FileInputStream(pathV),
                                                             StandardCharsets.UTF_8));


        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to open");
            return;
        }
        try{
            String addr;
            while((addr = readerVictims.readLine()) != null) {
                if(!addr.contains("@") || !addr.contains(".") || addr.contains(" ")|| addr.contains("\t")){
                    //if the address is invalid, we don't add it to the address list
                    System.out.println("Invalid address: " + addr);
                    continue;
                }
                addresses.add(addr);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Unable to read victims addresses");
        }
        try{
            File[] files = new File(pathM).listFiles();
            if(files == null){
                System.out.println("No message file");
                return;
            }
            for(File f : files){
                if(!f.isFile()) {
                    continue;
                }
                readerMessages =
                        new BufferedReader(new InputStreamReader(new FileInputStream(f),
                                                                 StandardCharsets.UTF_8));
                messages.add(new MailStruct());
                String msg;
                msg = readerMessages.readLine();
                messages.get(messages.size() - 1).subject = msg;
                messages.get(messages.size() - 1).body = "";
                while((msg = readerMessages.readLine()) != null) {
                    messages.get(messages.size() - 1).body += msg + "\n";
                }

            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Unable to read messages");
        }

        Collections.shuffle(messages);
        Collections.shuffle(addresses);
    }
    private void generateGroups(){

        groupsEmail = new ArrayList<>();
        //create the groups
        for(int i = 0; i < nGroups; i++){
            groupsEmail.add(new ArrayList<>());
        }

        //populate the groups
        int nbAddressesPerGroup = addresses.size() / nGroups;
        int nbAddressesAdded = 0;
        if(nbAddressesPerGroup < 3){
           throw new IllegalArgumentException("The number of victims is too small");
        }
        for(int i = 0; i < nGroups; i++){
            for(int j = 0; j < nbAddressesPerGroup &&
                    i * nbAddressesPerGroup + j < addresses.size() ; j++){
                groupsEmail.get(i).add(addresses.get(i * nbAddressesPerGroup + j));
                nbAddressesAdded++;
            }
        }
        //adding the missing addresses if the number of groups was not a multiple of
        // nAdresses
        for(int i = nbAddressesAdded; i < addresses.size(); i++){
            groupsEmail.get(groupsEmail.size()-1).add(addresses.get(i));
        }
    }
    public ArrayList<ArrayList<String>> getGroupsEmail(){
        if(groupsEmail == null) {
            generateGroups();
        }
        return groupsEmail;
    }
    public MailStruct getRandomMail(){
        if(messages == null) {
            throw new RuntimeException("object was not initialized correctly");
        }
        int min = 0;
        int max = messages.size();
        //get random in [min, max)
        int i = (int) ((Math.random() * (max - min)) + min);
        return messages.get(i);
    }

}
