package com.example.mexpense.fragments.trip;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.mexpense.R;
import com.example.mexpense.adapter.TripListAdapter;
import com.example.mexpense.databinding.FragmentTripMainBinding;
import com.example.mexpense.repositories.ExpenseRepository;
import com.example.mexpense.repositories.TripRepository;
import com.example.mexpense.utilities.AlertDialog;
import com.example.mexpense.utilities.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.json.JSONException;
import java.util.Map;

public class TripMainFragment extends Fragment {

    private TripMainViewModel mViewModel;
    private FragmentTripMainBinding binding;
    private TripListAdapter adapter;
    private final TripRepository tripRepo = new TripRepository();
    private final String TAG = "TripMainFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        configureActionBar();

        mViewModel = new ViewModelProvider(this).get(TripMainViewModel.class);
        binding = FragmentTripMainBinding.inflate(inflater, container, false);

        TripListAdapter.OnClickListener onClickListener = (id, view) -> {
            Bundle bundle = new Bundle();
            bundle.putLong("tripId", id);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.expenseMainFragment, bundle);
        };

        RecyclerView rv = binding.rvTripList;
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        mViewModel.liveData.observe(getViewLifecycleOwner(), trips -> {
            if(trips.isEmpty()) return;
            requireActivity().invalidateMenu();
            binding.clContents.setVisibility(View.VISIBLE);
            binding.clEmpty.setVisibility(View.GONE);
            adapter = new TripListAdapter(trips, onClickListener);
            rv.setAdapter(adapter);
        });
        mViewModel.liveData.setValue(tripRepo.getAll("", null));


        binding.btnAdd.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putLong("tripId", Constants.NEW_ID);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.tripFormFragment, bundle);
        });
        return binding.getRoot();
    }

    private void configureActionBar() {
        AppCompatActivity app = (AppCompatActivity) requireActivity();
        ActionBar ab = app.getSupportActionBar();
        ab.setTitle(R.string.trip_main);
        ab.setDisplayHomeAsUpEnabled(false);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_trip_search:
                Navigation.findNavController(binding.getRoot()).navigate(R.id.searchFragment);
                return true;
            case R.id.action_trip_delete:
                handleDeleteAllTrips();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleDeleteAllTrips() {
        displayAlertDialog(
                "Delete Confirmation",
                AlertDialog.DANGER,
                "All of the trips and relevant expenses will be deleted. Are you sure?",
                (var1, var2) -> {
                    boolean isSuccess = tripRepo.deleteAll();
                    if(isSuccess) {
                        NavController navController = Navigation.findNavController(binding.getRoot());
                        navController.popBackStack();
                        navController.navigate(R.id.tripMainFragment);
                    }
                    makeToast(isSuccess ? "Deleted trips successfully!" : "Deleted trips failed!");
                }
        );
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        if(mViewModel.liveData.getValue().isEmpty()) {
            menu.findItem(R.id.action_trip_delete).setVisible(false);
            menu.findItem(R.id.action_trip_search).setVisible(false);
            menu.findItem(R.id.action_trip_sync).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_trip_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void displayAlertDialog(String title, int type, String msg, DialogInterface.OnClickListener listener) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setIcon(type)
                .setMessage(msg)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", (dialogInterface, i) -> {})
                .show();
    }

    private void makeToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}