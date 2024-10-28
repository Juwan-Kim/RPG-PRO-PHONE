package com.example.rpg_pro_phone.rpg_lagacy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpg_pro_phone.R;

import java.util.ArrayList;

public class RecipeDialogAdapter extends RecyclerView.Adapter<RecipeDialogAdapter.RecipeHolder> {
    private ArrayList<BeverRecipe> list;
    private Context context;
    private OnClickRecipeItem onClickRecipeItem;

    private String firstBever;
    private String secondBever;
    private String thirdBever;

    public RecipeDialogAdapter(Context context, ArrayList<BeverRecipe> list) {
        this.context = context;
        this.list = list;
    }

    public ArrayList<BeverRecipe> getList() {
        return list;
    }

    public void setList(ArrayList<BeverRecipe> list) {
        this.list = list;
    }

    public String getFirstBever() {
        return firstBever;
    }

    public void setFirstBever(String firstBever) {
        this.firstBever = firstBever;
    }

    public String getSecondBever() {
        return secondBever;
    }

    public void setSecondBever(String secondBever) {
        this.secondBever = secondBever;
    }

    public String getThirdBever() {
        return thirdBever;
    }

    public void setThirdBever(String thirdBever) {
        this.thirdBever = thirdBever;
    }

    public void setOnClickRecipeItem(OnClickRecipeItem onClickRecipeItem) {
        this.onClickRecipeItem = onClickRecipeItem;
    }

    @NonNull
    @Override
    public RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_list_item, parent, false);

        return new RecipeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeHolder holder, int position) {
        holder.bindItems(list.get(position).getRecipe());

        if(list.get(position).isAi()) {
            holder.pullBeverage.setText("AI 음료 방출");
        } else {
            holder.pullBeverage.setText("음료 방출");
        }

        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num1 = 0;
                int num2 = 0;
                int num3 = 0;

                String firstBever = getFirstBever();
                String secondBever = getSecondBever();
                String thirdBever = getThirdBever();

                for(int i = 0; i < list.get(position).getRecipeCount().length; i++) {
                    if(list.get(position).getRecipe()[i].equals(firstBever)) {
                        num1 = list.get(position).getRecipeCount()[i];
                    } else if(list.get(position).getRecipe()[i].equals(secondBever)) {
                        num2 = list.get(position).getRecipeCount()[i];
                    } else if(list.get(position).getRecipe()[i].equals(thirdBever)) {
                        num3 = list.get(position).getRecipeCount()[i];
                    }
                }

                onClickRecipeItem.onClickRecipeItem(num1, num2, num3);

            }
        });

        if(list.get(position).getRecipe().length == 2) {
            holder.inVisiblePlus2();
            holder.visiblePlus1();
        } else if(list.get(position).getRecipe().length == 3) {
            holder.visiblePlus1();
            holder.visiblePlus2();
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    protected class RecipeHolder extends RecyclerView.ViewHolder {
        private final TextView firstItem;
        private final TextView secondItem;
        private final TextView thirdItem;
        private final TextView plus1;
        private final TextView plus2;
        public final TextView pullBeverage;

        public RecipeHolder(@NonNull View itemView) {
            super(itemView);

            firstItem = itemView.findViewById(R.id.first_item);
            secondItem = itemView.findViewById(R.id.second_item);
            thirdItem = itemView.findViewById(R.id.third_item);
            plus1 = itemView.findViewById(R.id.plus1);
            plus2 = itemView.findViewById(R.id.plus2);
            pullBeverage = itemView.findViewById(R.id.out_button);

        }

        public void bindItems(String[] item) {
            for(int i = 0; i < item.length; i++) {
                if(i == 0) {
                    firstItem.setText(item[i]);
                } else if(i == 1) {
                    secondItem.setText(item[i]);
                } else if(i == 2) {
                    thirdItem.setText(item[i]);
                }
            }
        }

        public void visiblePlus1() {
            plus1.setVisibility(View.VISIBLE);
        }
        public void inVisiblePlus2() {
            plus2.setVisibility(View.INVISIBLE);
        }
        public void visiblePlus2() {
            plus2.setVisibility(View.VISIBLE);
        }
        public void setOnClickListener(View.OnClickListener onClickListener) {
            pullBeverage.setOnClickListener(onClickListener);
        }


    }

    public interface OnClickRecipeItem {
        void onClickRecipeItem(int num1, int num2, int num3);
    }
}
