package common;

import common.Exceptions.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Interface meant to be implemented by any classes that need to understand
 * the Chatterbox protocol.
 */
public interface ChatterboxProtocol {
    //------------------------------------------<CONSTANTS>-------------------------------------------//
    /**
     * Chatterbox clients and servers should always use this port to establish
     * socket connections.
     */
    public static final int PORT = 6789;

    /**
     * Used to separate tokens in protocol messages.
     */
    public static String SEPARATOR = "::";

    //-----------------------------<CLIENT MESSAGES>------------------------------//
    //      sent from client to server

    /**
     * The protocol message sent from the client to the Chatterbox server to
     * initially connect to the chat room.
     */
    public static String CONNECT = "connect";

    /**
     * The protocol message sent from the client to the Chatterbox server to
     * disconnect from the chat room.
     */
    public static String DISCONNECT = "disconnect";

    /**
     * The protocol message sent from the client to the Chatterbox server to
     * send a message to the chat room.
     */
    public static String SEND_CHAT = "send_chat";

    /**
     * The protocol message sent from the client to the Chatterbox server to
     * send a whisper to another user in the chat room.
     */
    public static String SEND_WHISPER = "send_whisper";

    /**
     * The protocol message sent from the client to the Chatterbox server to
     * list the users currently connected to the chat room.
     */
    public static String LIST_USERS = "list_users";


    //-----------------------------<SERVER MESSAGES>------------------------------//
    //      sent from server to client

    /**
     * The protocol message sent from the Chatterbox server to client to
     * indicate that the connection was successful.
     */
    public static String CONNECTED = "connected";

    /**
     * The protocol message sent from the Chatterbox server to client to
     * indicate that the disconnection was successful.
     */
    public static String DISCONNECTED = "disconnected";

    /**
     * The protocol message sent from the Chatterbox server to client to
     * indicate that the chat message was successfully received.
     */
    public static String CHAT_RECEIVED = "chat_received";

    /**
     * The protocol message sent from the Chatterbox server to client to
     * indicate that the whisper was successfully received.
     */
    public static String WHISPER_RECEIVED = "whisper_received";

    /**
     * The protocol message indicating that a whisper has been successfully
     * sent to its intended recipient; used as an acknowledgement from the
     * server to sender that the message was delivered.
     */
    public static String WHISPER_SENT = "whisper_sent";

    /**
     * The protocol message sent from the Chatterbox server to client to
     * respond to a request for a list of users.
     */
    public static String USERS = "users";

    /**
     * The protocol message sent from the Chatterbox server to client to
     * notify the client that a new user has joined the chat room.
     */
    public static String USER_JOINED = "user_joined";

    /**
     * The protocol message sent from the Chatterbox server to client to
     * notify the client that a user has left the chat room.
     */
    public static String USER_LEFT = "user_left";

    /**
     * sent whisper specified an invalid target
     */
    public static String TARGET_ERROR = "target_error";

    /**
     * Client never negotiated a username with server
     */
    public static String NON_INITIALIZED_ERROR = "not_initialized_error";

    /**
     * Used whe negotiating username to signify an invalid name
     */
    public static String NAME_TAKEN_ERROR = "name_taken_error";

    /**
     * parse error, bad message sent
     */
    public static String PARSE_ERROR = "parse_error";

    /**
     * The protocol message sent from the ChatterBoc server to the client to
     * notify the client that a fatal error has occurred; this will be
     * followed by a termination of the connection between the client and the
     * server.
     */
    public static String FATAL_ERROR = "fatal_error";

    //--------------------------------------------<Fields>--------------------------------------------//


    //-------------------------------------------<Methods>--------------------------------------------//

    /**
     * If characters are available on the stream, this returns the next one as a 'String'.
     * otherwise, it returns a 'null'
     *
     * @param stream the 'InputStream' to check
     * @param reader 'stream's 'BufferedReader'
     * @return returns next 'char' from 'reader' as a 'String' if available, or 'null' if no new
     * character is available
     */
    default String nextLine (InputStream stream, Scanner reader) {
        String toReturn = "";
        try {
            if (stream.available () > 0) {
                toReturn = reader.nextLine ();
            }
        } catch (IOException e) {
            // Ignore this
        }

        return toReturn + "\n";
    }


    /**
     * Parses a User-side command, either creating it to send to the server or decoding it for
     * server-side execution
     *
     * @param strIn The 'String' to parse
     * @return The parsed 'String'
     */
    void ParseUserCommand (String strIn) throws ParseException;


    /**
     * Parses a Server-side command, either creating it to send to the user or decoding it for
     * client-side display
     *
     * @param strIn The 'String' to parse
     * @return The parsed 'String'
     */
    void ParseServerCommand (String strIn) throws ParseException;


}
