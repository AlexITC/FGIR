package com.alex.mail;

import java.sql.*;
import java.sql.Date;

import javax.mail.*;
import javax.mail.event.*;

public class MailHandler  extends MessageCountAdapter {

  public synchronized void messagesAdded(MessageCountEvent ev) {
    Message[] msgs = ev.getMessages();

    // Just dump out the new messages
    for (int i = 0; i < msgs.length; i++) {
      handleMessage(msgs[i]);
    }
  }
  
  private final static String ADDED_TO_GROUP = "added you to the group";
  private final static String FACEBOOK_LINK  = "https://www.facebook.com";
  private final static String POSTED_IN = "posted in";
  private final static String PHOTO_ATTACHMENT = "Photo attachment:";
  private final static String REPLY_TO_THIS_EMAIL = "Reply to this email to comment on this post";
  
  /**
   * handle message received from facebook
   * @param msg
   */
  public synchronized void handleMessage(Message msg)  {

    try  {
      
      // read messages from facebook only
      if  ( !msg.getFrom()[0].toString().toLowerCase().contains("facebook") ) {
        return;
      }
      
      // it can be two thigs, added to a group or message in a group
      String subject = msg.getSubject();
      if  ( subject.contains(ADDED_TO_GROUP) )  {
        handleAddedToGroup(msg);
        return;
      }
      String content = getContent(msg);
      if  ( content.contains(POSTED_IN) )
        handlePostedIn(msg, content);
      
    }  catch (Exception e)  {
      System.out.println(" READ ERROR: ");
      e.printStackTrace();
    }
  }

  /**
   * handle message posted in a group
   * this adds the new post to the database
   * @param msg message containing the post data
   * @throws Exception if any error occurs or the message is wrong
   */
  private void handlePostedIn(Message msg, String content) throws Exception  {
    Connection connection = null;
    PreparedStatement pst = null;
    boolean error = true;
    try  {
      String text = POSTED_IN;
      String subject = msg.getSubject();
      String s;
      
      // get sho
      int idx = content.indexOf(text);
      String username = content.substring(0, idx - 1);
      
      text = content;
      // get description
      idx = text.indexOf(PHOTO_ATTACHMENT);
      String description;
      String photo = "";
      boolean hasPhoto = idx >= 0;
      if  ( !hasPhoto )  {
        idx = text.indexOf(REPLY_TO_THIS_EMAIL);
      }
      description = text.substring(0, idx);
      description = description.substring( description.indexOf("\n"), description.length() );
      
      // get photo
      if  ( hasPhoto )  {
        // contains photo
        s = "&set=";
        photo = text.substring(idx + PHOTO_ATTACHMENT.length(), text.indexOf(s, idx) );
      }

      // get publication link
      idx = text.indexOf(REPLY_TO_THIS_EMAIL);
      text = text.substring(idx + REPLY_TO_THIS_EMAIL.length() + 1);
      
      idx = text.indexOf(FACEBOOK_LINK);
      String link = text.substring(idx, text.indexOf("%2F&aref=", idx) );
      
      // get group id
      s = "groups%2F";
      idx = link.indexOf(s);
      String groupId = link.substring( idx + s.length(), link.indexOf("%2Fpermalink", idx) );
      
      // get pub id
      s = "permalink%2F";
      idx = link.indexOf(s);
      String pubId = link.substring(idx + s.length() );
      
      // get user id
      s = "https://www.facebook.com/n/?profile.php&amp;id=";
      idx = text.indexOf(s);
      String userId = text.substring( idx + s.length(), text.indexOf("&amp;aref=", idx) );
      // save data
      Timestamp ts = new Timestamp(msg.getReceivedDate().getTime());
      
      
      pubId = pubId.trim();
      groupId = groupId.trim();
      userId = userId.trim();
      description = description.trim();
      photo = photo.trim();
      username = username.trim();
      
      System.out.println("pub added added: " );
      System.out.println(" pubid: " + pubId);
      System.out.println(" groupId: " + groupId);
      System.out.println(" userid: " + userId);
      System.out.println(" username: " + username);
      System.out.println(" desc: " + description);
      System.out.println(" photo: " + photo);
      System.out.println(" time: " + ts);
      System.out.println();
      
      
      connection = com.alex.db.Connector.getConnection();
      pst = connection.prepareCall("SELECT facebook.addPublication(?,?,?,?,?,?,?);");
      pst.setString(1, pubId);
      pst.setString(2, groupId);
      pst.setString(3, userId);
      pst.setString(4, username);
      pst.setString(5, description);
      pst.setString(6, photo);
      pst.setTimestamp(7, ts );
      
      pst.execute();
      connection.commit();
      error = false;
    }  catch (SQLException e) {
      if  ( connection != null )
      throw new Exception("SQLException: " + e.getMessage());
    }  catch (Exception e)  {
      throw new Exception("Error handling message added to a group");
    }  finally {
      if  ( pst != null )
        pst.close();
      if  ( connection != null )  {
        if  ( error )
          connection.rollback();
        connection.close();
      }
    }
  }

  /**
   * handle message added to group
   * this adds the new group to the database
   * @param msg message containing the group data
   * @throws Exception if any error occurs or the message is wrong
   */
  private void handleAddedToGroup(Message msg) throws Exception  {
    Connection connection = null;
    PreparedStatement pst = null;
    boolean error = true;
    try  {
      String text = ADDED_TO_GROUP;
      int idx = msg.getSubject().indexOf(text);
      text = msg.getSubject().substring(idx + text.length() );
      
      idx = text.indexOf("\"");
      text = text.substring(idx + 1);
      idx = text.indexOf("\"");
      text = text.substring(0, idx);
      
      String groupName = text;
      
      // get link
      text = getContent(msg);
      idx = text.indexOf(FACEBOOK_LINK);
      text = text.substring(idx);
      idx = text.indexOf("%2F&aref=");
      text = text.substring( 0, idx );
      String s = "groups%2F";
      idx = text.indexOf(s);
      String id = text.substring(idx + s.length());
      
      connection = com.alex.db.Connector.getConnection();
      pst = connection.prepareCall("SELECT facebook.addGroup(?, ?);");
      pst.setString(1, id);
      pst.setString(2, groupName);
      pst.execute();
      connection.commit();
      error = false;
      System.out.println("group added: " + groupName );
    }  catch (SQLException e) {
      if  ( connection != null )
      throw new Exception("SQLException: " + e.getMessage());
    }  catch (Exception e)  {
      throw new Exception("Error handling message added to a group");
    }  finally {
      if  ( pst != null )
        pst.close();
      if  ( connection != null )  {
        if  ( error )
          connection.rollback();
        connection.close();
      }
    }
  }
  
  /**
   * gets content from a message
   * @param msg
   * @return the message content
   * @throws Exception
   */
  private String getContent(Message msg)  throws Exception  {
    try  {
      Object obj = msg.getContent();
      if  ( obj instanceof Multipart )  {
        Multipart mp = (Multipart) msg.getContent();
            
        StringBuilder sb = new StringBuilder();
        int cnt = mp.getCount();
        for (int i = 0; i < cnt; i++)  {
          BodyPart bp = mp.getBodyPart(i);
          
          sb.append( bp.getContent() );
          sb.append("\n");
        }
        
        return  new String( sb.toString().getBytes(), "UTF-8");
      }
      return  obj.toString();
    }  catch(Exception e)  {
      throw new Exception("Error getting content from message");
    }
  }

  public void print(Message msg)  {
    try  {
      Address[] addr = msg.getFrom();
      System.out.println("from:" );
      for (Address a : addr)
        System.out.println( " " + a + ", " );
      
      System.out.println("SENT DATE:" + msg.getSentDate());
      System.out.println("SUBJECT:" + msg.getSubject());

      System.out.println("CONTENT:" + getContent(msg) );
      System.out.println("\n");
    
    }  catch (Exception e)  {
      System.out.println(" READ ERROR: ");
      e.printStackTrace();
    }
  }
}
