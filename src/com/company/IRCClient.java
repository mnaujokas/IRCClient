package com.company;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.text.DefaultCaret;



public class IRCClient implements ActionListener, Runnable {

    private static final String HOST = "shell.riftus.lt";
    private static final int PORT = 6667;
    private final JFrame f = new JFrame();
    private final JTextField tf = new JTextField(25);
    private final JTextArea ta = new JTextArea(15, 50);
    private final JButton send = new JButton("Send");
    private BufferedWriter writer;
    private BufferedReader reader;
    private Scanner in;
    private Thread thread;
    private String line = null;
    private boolean connected = false;
    private boolean joined = false;
    private Parser parser;
    private Message data;

    private String nick = "mifas";
    private String login = "mifas";
    private String channel = "#test";


    public IRCClient(boolean status, String nickname, String targetChannel, BufferedReader in, BufferedWriter out) {
        connected = status;
        nick = nickname;
        channel = targetChannel;
        writer = out;
        reader = in;
        f.setTitle("IRC Client");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getRootPane().setDefaultButton(send);
        f.add(tf, BorderLayout.NORTH);
        f.add(new JScrollPane(ta), BorderLayout.CENTER);
        f.add(send, BorderLayout.SOUTH);
        f.setLocation(100, 300);
        f.pack();
        send.addActionListener(this);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) ta.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        display("client" + HOST + " on port " + PORT);
        thread = new Thread(this);
    }

    public IRCClient() {
        this(false, "miffas", "#test", null, null);
        joined = false;
    }

    public void start() {
        f.setVisible(true);
        thread.start();
    }

    //@Override
    public void actionPerformed(ActionEvent ae) {
        String s = tf.getText();
        if (writer != null) try {
            if (s.toLowerCase().startsWith("/join")) {
                channel = s.substring(5);
                //  new IRCClient(true, nick, s.substring(5), reader, writer).start();
            }
            if (s.toLowerCase().startsWith("/quit")) {
                writer.write(s.substring(1) + "\r\n");
                System.exit(0);
            }
            if (s.startsWith("/")) writer.write(s.substring(1) + "\r\n");
            else writer.write("PRIVMSG " + channel + " :" + s + "\r\n");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        display("@" + nick + "\t" + s);
        tf.setText("");
    }

    public void connect() {
        try {
            Socket socket = new Socket(HOST, PORT);


            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            System.out.println(reader.readLine());
            writer.write("NICK " + nick + "\r\n");
            writer.write("USER " + login + " 8 * : Java IRC client\r\n");
            writer.flush();

            line = null;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                data = parser.parse(line);
                if (data.command.equals("ping")) {
                    writer.write("PONG " + line.substring(5) + "\r\n");
                    writer.flush();
                    display(line);
                    display("pongas");

                }

                if (line.indexOf("004") >= 0) {
                    display("You are now logged in.");
                    break;
                } else if (line.indexOf("433") >= 0) {
                    display("Nickname is already in use.");
                    return;
                }
            }
            connected = true;
            joined = false;

        } catch (Exception e) {
            display(e.getMessage());
            e.printStackTrace(System.err);
        }
    }


    //@Override
    public void run() {
        try {
            if (!connected) connect();

            //writer.write("JOIN " + channel + "\r\n");
          // writer.flush();


            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                data = Parser.parse(line);
                if (data.command.equals("ping")) {
                    writer.write("PONG " + line.substring(5) + "\r\n");
                    writer.flush();
                    display(line);
                    display("pongas");

                } else if (data.command.equals("privmsg")) {
                    display("@" + data.nickname + ":\t" + data.content);

                } else if (data.command.equals("nick")) {
                    display("@" + data.nickname + " changed nickname to:\t" + data.content);
                    if (nick.equals(data.nickname)) nick = data.content;
                } else {
                    display(parser.removeOrigin(line, nick));
                }
            }
        } catch (Exception e) {
            display(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private void display(final String s) {
        EventQueue.invokeLater(new Runnable() {
            //@Override
            public void run() {
                ta.append(s + "\n");
            }
        });
    }


    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            //@Override
            public void run() {
                new IRCClient().start();
            }
        });
    }
}