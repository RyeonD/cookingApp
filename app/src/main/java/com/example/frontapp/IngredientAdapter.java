package com.example.frontapp;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private static String TAG = "IngredientAdapter";

    private ArrayList<Ingredient> mIngredientList = new ArrayList<>();
    private boolean isCheckbox;
    private String freshLevel;

    public IngredientAdapter(ArrayList<Ingredient> mIngredientList, boolean makeCheckbox) {
        this.mIngredientList = mIngredientList;
        this.isCheckbox = makeCheckbox;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(isCheckbox) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_row_chekbox, parent, false);
        }
        else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_row, parent, false);
        }

        IngredientViewHolder viewHolder = new IngredientViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        if(isCheckbox) {
            holder.checkBox.setText(mIngredientList.get(position).getName());

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Ingredient ingredient = mIngredientList.get(position);
                    if(ingredient.isCheck()) {
                        ingredient.setChecked(false);
                        Log.e(TAG, ingredient.getName() + " - " +ingredient.isCheck());
                    }
                    else {
                        ingredient.setChecked(true);
                        Log.e(TAG, ingredient.getName() + " - " +ingredient.isCheck());
                    }
                }
            });
        }
        else {
            holder.name.setText(mIngredientList.get(position).getName());
        }

        freshLevel = mIngredientList.get(position).getFreshLevel();
        holder.freshLevel.setText(freshLevel);

        // 신선도에 따른 색생 변경
        switch (freshLevel) {
            case "신선" : {
                holder.freshLevel.setBackgroundColor(Color.rgb(0,150,12));
                break;
            }
            case "양호" : {
                holder.freshLevel.setBackgroundColor(Color.rgb(255,224,71));
                break;
            }
            case "위험" : {
                holder.freshLevel.setBackgroundColor(Color.rgb(252, 127, 3));
                break;
            }
            case "만료" : {
                holder.freshLevel.setBackgroundColor(Color.RED);
                break;
            }
            default: {
                Log.e(TAG, "Input Color in FreshLevel error");
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != mIngredientList ? mIngredientList.size() : 0);
    }

    // item(재료별) 정의
    public class IngredientViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView freshLevel;
        protected CheckBox checkBox;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);

            if(isCheckbox) {
                this.checkBox = (CheckBox) itemView.findViewById(R.id.ingredient_checkbox);
                this.freshLevel = (TextView) itemView.findViewById(R.id.ingredient_fresh_level);
            }
            else {
                this.name = (TextView) itemView.findViewById(R.id.ingredient_name);
                this.freshLevel = (TextView) itemView.findViewById(R.id.ingredient_fresh_level);
            }
        }
    }
}
