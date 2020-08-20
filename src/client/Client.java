package client;

import common.ChatterboxProtocol;
import common.Exceptions.ParseException;
import server.ChatterboxServer;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements ChatterboxProtocol {
    /**
     * Color Codes from
     * https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
     */
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main (String[] args) throws IOException {
        Client client = null;
        if (args.length == 0) {
            client = new Client ("localHost");
        } else {
            client = new Client (args[0]);
        }
        client.Connect ();

        while (client.isConnected ()) {
            try {
                String fromConsole = client.GetConsoleCommand ();
                if (!fromConsole.isEmpty ()) {
                    client.ParseUserCommand (fromConsole);
                }
            } catch (ParseException e) {
                //
            }


            try {
                String fromServer = client.GetServerCommand ();
                if (!fromServer.isEmpty ()) {
                    client.ParseServerCommand (fromServer);
                }
            } catch (ParseException e) {
                //
            }

        }


    }

    //--------------------------------------------<Fields>--------------------------------------------//
    //------------------------------<Network Layer>-------------------------------//
    private Socket socket;

    //---------------------------------<Inputs>-----------------------------------//
    //  Streams
    private InputStream serverIn;
    private InputStream consoleIn;

    //  Readers
    private Scanner serverReader;
    private Scanner consoleReader;

    //  Buffers
    private String serverInBuffer;
    private String consoleInBuffer;


    //---------------------------------<Outputs>----------------------------------//
    //  Streams
    private OutputStream serverOut;

    //  Writers
    private PrintStream serverPrinter;

    //  Buffers

    //-----------------------------------<Misc>-----------------------------------//
    private boolean connected;
    private boolean useSound;
    private boolean verboseChat;
    private String username;


    //-------------------------------------------<Methods>--------------------------------------------//
    public Client () throws IOException {
        new Client ("localHost");
    }

    public Client (String hostName) throws IOException {
        try {
            this.socket = new Socket (hostName, ChatterboxProtocol.PORT);
        } catch (IOException e) {
            System.out.println ("Failed to connect to Server, shutting down client");
            System.exit (0);
        }
        serverIn = socket.getInputStream ();
        consoleIn = System.in;
        serverOut = socket.getOutputStream ();

        serverInBuffer = "";
        consoleInBuffer = "";

        serverReader = new Scanner (serverIn);
        consoleReader = new Scanner (consoleIn);
        serverPrinter = new PrintStream (serverOut);

        connected = false;
        useSound = false;
        verboseChat = true;
    }

    public void Connect () throws IOException {
        while (!connected) {
            System.out.print ("Choose a username: ");
            this.username = consoleReader.nextLine ().trim ();
            if (username.compareToIgnoreCase ("Quit") == 0)
                this.Close ();
            else {
                serverPrinter.println (ChatterboxProtocol.CONNECT + ChatterboxProtocol.SEPARATOR + username);
                System.out.println ("Waiting for connection acc");
                String response = "";
                do {
                    response = this.GetServerCommand ().trim ();
                } while (response.isEmpty ());
                System.out.println (response);

                if (response.compareTo (ChatterboxProtocol.CONNECTED) == 0) {
                    connected = true;
                    this.username = username;
                    System.out.println ("Connected to server as " + username);
                }
            }
        }
    }


    public void Close () {
        // close the connection safely; insure all written data is sent
        try {
            socket.shutdownOutput ();
            socket.shutdownInput ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    /**
     * Parses user input into a command for transmission to server
     *
     * @param strIn The 'String' to parse
     * @throws ParseException Iff the command tag to receive is not recognized
     */
    @Override
    public void ParseUserCommand (String strIn) throws ParseException {
        boolean toMute = false;
        String toCompare = strIn.trim ();

        if (toCompare.isEmpty ()) {
            //IDFK
        } else if (toCompare.charAt (0) == '/') {
            try {
                String commandTag = toCompare.substring (1).split (" ")[0];
                switch (commandTag.toUpperCase ().trim ()) {
                    case "LIST":
                        serverPrinter.println (ChatterboxProtocol.LIST_USERS);
                        break;
                    case "TELL":
                    case "WHISPER":
                    case "MSG":
                        String[] data = toCompare.split (" ", 3);
                        serverPrinter.println (ChatterboxProtocol.SEND_WHISPER + ChatterboxProtocol.SEPARATOR + data[1] +
                                ChatterboxProtocol.SEPARATOR + data[2]);
                        break;
                    case "DISCONNECT":
                    case "DCN":
                        serverPrinter.println (ChatterboxProtocol.DISCONNECT);
                        break;
                    case "SOUND":
                        useSound = !useSound;
                        break;
                    case "V":
                        verboseChat = !verboseChat;
                        break;
                    default:
                        throw new ParseException (strIn);

                }
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                System.out.println ("\t***ERROR! too few arguments in command");
            }
        } else {
            serverPrinter.println (ChatterboxProtocol.SEND_CHAT + ChatterboxProtocol.SEPARATOR + strIn);
        }

    }

    /**
     * @param strIn The 'String' to parse
     * @throws ParseException Iff the command tag sent is not recognized
     */
    @Override
    public void ParseServerCommand (String strIn) throws ParseException {
        if (!strIn.trim ().isEmpty ())
//            System.out.println ("Passed server command: " + strIn);
        strIn = strIn.trim ();
        String[] data = strIn.split (ChatterboxProtocol.SEPARATOR, 2);

        switch (data[0]) {
            case ChatterboxProtocol.CONNECTED:
                System.out.println ("Connected to server");
                break;
            case ChatterboxProtocol.DISCONNECTED:
                System.out.println ("disconnected from the server, shutting down");
                this.Close ();
                System.exit (42);
                break;
            case ChatterboxProtocol.CHAT_RECEIVED:
                if (useSound)
                    java.awt.Toolkit.getDefaultToolkit ().beep ();
                data = strIn.split (ChatterboxProtocol.SEPARATOR, 3);
                if (data[1].trim ().equals (username.trim ())) {

                    System.out.println (ANSI_BLUE + "<you> " + data[2].trim () + ANSI_RESET);
                } else {
                    System.out.println (ANSI_RED + "<" + data[1] + "> " + data[2].trim () + ANSI_RESET);
                }


                break;
            case ChatterboxProtocol.WHISPER_SENT:
                data = strIn.split (ChatterboxProtocol.SEPARATOR, 3);
                System.out.println (ANSI_CYAN + "[direct] <You -> " + data[1] + "> " + data[2].trim () + ANSI_RESET);
                break;
            case ChatterboxProtocol.WHISPER_RECEIVED:
                if (useSound)
                    java.awt.Toolkit.getDefaultToolkit ().beep ();
                data = strIn.split (ChatterboxProtocol.SEPARATOR, 3);
                System.out.println (ANSI_GREEN + "[direct] <" + data[1] + " -> You> " + data[2].trim () + ANSI_RESET);
                break;
            case ChatterboxProtocol.USERS:
                data = strIn.split (ChatterboxProtocol.SEPARATOR);
                System.out.println (data.length - 1 + " users currently online:");
                for (int i = 1; i < data.length; i++) {
                    String aData = data[i];
                    System.out.println ("\t" + aData);
                }
                break;
            case ChatterboxProtocol.USER_JOINED:
                if (verboseChat)
                    System.out.println ("*****User '" + data[1] + "' has joined the chat*****");
                break;
            case ChatterboxProtocol.USER_LEFT:
                if (verboseChat)
                    System.out.println ("*****User '" + data[1] + "' has left the chat*****");
                break;
            case ChatterboxProtocol.TARGET_ERROR:
                System.out.println ("\t***ERROR! The user you whispered to, '" + data[1] + "' is not logged onto the " +
                        "server");
                break;
            case ChatterboxProtocol.NON_INITIALIZED_ERROR:
                System.out.println ("\t***ERROR! You have not negotiated a username with the server");
                connected = false;
                try {
                    this.Connect ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
                break;
            case ChatterboxProtocol.NAME_TAKEN_ERROR:
                System.out.println ("\t***ERROR! the username you have chosen ('" + data[1] + "') is already taken");
                break;
            case ChatterboxProtocol.PARSE_ERROR:
                System.out.println ("\t***Error! The server encountered an error in attempting to parse your command");
                break;
            default:
                throw new ParseException (strIn);
        }
    }

    private String NextConsole () {
        return nextLine (consoleIn, consoleReader);
    }

    private String NextServer () {
        return nextLine (serverIn, serverReader);
    }

    private String GetConsoleCommand () throws ParseException {
        String lastLine = NextConsole ();
        if (!lastLine.isEmpty ()) {
            if (lastLine.charAt (lastLine.length () - 1) == '\n') {
                String temp = consoleInBuffer + lastLine;
                consoleInBuffer = "";
                return (temp);
            } else {
                consoleInBuffer += lastLine;
            }
        }

        return "";
    }

    private String GetServerCommand () {
        String lastLine = NextServer ();
        if (!lastLine.isEmpty ()) {
            serverInBuffer += lastLine;
            if (lastLine.charAt(lastLine.length () - 1) == '\n') {
                String temp = serverInBuffer;
                serverInBuffer = "";
                return temp;
            }
        }

        return "";
    }

    public boolean isConnected () {
        return connected;
    }
}

