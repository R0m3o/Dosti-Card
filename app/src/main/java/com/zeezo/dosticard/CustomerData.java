package com.zeezo.dosticard;

/**
 * Created by masho on 27-Feb-17.
 */
public class CustomerData {

    private String name;
    private int points, balance;

    public CustomerData(String name, int points, int balance) {
        this.name = name;
        //this.customer_id = customer_id;
        this.balance = balance;
        this.points = points;
    }

    /*public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }*/

    public int getBalance() {
        return balance;
    }


    public int getPoints() {
        return points;
    }


    public String getName() {
        return name;
    }

}
