package com.driver;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {

    private HashMap<String,Order> orderMap=new HashMap<>();
    private HashMap<String,DeliveryPartner> deliveryPartnerMap=new HashMap<>();
    private HashMap<String, List<String>> orderPartnerMap=new HashMap<>();
    private HashMap<String,String> partnerPairDb=new HashMap<>();

    public void addOrder(Order order){
        orderMap.put(order.getId(),order);
    }

    public void addPartner(String partnerId){
        DeliveryPartner dp=new DeliveryPartner(partnerId);
        deliveryPartnerMap.put(partnerId,dp);
    }

    public void addOrderPartnerPair(String orderId,String partnerId){
        partnerPairDb.put(orderId,partnerId);
        if(orderMap.containsKey(orderId) && deliveryPartnerMap.containsKey(partnerId)){
            int orderCount=deliveryPartnerMap.get(partnerId).getNumberOfOrders();
            orderCount++;
            deliveryPartnerMap.get(partnerId).setNumberOfOrders(orderCount);

            if(orderPartnerMap.containsKey(partnerId)){
                orderPartnerMap.get(partnerId).add(orderId);
            }
            else{
                List<String> orderList=new ArrayList<>();
                orderList.add(orderId);
                orderPartnerMap.put(partnerId,orderList);
            }
        }
    }

    public Order getOrderById(String orderId) {
        return orderMap.get(orderId);
    }

    public DeliveryPartner getPartnerById(String partnerId){
        if (deliveryPartnerMap.containsKey(partnerId)){
            return deliveryPartnerMap.get(partnerId);
        }
        return new DeliveryPartner();
    }

    public int getOrderCountByPartnerId(String partnerId){
            return deliveryPartnerMap.get(partnerId).getNumberOfOrders();
    }

    public List<String> getOrdersByPartnerId(String partnerId){
        return orderPartnerMap.get(partnerId);
    }

    public List<String> getAllOrders(){
        List<String> orders=new ArrayList<>();
        for(String x : orderMap.keySet()){
            orders.add(x);
        }
        return orders;
    }

    public int getCountOfUnassignedOrders(){
        return (orderMap.size()-partnerPairDb.size());
    }

    public int getOrdersLeftAfterGivenTimeByPartnerId(String time,String partnerId){
        if(deliveryPartnerMap.containsKey(partnerId)){
            String[] arr=time.split(":");
            int HH=Integer.parseInt(arr[0]);
            int MM=Integer.parseInt(arr[1]);

            int givenTime=(HH*60)+MM;
            int count =0;
            List<String> l1=orderPartnerMap.get(partnerId);

            for(String x:l1){
                if(orderMap.get(x).getDeliveryTime() < givenTime){
                    count++;
                }
            }
            return count;
        }
        return -1;
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId){
        if(deliveryPartnerMap.containsKey(partnerId)){
            List<String> orders = orderPartnerMap.get(partnerId);
            int time=0;
            int max=0;
            for(String x:orders){
                time=orderMap.get(x).getDeliveryTime();
                max=Math.max(max,time);
            }

            String HH=String.valueOf(max/60);
            String MM=String.valueOf(max%60);

            if(HH.length()==1){
                HH="0"+HH;
            }
            if(MM.length()==1){
                MM="0"+MM;
            }
            return (HH + ":" +MM);
        }
        return "";
    }

    public void deletePartnerById(String partnerId){
        if(deliveryPartnerMap.containsKey(partnerId)){
            List<String> l =orderPartnerMap.get(partnerId);
            deliveryPartnerMap.remove(partnerId);

            for(String ord: l){
                partnerPairDb.remove(ord);
            }

            orderPartnerMap.remove(partnerId);
        }
    }

    public void deleteOrderById(String orderId){
        if(orderMap.containsKey(orderId)){
            String partner=partnerPairDb.get(orderId);

            //Removing from OrderDeliveryPair db
            List<String> l=orderPartnerMap.get(partner);
            l.remove(orderId);
            orderPartnerMap.put(partner,l);

            deliveryPartnerMap.get(partner).setNumberOfOrders(deliveryPartnerMap.get(partner).getNumberOfOrders()-1);

            partnerPairDb.remove(orderId);
            orderMap.remove(orderId);
        }
    }


}
