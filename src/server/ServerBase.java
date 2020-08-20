package server;


import common.ChatterboxProtocol;
import common.Exceptions.InvalidRecipientException;
import common.Exceptions.NameTakenException;
import common.Exceptions.ParseException;
import common.Exceptions.UserNotInitializedException;

import java.util.HashMap;
import java.util.Set;

public class ServerBase {
    private HashMap<String, ClientConnection> connectedUsers;

    public ServerBase () {
        this.connectedUsers = new HashMap<> ();
    }

    synchronized public void connect (ClientConnection client) throws NameTakenException, ParseException {
        String username = client.getUsername ();
        if (connectedUsers.containsKey (username) || username.contains (" ")) {
            throw new NameTakenException (username);
        } else {
            String toSend = ChatterboxProtocol.USER_JOINED + ChatterboxProtocol.SEPARATOR + username;
            TellAll (toSend);
            System.out.println ("********************User '" + username + "' Has Joined********************");
            connectedUsers.put (username, client);
            client.setConnected (true);
            client.ParseServerCommand (ChatterboxProtocol.CONNECTED);
            System.out.println ("Sent user '" + username + "' command '" + ChatterboxProtocol.CONNECTED + "'");
        }


    }

    synchronized private void TellAll (String message) {
        System.out.println ("Sent command '" + message + "' to all users");
        for (String username : connectedUsers.keySet ()) {
            try {
                connectedUsers.get (username).ParseServerCommand (message);
            } catch (ParseException e) {
                e.printStackTrace ();
            }
        }
    }


    synchronized public void disconnect (ClientConnection client) throws UserNotInitializedException, ParseException {
        if (!connectedUsers.containsKey (client.getUsername ())) {
            throw new UserNotInitializedException ();
        } else {
            client.ParseServerCommand (ChatterboxProtocol.DISCONNECTED);
            client.setConnected (false);
            connectedUsers.remove (client.getUsername ());
            String toSend = ChatterboxProtocol.USER_LEFT + ChatterboxProtocol.SEPARATOR + client.getUsername ();
            TellAll (toSend);
            System.out.println ("*********************User '" + client.getUsername () + "' Has Left*********************");
        }
    }

    synchronized public void broadcast (String sender, String message) throws UserNotInitializedException,
            ParseException {
        if (!connectedUsers.containsKey (sender)) {
            throw new UserNotInitializedException ();
        } else {
            TellAll (ChatterboxProtocol.CHAT_RECEIVED + ChatterboxProtocol.SEPARATOR + sender +
                    ChatterboxProtocol.SEPARATOR + message);
        }
    }

    synchronized public void whisper (String sender, String recipient, String message) throws
            UserNotInitializedException, InvalidRecipientException, ParseException {

        if (!connectedUsers.containsKey (sender)) {
            throw new UserNotInitializedException ();
        } else if (!connectedUsers.containsKey (recipient)) {
            throw new InvalidRecipientException (recipient);
        } else {
            String toSender = ChatterboxProtocol.WHISPER_SENT +
                    ChatterboxProtocol.SEPARATOR + recipient +
                    ChatterboxProtocol.SEPARATOR + message;
            String toRecipient = ChatterboxProtocol.WHISPER_RECEIVED +
                    ChatterboxProtocol.SEPARATOR + sender +
                    ChatterboxProtocol.SEPARATOR + message;

            System.out.println ("Sent command '" + toSender + "' to '" + sender + "'");
            System.out.println ("Sent command '" + toRecipient + "' to '" + recipient + "'");

            connectedUsers.get (sender).ParseServerCommand (toSender);
            connectedUsers.get (recipient).ParseServerCommand (toRecipient);
        }

    }

    public void getUserList (ClientConnection requestor) {
        String userList = ChatterboxProtocol.USERS;

        for (String username : connectedUsers.keySet ()) {
            userList = userList + ChatterboxProtocol.SEPARATOR + username;
        }
        System.out.println ("Sent user '" + requestor.getUsername () + "' command '" + userList + "'");

        try {
            requestor.ParseServerCommand (userList);
        } catch (ParseException e) {
            e.printStackTrace ();
        }
    }
}
