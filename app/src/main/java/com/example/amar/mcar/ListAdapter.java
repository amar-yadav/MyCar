package com.example.amar.mcar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Amar on 11/5/16.
 */

public class ListAdapter extends ArrayAdapter<ListItem> {

    Context mContext;
    ArrayList<ListItem> listItems;

    public ListAdapter(Context context, ArrayList<ListItem> listItems) {
        super(context, 0, listItems);
        this.mContext = context;
        this.listItems = listItems;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(mContext, R.layout.list_item, null);
            ((TextView) convertView.findViewById(R.id.textView)).setText(listItems.get(position).name);
        }

        ((TextView) convertView.findViewById(R.id.textView)).setText(listItems.get(position).name);

        return convertView;
    }
}
