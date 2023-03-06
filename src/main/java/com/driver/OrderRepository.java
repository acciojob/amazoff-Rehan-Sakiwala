package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class OrderRepository {

    private HashMap<String,Order> orderDb;
    private HashMap<String,DeliveryPartner> deliveryPartnerDb;

    private HashMap<String, List<String>> partnerPairDb;

    private HashMap<String,String> orderPairDb;

    public OrderRepository() {
        orderDb = new HashMap<String,Order>();
        deliveryPartnerDb = new HashMap<String,DeliveryPartner>();
        partnerPairDb = new HashMap<String,List<String>>();
        orderPairDb =  new HashMap<String, String>();

    }

    public void addOrder(Order order) {
        orderDb.put(order.getId(),order);
    }

    public void addPartner(String partnerId) {
        DeliveryPartner dp = new DeliveryPartner(partnerId);
        deliveryPartnerDb.put(partnerId,dp);
    }

    public void addOrderPartnerPair(String orderId, String partnerId) {
        if (orderDb.containsKey(orderId) && deliveryPartnerDb.containsKey(partnerId)) {

            orderPairDb.put(orderId, partnerId);
            //need to increase the order count 1 for the partner
            int orderHave = deliveryPartnerDb.get(partnerId).getNumberOfOrders();
            orderHave++;
            deliveryPartnerDb.get(partnerId).setNumberOfOrders(orderHave);
            //we also need to make a pair with the partner db and list of the orders
            if (partnerPairDb.containsKey(partnerId)) {
                //simply add the new order to the list in partner
                partnerPairDb.get(partnerId).add(orderId);
            } else {
                List<String> orderList = new ArrayList<>();
                orderList.add(orderId);
                partnerPairDb.put(partnerId, orderList);
            }
        }
    }

    public Order getOrderById(String orderId) {
        if(orderDb.containsKey(orderId))
            return orderDb.get(orderId);
        return new Order();
    }

    public DeliveryPartner getPartnerById(String partnerId) {
        if(deliveryPartnerDb.containsKey(partnerId))
            return deliveryPartnerDb.get(partnerId);
        return new DeliveryPartner();
    }

    public Integer getOrderCountByPartnerId(String partnerId) {
        return deliveryPartnerDb.get(partnerId).getNumberOfOrders();
    }

    public List<String> getOrdersByPartnerId(String partnerId) {
        if(!partnerPairDb.containsKey(partnerId)){
            return new ArrayList<String>();
        }
        return partnerPairDb.get(partnerId);
    }

    public List<String> getAllOrders() {
        List<String> orders = new ArrayList<String>();
        for(String i : orderDb.keySet()){
            orders.add(i);
        }
        return orders;
    }

    public Integer getCountOfUnassignedOrders() {
        return (orderDb.size()-orderPairDb.size());
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId) {
        if(partnerPairDb.containsKey(partnerId)) {

            String[] arr = time.split(":");

            int HH = Integer.parseInt(arr[0]);
            int MM = Integer.parseInt(arr[1]);

            int givenTime = (HH * 60) + MM;
            int count = 0;
            List<String> l1 = partnerPairDb.get(partnerId);
            for (String h : l1) {
                int temp = orderDb.get(h).getDeliveryTime();
                if (temp > givenTime)
                    count++;

            }

            return count;
        }
        return -1;
    }

    public void deleteOrderById(String orderId) {
        String partnerAssigned = orderPairDb.get(orderId); //getting obj
        //delete order id from partner assigned list
        List<String> orderHave = deleteOrder(partnerAssigned,orderId);
        partnerPairDb.put(partnerAssigned,orderHave);
        //decrease a order from the assigned order number
        int numberOfOrder = deliveryPartnerDb.get(partnerAssigned).getNumberOfOrders();
        numberOfOrder--;
        deliveryPartnerDb.get(partnerAssigned).setNumberOfOrders(numberOfOrder);
        orderPairDb.remove(orderId);
        orderDb.remove(orderId);
    }
    public List<String> deleteOrder(String partnerAssigned, String orderId){
        List<String> order = new ArrayList<>();
        for(String h : partnerPairDb.get(partnerAssigned)){
            if(!h.equals(orderId)){
                order.add(h);
            }
        }
        return order;
    }

    public void deletePartnerById(String partnerId) {
        if(deliveryPartnerDb.containsKey(partnerId)) {
            deliveryPartnerDb.remove(partnerId);
            List<String> l = partnerPairDb.get(partnerId);
            for(String ord :l){
                if(orderPairDb.get(ord).equals(partnerId))
                    orderPairDb.remove(ord);//remove that order from order and partner pair
            }
            partnerPairDb.remove(partnerId);
        }
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId) {
        if(deliveryPartnerDb.containsKey(partnerId)) {

            List<String> orders = partnerPairDb.get(partnerId);
            int time = 0;
            int max = 0;
            for (String you : orders) {
                time = orderDb.get(you).getDeliveryTime();
                max = Math.max(max,time);
            }
            //now we have last time in max variable
            String HH = String.valueOf(max / 60);
            String MM = String.valueOf(max % 60);
            if (HH.length() == 1) {
                HH = "0" + HH;
            }
            if (MM.length() == 1) {
                MM = "0" + MM;
            }
            return (HH + ":" + MM);
        }
        return "";
    }
}