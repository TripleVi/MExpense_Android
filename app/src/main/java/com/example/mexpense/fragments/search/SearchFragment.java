package com.example.mexpense.fragments.search;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;

import com.example.mexpense.R;
import com.example.mexpense.adapter.TripListSearchAdapter;
import com.example.mexpense.databinding.FragmentSearchBinding;
import com.example.mexpense.databinding.TripFilterBinding;
import com.example.mexpense.repositories.TripRepository;
import com.example.mexpense.utilities.DatetimeUtil;
import com.example.mexpense.utilities.Utilities;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SearchFragment extends Fragment {

    private SearchViewModel mViewModel;
    private FragmentSearchBinding binding;
    private TripListSearchAdapter adapter;
    private Long selectedStartDate, selectedEndDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        TripRepository tripRepo = new TripRepository();

        configureActionBar();

        RecyclerView rv = binding.rvList;
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        mViewModel.liveData.observe(getViewLifecycleOwner(), trips -> {
            if(trips.isEmpty()) {
                binding.clEmpty.setVisibility(View.VISIBLE);
                binding.rvList.setVisibility(View.GONE);
                return;
            }
            binding.clEmpty.setVisibility(View.GONE);
            binding.rvList.setVisibility(View.VISIBLE);
            adapter = new TripListSearchAdapter(trips, (id, view) -> {
                Bundle bundle = new Bundle();
                bundle.putLong("tripId", id);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.expenseMainFragment, bundle);
            });
            rv.setAdapter(adapter);
        });
        mViewModel.liveData.setValue(tripRepo.getAll("", null));

        SearchView searchBtn = binding.btnSearch;
        searchBtn.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                mViewModel.liveData.setValue(tripRepo.searchByNameOrDestination(s));
                return true;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        Utilities.hideKeyboard(requireView(), requireActivity());
        super.onDestroyView();
    }

    private void configureActionBar() {
        AppCompatActivity app = (AppCompatActivity) requireActivity();
        ActionBar ab = app.getSupportActionBar();
        ab.setTitle(R.string.trip_search);
        ab.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Navigation.findNavController(requireView()).navigateUp();
                return true;
            case R.id.action_search_filter:
                if(mViewModel.liveData.getValue() != null) {
                    Utilities.hideKeyboard(requireView(), requireActivity());
                    handleFilter();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleFilter() {
        TripFilterBinding filterBinding = TripFilterBinding.inflate(LayoutInflater.from(getContext()));
        EditText startDate = filterBinding.etStartDatePicker;
        EditText endDate = filterBinding.etEndDatePicker;
        if(selectedStartDate != null) {
            startDate.setText(DatetimeUtil.getDate(selectedStartDate, null));
            endDate.setText(DatetimeUtil.getDate(selectedEndDate, null));
        }
        createDatePicker(startDate, selection -> {
            startDate.setText(DatetimeUtil.getDate(selection, null));
            selectedStartDate = selection;
        });
        createDatePicker(endDate, selection -> {
            endDate.setText(DatetimeUtil.getDate(selection, null));
            selectedEndDate = selection;
        });
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filter")
                .setView(filterBinding.getRoot())
                .setNeutralButton("Cancel", null)
                .setPositiveButton("Apply", null)
                .setNegativeButton("Reset", (dialogInterface, i) -> {
                    selectedStartDate = null;
                    selectedEndDate = null;
                    adapter.resetFilter();
                })
                .create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                boolean isValidated = true;
                String emptyError = "This field cannot be empty.";
                if(selectedStartDate == null) {
                    filterBinding.tilStartDatePicker.setError(emptyError);
                    isValidated = false;
                }
                if(selectedEndDate == null) {
                    filterBinding.tilEndDatePicker.setError(emptyError);
                    isValidated = false;
                }else {
                    if(selectedStartDate > selectedEndDate)  {
                        filterBinding.tilEndDatePicker.setError("Cannot less than start date.");
                        isValidated = false;
                    }
                }
                if(isValidated) {
                    adapter.filterByDate(selectedStartDate, selectedEndDate);
                    dialogInterface.dismiss();
                }
            });
        });
        dialog.show();
    }

    private void createDatePicker(EditText editText, MaterialPickerOnPositiveButtonClickListener<Long> onPositiveButtonClickListener) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        datePicker.addOnPositiveButtonClickListener(onPositiveButtonClickListener);

        editText.setOnFocusChangeListener((view, b) -> {
            if(b) datePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
        });

        editText.setOnClickListener(view -> {
            datePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
        });
    }

}