package com.example.mexpense.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mexpense.R;
import com.example.mexpense.databinding.TripListItem2Binding;
import com.example.mexpense.databinding.TripListItemBinding;
import com.example.mexpense.entities.TripEntity;
import com.example.mexpense.utilities.DatetimeUtil;
import com.example.mexpense.utilities.Utilities;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.List;
import java.util.Objects;

public class TripListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public List<TripEntity> trips;
    private final OnClickListener onClickListener;
    private final long today = MaterialDatePicker.todayInUtcMilliseconds();
    private long current = 0;

    public TripListAdapter(@NonNull List<TripEntity> trips, @NonNull OnClickListener listener) {
        this.trips = Objects.requireNonNull(trips);
        this.onClickListener = Objects.requireNonNull(listener);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        int layoutId = viewType == 1 ? R.layout.trip_list_item : R.layout.trip_list_item2;
//        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
//        return viewType == 1 ? new TripViewHolder(itemView) : new TripViewHolder2(itemView);

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_list_item2, parent, false);
        return new TripViewHolder2(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        int viewType = getItemViewType(position);
//        if(viewType == 1) ((TripViewHolder) holder).bindData(filteredTrips.get(position));
//        else ((TripViewHolder2) holder).bindData(filteredTrips.get(position));

        ((TripViewHolder2) holder).bindData(trips.get(position));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

//    @Override
//    public int getItemViewType(int position) {
//        boolean flag = filteredTrips.get(position).getRequiredRiskAssessment();
//        return flag ? 1 : 0;
//    }

    public class TripViewHolder extends RecyclerView.ViewHolder {
        private final TripListItemBinding binding;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = TripListItemBinding.bind(itemView);
        }

        public void bindData(@NonNull TripEntity trip) {
            binding.imgIcon.setImageResource(trip.getCategory().getIconId());
            binding.tvCategory.setText(Utilities.capitalizeWords(trip.getCategory().getName()));
            binding.tvTotal.setText(String.format("$%s", trip.getTotal()));
            binding.tvDestination.setText(trip.getDestination());
            binding.getRoot().setOnClickListener(view -> onClickListener.onClick(trip.getId(), view));

            if (trip.getDescription().length() == 0) binding.tvDescription.setVisibility(View.GONE);
            else binding.tvDescription.setText(trip.getDescription());

            long utcMs = trip.getStartDate();
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

    public class TripViewHolder2 extends RecyclerView.ViewHolder {
        private final TripListItem2Binding binding;

        public TripViewHolder2(@NonNull View itemView) {
            super(itemView);
            binding = TripListItem2Binding.bind(itemView);
        }

        public void bindData(@NonNull TripEntity trip) {
            binding.imgIcon.setImageResource(trip.getCategory().getIconId());
            binding.tvCategory.setText(Utilities.capitalizeWords(trip.getCategory().getName()));
            binding.tvTotal.setText(String.format("$%s", trip.getTotal()));
            binding.tvDestination.setText(trip.getDestination());
            binding.getRoot().setOnClickListener(view -> onClickListener.onClick(trip.getId(), view));

            if (trip.getDescription().length() == 0) binding.tvDescription.setVisibility(View.GONE);
            else binding.tvDescription.setText(trip.getDescription());

            binding.tvDate.setVisibility(View.GONE);
            long utcMs = trip.getStartDate();
            if(utcMs != current) {
                System.out.println("current: " + current + " utc: " + utcMs);
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
