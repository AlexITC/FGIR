package com.alex.mail;

import java.awt.event.*;
import java.util.*;

import javax.mail.*;
import javax.mail.event.*;
import javax.mail.search.FlagTerm;

/**
 * GMailReader is a class for retrieving gmail messages
 * 
 * @author Alexis Hernandez
 */
public class GMailReader {
  
  public GMailReader(String username, String password)  throws Exception  {
    String folderName = "INBOX";
    try  {
      System.out.println("connecting to gmail");
      MailHandler handler = new MailHandler();
      
      Properties props = System.getProperties();
      props.setProperty("mail.store.protocol", "imaps");
      
      Session session = Session.getInstance(props);
  
      Store store = session.getStore("imaps");
      store.connect("imap.gmail.com", username, password);
  
      final Folder folder = store.getFolder(folderName);
      
      if  ( folder == null || !folder.exists() )
        throw new Exception("Folder " + folderName + " doesn't exists");
  
      folder.open( Folder.READ_WRITE );

      System.out.println("connected");
      
      // handle unread messages
      Message[] unreadMessages = folder.search( 
        new FlagTerm( new Flags(Flags.Flag.SEEN), false)
      );
      
      for (int k = 0; k < unreadMessages.length; k++)  {
        handler.handleMessage( unreadMessages[k] );
      }
      
      // handle new events
      folder.addMessageCountListener(handler);

      int delay = 30000;  // 5 seconds
      new javax.swing.Timer(delay, new ActionListener() {
        public void actionPerformed(ActionEvent e)  {
          try {
            folder.getMessageCount();
          } catch (MessagingException e1) {
            e1.printStackTrace();
          }
        }
      }).start();
    }  catch (Exception e)  {
      throw e;
    } 
  }
}
