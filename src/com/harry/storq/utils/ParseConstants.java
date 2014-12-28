package com.harry.storq.utils;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseObject;
import com.parse.ParseUser;

public final class ParseConstants {
	// Class name
	public static final String CLASS_MESSAGES = "Messages";
	
	// Field names
	public static final String KEY_USER_ID = "userId";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_FRIENDS_RELATION = "friendsRelation";
	public static final String KEY_RECIPIENT_IDS = "recipientIds";
	public static final String KEY_SENDER_ID = "senderId";
	public static final String KEY_SENDER_NAME = "senderName";
	public static final String KEY_FILE = "file";
	public static final String KEY_FILE_TYPE = "filetype";
	public static final String KEY_CREATED_AT = "createdAt";
	
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_VIDEO = "video";
	public static final String TYPE_TEXT = "text";
	public static final String KEY_OBJECT_ID = "objectId";
	public static final String KEY_STORQ = "storq";
	
	
	public static String[] parseString(String senders) {
        String[] tokens = senders.split(",");
		return tokens;
	}
	

	public static void deleteMessage(ParseObject message) {
		List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);
		
		if (ids.size() == 1) {
			// last recipient - delete the whole thing!
			message.deleteInBackground();
		}
		else {
			// remove the recipient and save
			ids.remove(ParseUser.getCurrentUser().getObjectId());
			
			ArrayList<String> idsToRemove = new ArrayList<String>();
			idsToRemove.add(ParseUser.getCurrentUser().getObjectId());
			
			message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
			message.saveInBackground();
		}
	}
	
}