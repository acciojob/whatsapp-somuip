package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
       if(userMobile.contains(mobile)){
           throw new Exception("User already exists");
       }
       userMobile.add(mobile);
       return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        int count = users.size();
        if(count == 2){
            Group group = new Group((users.get(1).getName()), count);
            group.setName((users.get(1).getName()));
            adminMap.put(group, users.get(0));
            groupUserMap.put(group,users);
            return group;
        }else{

            customGroupCount++;
            String name = "Group "+String.valueOf(customGroupCount);
            Group group = new Group(name, count);
            groupUserMap.put(group, users);
            adminMap.put(group, users.get(0));
            return group;
        }
    }

    public int createMessage(String content) {
        messageId++;
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        Message message = new Message(messageId, content, date);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        List<User> userList = groupUserMap.get(group);
        if(!userList.contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        senderMap.put(message, sender);
        List<Message> messageList = groupMessageMap.get(group);
        messageList.add(message);
        groupMessageMap.put(group, messageList);
        return messageList.size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(adminMap.get(group) != approver){
            throw new Exception("Approver does not have rights");
        }
        List<User> users = groupUserMap.get(group);
        if(!users.contains(user)){
            throw new Exception("User is not a participant");
        }
        adminMap.remove(group, approver);
        adminMap.put(group, user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        int ans = 0;
        for(Group group : groupUserMap.keySet()){
            List<User> userList = groupUserMap.get(group);
            if(!userList.contains(user)){
                throw new Exception("User not found");
            }
        }
        if(adminMap.containsValue(user)){
            throw new Exception("Cannot remove admin");
        }
        for(Group group : groupUserMap.keySet()){
            List<User> userList = groupUserMap.get(group);
            if(userList.contains(user)){
                userList.remove(user);
                ans += userList.size();
            }
        }
        for(Message message1 : senderMap.keySet()){
            User user1 = senderMap.get(user);
            if(user1 == user){
                senderMap.remove(message1, user1);
                ans += senderMap.size();
            }
        }

//        Message message = senderMap.containsValue(user);
//        for(Group group : groupMessageMap.keySet()){
//            List<Message> messageList = groupMessageMap.get(group);
//            for(Message message1 : messageList){
//                if(){
//                    messageList.remove(message1);
//                    ans += messageList.size();
//                }
//            }
//        }
        return ans;
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> messageList = new ArrayList<>();
        for(Group group : groupMessageMap.keySet()){
            List<Message> messages = groupMessageMap.get(group);
            for(Message message1 : messageList){
                Date date = message1.getTimestamp();
                int startDate = start.compareTo(date);
                int endDate = end.compareTo(date);
                if(startDate > 0 && endDate < 0){
                    messageList.add(message1);
                }
            }
        }
        if(messageList.size() < k){
            throw new Exception("K is greater than the number of messages");
        }

        Message message = messageList.get(messageList.size()-k-1);
        return message.getContent();
    }
}
