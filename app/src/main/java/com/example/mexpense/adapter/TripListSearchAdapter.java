package com.example.mexpense.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mexpense.R;
import com.example.mexpense.databinding.TripSearchListItemBinding;
import com.example.mexpense.entities.TripEntity;
import com.example.mexpense.utilities.DatetimeUtil;
import com.example.mexpense.utilities.Utilities;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TripListSearchAdapter extends RecyclerView.Adapter<TripListSearchAdapter.TripViewHolder> {
    public List<TripEntity> trips;
    private List<TripEntity> filteredTrips;
    private final OnClickListener onClickListener;

    public TripListSearchAdapter(@NonNull List<TripEntity> trips, @NonNull OnClickListener listener) {
        this.trips = Objects.requireNonNull(trips);
        this.onClickListener = Objects.requireNonNull(listener);
        filteredTrips = trips;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_search_list_item, parent, false);
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        holder.bindData(filteredTrips.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredTrips.size();
    }

    public void resetFilter() {
        filteredTrips = trips;
        notifyDataSetChanged();
    }

    public void filterByDate(long startDate, long endDate) {
        filteredTrips = trips
                .stream()
                .filter(trip -> trip.getStartDate() >= startDate && trip.getStartDate() <= endDate)
                .collect(Collectors.toList());
        notifyDataSetChanged();
    }

    public class TripViewHolder extends RecyclerView.ViewHolder {
        private final TripSearchListItemBinding binding;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = TripSearchListItemBinding.bind(itemView);
        }

        public void bindData(@NonNull TripEntity trip) {
            binding.imgIcon.setImageResource(trip.getCategory().getIconId());
            binding.tvCategory.setText(Utilities.capitalizeWords(trip.getCategory().getName()));
            binding.tvDestination.setText(trip.getDestination());
            binding.tvStartDate.setText(DatetimeUtil.getDate(trip.getStartDate(), null));
            binding.getRoot().setOnClickListener(view -> onClickListener.onClick(trip.getId(), view));
        }
    }

    @FunctionalInterface
    public interface OnClickListener {
        void onClick(long id, View view);
    }
}
