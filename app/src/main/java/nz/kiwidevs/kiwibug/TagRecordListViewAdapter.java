package nz.kiwidevs.kiwibug;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Michael on 13.05.2017.
 */

public class TagRecordListViewAdapter extends ArrayAdapter<TagRecord> {

    private ArrayList<TagRecord> tags;
    private  ArrayList<Integer> selectedItems;
    private int layout;

    public TagRecordListViewAdapter(Context context, int layout, ArrayList<TagRecord> tagRecords) {
        super(context, layout, tagRecords);

        this.layout = layout;
        this.tags = tagRecords;
        this.selectedItems = new ArrayList<>();

    }

    public ArrayList<Integer> getSelectedItems(){
        return this.selectedItems;
    }

    public void toggleSelected(Integer position){
        if(selectedItems.contains(position)){
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
    }

    @Override
    public int getCount(){
        return tags.size();
    }

    @Override
    public TagRecord getItem(int position){
        return tags.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
        }

        TagRecord tag = tags.get(position);

        if(tag != null){
            TextView textViewTag = (TextView) convertView.findViewById(R.id.textViewTag);
            TextView textViewTagID = (TextView) convertView.findViewById(R.id.textViewUserID);
            TextView textViewTagTime = (TextView) convertView.findViewById(R.id.textViewTagTime);

           if(textViewTag != null){
                textViewTag.setText(tag.getAddress());
                //textViewTag.setText("ID:" + tag.getAddress());
           }
            if(textViewTagID != null){
                textViewTagID.setText(tag.getUsername());
            }

            if(textViewTagTime != null){
                textViewTagTime.setText(tag.getTagTime());
            }



        }


        return convertView;
    }

    public void refreshAdapter(ArrayList<TagRecord> tags){

        this.notifyDataSetChanged();



    }





}
