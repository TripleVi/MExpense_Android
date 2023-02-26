package com.example.mexpense.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mexpense.R;
import com.example.mexpense.databinding.ExpenseListItemBinding;
import com.example.mexpense.entities.ExpenseEntity;
import com.example.mexpense.utilities.DatetimeUtil;
import com.example.mexpense.utilities.Utilities;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.List;
import java.util.Objects;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.ExpenseViewHolder> {
    private final List<ExpenseEntity> expenses;
    private final OnClickListener onItemClickListener;
    private final long today = MaterialDatePicker.todayInUtcMilliseconds();
    private long current = 0;

    public ExpenseListAdapter(@NonNull List<ExpenseEntity> expenses, @NonNull OnClickListener listener) {
        this.expenses = Objects.requireNonNull(expenses);
        this.onItemClickListener = Objects.requireNonNull(listener);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_list_item, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bindData(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ExpenseListItemBinding binding;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ExpenseListItemBinding.bind(itemView);
        }

        public void bindData(@NonNull ExpenseEntity expense) {
            binding.imgIcon.setImageResource(expense.getCategory().getIconId());
            binding.tvCategory.setText(Utilities.capitalizeWords(expense.getCategory().getName()));
            binding.tvCost.setText(String.format("$%s", expense.getCost()));
            binding.getRoot().setOnClickListener(view -> onItemClickListener.onClick(expense.getId(), view));
            String date = DatetimeUtil.getDate(expense.getDate(), null);
            binding.tvDate.setText(date);

            if (expense.getComments().length() == 0) binding.tvComments.setVisibility(View.GONE);
            else binding.tvComments.setText(expense.getComments());

            long utcMs = expense.getDate();
            if(utcMs != current) {
                current = utcMs;
                binding.tvDate.setVisibility(View.VISIBLE);
                if(utcMs == today) {
                    binding.tvDate.setText("Today");
                }else {
                    binding.tvDate.setText(DatetimeUtil.getDate(utcMs, null));
                }
            }
        }
    }

    @FunctionalInterface
    public interface OnClickListener {
        void onClick(long id, View view);
    }
}
