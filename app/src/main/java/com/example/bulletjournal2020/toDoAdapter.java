package com.example.bulletjournal2020;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.TextView;

//Responsible for displaying data from model into row in recycler view
public class toDoAdapter extends RecyclerView.Adapter<toDoAdapter.ViewHolder>{

    public interface OnLongClickListener{

        void onItemLongClicked(int position);

    }

    public interface OnClickListener{

        void onItemClicked(int position);

    }

    List<String> items;

    OnLongClickListener longClickListener;

    OnClickListener clickListener;

    public toDoAdapter(List<String> items, OnLongClickListener longClickListener, OnClickListener clickListener) {

        this.items = items;

        this.longClickListener= longClickListener;

        this.clickListener = clickListener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //use layout inflater to inflate view
        View todoView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        //wrap inside a View Holder and return it
        return new ViewHolder(todoView);
    }

    //responsible for binding data to particular view holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //grab item at position
        String item = items.get(position);

        //bind item in specified view holder
        holder.bind(item);

    }

    //tells RV how many items are in list
    @Override
    public int getItemCount() {


        return items.size();
    }

    //Container to provide easy access to views that represent each row of list
    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvItem;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            tvItem = itemView.findViewById(android.R.id.text1);

        }

        //update  view inside view holder with data
        public void bind(String item) {

            tvItem.setText(item);
            tvItem.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v){

                    clickListener.onItemClicked(getAdapterPosition());

                }
            });


            tvItem.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View v){

                    //notify listener which position was long pressed
                    longClickListener.onItemLongClicked(getAdapterPosition());
                    return true;

                }
            });
        }
    }
}
