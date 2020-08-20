package server;


import common.ChatterboxProtocol;
import common.Exceptions.InvalidRecipientException;
import common.Exceptions.NameTakenException;
import common.Exceptions.ParseException;
import common.Exceptions.UserNotInitializedException;
import javafx.css.CssParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection implements Runnable, ChatterboxProtocol {

    private final Socket socket;
    private String username;
    private String clientInBuffer;
    private boolean connected;

    private ServerBase server;

    private InputStream clientIn;
    private OutputStream clientOut;

    private Scanner clientReader;
    private PrintWriter clientWriter;

    public ClientConnection (Socket socket, ServerBase server) throws IOException {
        this.socket = socket;

        username = "";
        clientInBuffer = "";
        connected = false;

        this.server = server;

        clientIn = socket.getInputStream ();
        clientOut = socket.getOutputStream ();

        clientReader = new Scanner (clientIn);
        clientWriter = new PrintWriter (clientOut);

    }

    @Override
    public void run () {
        //---------------------------------<Connect Phase>----------------------------------//
        try {
            Connect ();
        } catch (ParseException e) {
            //
        }

        //-----------------------------------<Main Phase>-----------------------------------//
        while (connected) {
            Process ();
        }

        //---------------------------------<Shutdown Phase>---------------------------------//
    }

    private void Connect () throws ParseException {
        while (!connected && socket.isConnected ()) {
            String[] data = clientReader.nextLine ().trim ().split (
                    ChatterboxProtocol.SEPARATOR);

            if (!data[0].equals (ChatterboxProtocol.CONNECT)) {
                clientWriter.println (ChatterboxProtocol.NON_INITIALIZED_ERROR);
            } else {
                try {
                    username = data[1];
                    server.connect (this);
                    connected = true;

                } catch (NameTakenException e) {
                    try {
                        ParseServerCommand (ChatterboxProtocol.NAME_TAKEN_ERROR +
                                ChatterboxProtocol.SEPARATOR + username);
                    } catch (ParseException e1) {
                        //Do nothing, will work
                    }
                }
            }
        }
    }

    public void setConnected (boolean connected) {
        this.connected = connected;
    }

    private void Process () {
        try {
            String fromClient = GetClientCommand ();
            if (!fromClient.isEmpty ()) {
                ParseUserCommand (fromClient);
            }
        } catch (ParseException e) {
            //
        }
    }

    public String getUsername () {
        return username;
    }


    @Override
    public void ParseUserCommand (String strIn) throws ParseException {
        strIn = strIn.trim ();
        String[] data = strIn.split (ChatterboxProtocol.SEPARATOR, 2);

        try {
            switch (data[0]) {
                case ChatterboxProtocol.SEND_CHAT:
                    server.broadcast (username, data[1]);
                    break;
                case ChatterboxProtocol.SEND_WHISPER:
                    data = strIn.split (ChatterboxProtocol.SEPARATOR, 3);
                    server.whisper (username, data[1], data[2]);
                    break;
                case ChatterboxProtocol.LIST_USERS:
                    server.getUserList (this);
                    break;
                case ChatterboxProtocol.DISCONNECT:
                    server.disconnect (this);
                    break;
                default:
                    throw new ParseException (strIn);
            }
        } catch (UserNotInitializedException e) {
            ParseServerCommand (ChatterboxProtocol.NON_INITIALIZED_ERROR);
        } catch (InvalidRecipientException e) {
            ParseServerCommand (ChatterboxProtocol.TARGET_ERROR + ChatterboxProtocol.SEPARATOR + e.getMessage ());
        } catch (ParseException e) {
            //ParseServerCommand (ChatterboxProtocol.PARSE_ERROR);
        }

    }

    @Override
    public void ParseServerCommand (String strIn) throws ParseException {
        //System.out.println ("Sent '" + strIn + "' to user " + username);
        clientWriter.println (strIn);
        clientWriter.flush ();
    }

    private String NextClient () {
        return nextLine (clientIn, clientReader);
    }

    private String GetClientCommand () {
        String lastLine = NextClient ();
        if (!lastLine.isEmpty ()) {
            clientInBuffer += lastLine;
            if (lastLine.charAt (lastLine.length () - 1) == '\n') {
                String temp = clientInBuffer;
                clientInBuffer = "";
                return temp;
            }
        }

        return "";
    }
}
