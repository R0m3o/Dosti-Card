package com.zeezo.dosticard;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by masho on 27-Feb-17.
 */
public class CustomerAdapter extends ArrayAdapter {

    private List customerData = new ArrayList<>();

    public CustomerAdapter(Context context, int resource) {
        super(context, resource);
    }

    static class DataHandler {
        TextView name;
        TextView balance;
        TextView points;
    }

    @Override
    public void add(Object object) {
        super.add(object);
        customerData.add(object);
    }

    @Override
    public int getCount() {
        return this.customerData.size();
    }

    @Override
    public Object getItem(int position) {
        return this.customerData.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        DataHandler handler;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.customer_listview, parent, false);
            handler = new DataHandler();
            handler.name = (TextView) rowView.findViewById(R.id.idCViewName);
            handler.balance = (TextView) rowView.findViewById(R.id.idCViewBalance);
            handler.points = (TextView) rowView.findViewById(R.id.idCViewPoints);
            rowView.setTag(handler);
        } else {
            handler = (DataHandler) rowView.getTag();
        }
        CustomerData dataProvider = (CustomerData) this.getItem(position);
        handler.name.setText(dataProvider.getName());
        handler.balance.setText(String.valueOf(dataProvider.getBalance()));
        handler.points.setText(String.valueOf(dataProvider.getPoints()));

        return rowView;
    }
}
