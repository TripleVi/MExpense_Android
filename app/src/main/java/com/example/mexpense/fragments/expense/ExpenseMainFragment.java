package com.example.mexpense.fragments.expense;

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

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mexpense.R;
import com.example.mexpense.adapter.ExpenseListAdapter;
import com.example.mexpense.databinding.FragmentExpenseMainBinding;
import com.example.mexpense.entities.ExpenseEntity;
import com.example.mexpense.entities.TripEntity;
import com.example.mexpense.repositories.ExpenseRepository;
import com.example.mexpense.repositories.TripRepository;
import com.example.mexpense.utilities.AlertDialog;
import com.example.mexpense.utilities.Constants;
import com.example.mexpense.utilities.DatetimeUtil;
import com.example.mexpense.utilities.Utilities;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ExpenseMainFragment extends Fragment {

    private ExpenseMainViewModel mViewModel;
    private FragmentExpenseMainBinding binding;
    private ExpenseListAdapter adapter;
    private long tripId;
    private final TripRepository tripRepo = new TripRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        configureActionBar();

        mViewModel = new ViewModelProvider(this).get(ExpenseMainViewModel.class);
        binding = FragmentExpenseMainBinding.inflate(inflater, container, false);

        tripId = requireArguments().getLong("tripId");

        RecyclerView rv = binding.rvExpenseList;
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        mViewModel.tripLiveData.observe(getViewLifecycleOwner(), trip -> {
            if(trip == null) return;
            binding.tvTripCategory.setText(Utilities.capitalizeWords(trip.getCategory().getName()));
            binding.tvTripTotal.setText(String.format("%s", trip.getTotal()));
            String date = DatetimeUtil.getDate(trip.getStartDate(), null);
            binding.tvTripStartDate.setText(date);
            date = DatetimeUtil.getDate(trip.getEndDate(), null);
            binding.tvTripEndDate.setText(date);
            binding.tvTripRiskAssessment.setText(trip.getRequiredRiskAssessment() ? "Risk Assessment Required" : "Risk Assessment Not Required");

            requireActivity().invalidateMenu();
            List<ExpenseEntity> expenses = trip.getExpenses();
            if(expenses.isEmpty()) return;
            binding.clContents.setVisibility(View.VISIBLE);
            binding.clEmpty.setVisibility(View.GONE);
            binding.tvExpenseNumber.setText(String.format("%s %s", expenses.size(), expenses.size() == 1 ? "item" : "items"));
            adapter = new ExpenseListAdapter(expenses, (id, view) -> {
                Bundle bundle = new Bundle();
                bundle.putLong("expenseId", id);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.expenseDetailsFragment, bundle);
            });
            rv.setAdapter(adapter);
        });
        if(tripId != Constants.NEW_ID) {
            mViewModel.tripLiveData.setValue(tripRepo.getOneById(tripId));
        }

        binding.btnAdd.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putLong("expenseId", Constants.NEW_ID);
            bundle.putLong("tripId", tripId);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.expenseFormFragment, bundle);
        });

        return binding.getRoot();
    }

    private void configureActionBar() {
        AppCompatActivity app = (AppCompatActivity) requireActivity();
        ActionBar ab = app.getSupportActionBar();
        ab.setTitle(R.string.expense_main);
        ab.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        boolean visible = mViewModel.tripLiveData.getValue().getExpenses().isEmpty();
        menu.findItem(R.id.action_delete).setVisible(!visible);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_delete, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Navigation.findNavController(requireView()).navigateUp();
                return true;
            case R.id.action_edit:
                Bundle bundle = new Bundle();
                bundle.putLong("tripId", tripId);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.tripFormFragment, bundle);
                return true;
            case R.id.action_delete:
                String msg = "All of the expenses will be deleted. Are you sure?";
                displayAlertDialog("Delete Confirmation", AlertDialog.DANGER, msg, (var1, var2) -> handleDelete());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleDelete() {
        TripEntity trip = mViewModel.tripLiveData.getValue();
        if(trip == null) return;
        boolean isSuccess = new ExpenseRepository().deleteAll(trip);
        if(isSuccess) {
            NavController navController = Navigation.findNavController(requireView());
            navController.popBackStack();
            Bundle bundle = new Bundle();
            bundle.putLong("tripId", tripId);
            navController.navigate(R.id.expenseMainFragment, bundle);
        }
        makeToast(isSuccess ? "Deleted expenses successfully!" : "Deleted expenses failed!");
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