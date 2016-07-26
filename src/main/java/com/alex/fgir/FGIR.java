package com.alex.fgir;

import com.alex.mail.*;

/**
 * FGIR (Facebook Groups Information Retriever)
 * 
 * is an application to retrieve and organize information from facebook groups
 * into a database
 * 
 * @author Alexis Hernandez
 */
public class FGIR {

  public static void main(String[] args) {
    if (args.length != 2) {
      throw new RuntimeException("Usage java FGIR [email] [password]");
    }

    try {
      final String EMAIL = args[0];
      final String PASSWORD = args[1];
      GMailReader gmail = new GMailReader(EMAIL, PASSWORD);

      // GMailReader is listening for messages in and handles them in
      // a separate thread, sleep forever to keep the program running
      while (true)
        Thread.sleep(1000 * 60 * 60 * 24);
      
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
  }

}
