package com.csumb.cst438.a1;  
import java.net.*;
import java.util.*;
import com.sun.net.httpserver.*;
import java.io.*;

public class MyHttpServer {

	static final String RESOURCE_DIR = "src/main/resources/";
	static final int PORT = 8080;
        static Random generator = new Random();

	public static void main(String[] args) {

		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
			System.out.print("Server started. ");
			// Display current directory
			System.out.print("pwd=" + System.getProperty("user.dir"));
			server.createContext("/", new MyHandler());
			server.setExecutor(null); // creates a default executor
			server.start();
			System.out.println();
			System.out.println("MyHttp Server running.");
		} catch (Exception e) {
			System.out.println("My HttpServer Exception: " + e.getMessage());
		}
	}

	public static class MyHandler implements HttpHandler {
            
                private Game game = new Game();
                private String cookie="0";     // used to keep track of current game 

                /**
                 * handle an HTTP request
                 * @param t
                 * @throws IOException 
                 */
                
                private String errorMessage(boolean invalid) {
                    return invalid ? "<br /><p style=\"color: red;\">Please enter a valid letter</p><br />" : "";
                }
                
		public void handle(HttpExchange t) throws IOException {
			String uri = t.getRequestURI().toString();
                        boolean invalid_char = true;
			System.out.println("URI=" + uri);
                        // Check length of guess parameter
                        if(uri.startsWith("/?guess=")){
                            String user_val = uri.substring(uri.indexOf("/?guess=") + 8, uri.length());
                            if(user_val.length() == 1 && (((user_val.charAt(0) >= 65 && user_val.charAt(0) <= 90)) || (user_val.charAt(0) >= 97 && user_val.charAt(0) <= 122))) {
                                invalid_char = false;
                            }                       
                        }

			if (uri.endsWith(".gif") || uri.endsWith(".ico")) {
				// http get request for an image file
				try {
                                    sendFile(t, uri.substring(1));
                                } catch(Exception e) {
                                    
                                }
			} else {
				// come here to play the game
                                String response = "";
                                // get cookie value from http   request
                                String requestCookie = t.getRequestHeaders().getFirst("Cookie");
                                System.out.println("Cookie=" + requestCookie);
                                // if there is no cookie, or it is "0" or differfent from the current value, then start a new game
                                if (requestCookie==null || requestCookie.equals("0") || cookie.equals("0") || !requestCookie.equals(cookie)) {
                                    game.startNewGame();
                                    cookie=generateCookie();    
                                    response = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                            + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                                            + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + game.getDisplayWord() + "</h2>"
                                            + "<form action=\"/\" method=\"get\"> "
                                            + "Guess a character <input type=\"text\" name=\"guess\"><br>"
                                            + "<input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
                                } else {
                                    // continue with current game
                                    char ch = uri.charAt(uri.length()-1);  // letter that user has guessed
                                    int result;
                                    if(invalid_char)
                                        result = 0;
                                    else
                                        result = game.playGame(ch);
                                    switch(result) {
                                        case 0: // good guess, continue game
                                            response = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                            + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                                            + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + game.getDisplayWord() + "</h2>"
                                            + "<form action=\"/\" method=\"get\"> " + errorMessage(invalid_char) 
                                            + "Guess a character <input type=\"text\" name=\"guess\"><br>"
                                            + "<input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
                                            break;
                                        case 1: // good guess, win game
                                             response = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                            + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                                            + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + "</h2>"
                                            + "<h2>Congratulations you win!</h2>" + "</body></html>";
                                             cookie="0";
                                             break;
                                        case 2: // bad guess, continue game
                                            response = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                            + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                                            + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + game.getDisplayWord() + "</h2>"
                                            + "<form action=\"/\" method=\"get\"> "
                                            + "Guess a character <input type=\"text\" name=\"guess\"><br>"
                                            + "<input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
                                            break;
                                        case 3: // bad guess, lost game
                                             response = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                            + "<img src=\"" + "h7.gif" + "\">" + "<h2>You lost!  The word is " + game.getWord() + "</h2>"
                                            + "</body></html>";
                                             cookie="0";
                                             break;
                                    }
                                    
                                }
                                t.getResponseHeaders().set("Content-Type", "text/html");
                                if (cookie!=null) 
                                     t.getResponseHeaders().set("Set-Cookie", cookie);
                                else 
                                     t.getResponseHeaders().set("Set-Cookie", "0");
                                System.out.println("New cookie:" + cookie);
                                t.sendResponseHeaders(200, response.length());
                                System.out.println("response=" + response);
                                OutputStream os = t.getResponseBody();
                                os.write(response.getBytes());
                                os.close();
                         }
                }

                /*
                 * send a gif file
                 */
                private static void sendFile(HttpExchange t, String filename) {

                        try {
                                File file = new File(RESOURCE_DIR + filename);
                                byte[] fileData = new byte[(int) file.length()];
                                DataInputStream dis = new DataInputStream(new FileInputStream(file));
                                dis.readFully(fileData);
                                dis.close();

                                t.getResponseHeaders().set("Content-Type", "image/gif");
                                t.sendResponseHeaders(200, fileData.length);
                                OutputStream os = t.getResponseBody();
                                os.write(fileData);
                                os.close();

                        } catch (Exception e) {
                                System.out.println("Error in sendFile:" + filename + " " + e.getMessage());
                                String err = "Error fetching file";
                                try {
                                    t.sendResponseHeaders(404, err.length());
                                    OutputStream os = t.getResponseBody();
                                    os.write(err.getBytes());
                                    os.close();
                                } catch(Exception e2) {
                                    System.out.println(e2.getMessage());
                                }
                        }

                }
        
                /*
                * generate a random cookie which is a random long integer
                */
                 private String generateCookie() {
                    return Long.toString(generator.nextLong());
                 }
                 
        }  // end of static class MyHandler
        
}
