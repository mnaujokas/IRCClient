

package com.company;

public class Parser {

    public static Message parse(String ircMessage) {
        int spIndex;
        Message message = new Message();

        if (ircMessage.startsWith(":")) {
            spIndex = ircMessage.indexOf(' ');
            if (spIndex > -1) {
                message.origin = ircMessage.substring(1, spIndex);
                ircMessage = ircMessage.substring(spIndex + 1);

                int uIndex = message.origin.indexOf('!');
                if (uIndex > -1) {
                    message.nickname = message.origin.substring(0, uIndex);
                }
            }
        }
        spIndex = ircMessage.indexOf(' ');
        if (spIndex == -1) {
            message.command = "null";
            return message;
        }

        message.command = ircMessage.substring(0, spIndex).toLowerCase();
        ircMessage = ircMessage.substring(spIndex + 1);

       // if (message.command.equals("372"))

       // parse privmsg params
        if (message.command.equals("privmsg")) {
            spIndex = ircMessage.indexOf(' ');
            message.target = ircMessage.substring(0, spIndex);
            ircMessage = ircMessage.substring(spIndex + 1);

            if (ircMessage.startsWith(":")) {
                message.content = ircMessage.substring(1);
            }
            else {
                message.content = ircMessage;
            }
        }

        // parse quit/join
        if (message.command.equals("quit") || message.command.equals("join")) {

            if (ircMessage.startsWith(":")) {
                message.content = ircMessage.substring(1);
            }
            else {
                message.content = ircMessage;
            }
        }

        // parse ping params
        if (message.command.equals("ping")) {
            spIndex = ircMessage.indexOf(' ');
            if (spIndex > -1) {
                message.content = ircMessage.substring(0, spIndex);
            }
            else {
                message.content = ircMessage;
            }
        }
        //parse nick
        if (message.command.equals("nick")){

            spIndex = ircMessage.indexOf(':');
            if (spIndex > -1) {
                message.content = ircMessage.substring(spIndex);
            }
            else {
                message.content = ircMessage;
            }
        }
        return message;
    }
    public static String removeOrigin(String ircMessage, String nick){
        int spIndex;


        if (ircMessage.startsWith(":")) {
            spIndex = ircMessage.indexOf(nick);
            if (spIndex > -1) {
                ircMessage = ircMessage.substring(spIndex + nick.length());
            }
        }
        return ircMessage;
    }

}