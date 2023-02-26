package com.example.mexpense.fragments.trip;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mexpense.R;
import com.example.mexpense.databinding.FragmentTripFormBinding;
import com.example.mexpense.entities.TripCategoryEntity;
import com.example.mexpense.entities.TripEntity;
import com.example.mexpense.repositories.TripCategoryRepository;
import com.example.mexpense.repositories.TripRepository;
import com.example.mexpense.utilities.AlertDialog;
import com.example.mexpense.utilities.Constants;
import com.example.mexpense.utilities.DatetimeUtil;
import com.example.mexpense.utilities.Utilities;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TripFormFragment extends Fragment {
    private TripFormViewModel mViewModel;
    private FragmentTripFormBinding binding;
    private final TripRepository tripRepo = new TripRepository();
    private final TripCategoryRepository cateRepo = new TripCategoryRepository();
    ArrayAdapter<TripCategoryEntity> adapter;
    private long tripId;
    private Long startDate, endDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(TripFormViewModel.class);
        binding = FragmentTripFormBinding.inflate(inflater, container, false);
        tripId = requireArguments().getLong("tripId");

        configureActionBar();

        mViewModel.tripLiveData.observe(getViewLifecycleOwner(), trip -> {
            if(trip == null) return;
            binding.ddTripCategories.setText(Utilities.capitalizeWords(trip.getCategory().getName()));
            binding.ddTripCategories.setTag(trip.getCategory());
            binding.etDestination.setText(trip.getDestination());
            startDate = trip.getStartDate();
            binding.etStartDatePicker.setText(DatetimeUtil.getDate(startDate, null));
            endDate = trip.getEndDate();
            binding.etEndDatePicker.setText(DatetimeUtil.getDate(endDate, null));
            binding.etDescription.setText(trip.getDescription());
            binding.swRiskAssessment.setChecked(trip.getRequiredRiskAssessment());
        });
        if(tripId != Constants.NEW_ID) {
            mViewModel.tripLiveData.setValue(tripRepo.getOneById(tripId));
        }

        binding.ddTripCategories.setShowSoftInputOnFocus(false);

        mViewModel.tripCategoryLiveData.observe(getViewLifecycleOwner(), categories -> {
            if(categories == null) return;
            adapter = new ArrayAdapter<TripCategoryEntity>(requireContext(), 0, categories) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TripCategoryEntity category = getItem(position);
                    if(convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.dropdown_item, parent, false);
                    }
                    ((TextView) convertView).setText(Utilities.capitalizeWords(category.getName()));
                    return convertView;
                }
            };
            binding.ddTripCategories.setAdapter(adapter);
        });
        mViewModel.tripCategoryLiveData.setValue(cateRepo.getAll());

        // parent The AdapterView where the click happened.
        // view The view within the AdapterView that was clicked (this will be a view provided by the adapter)
        // position The position of the view in the adapter
        // id The row id of the item that was clicked.
        binding.ddTripCategories.setOnItemClickListener((adapterView, view, i, l) -> {
            TripCategoryEntity category = (TripCategoryEntity) adapterView.getItemAtPosition(i);
            binding.ddTripCategories.setText(Utilities.capitalizeWord(category.getName()), false);
            binding.ddTripCategories.setTag(category);
        });

        createDatePicker(binding.etStartDatePicker, selection -> {
            startDate = selection;
            binding.etStartDatePicker.setText(DatetimeUtil.getDate(startDate, null));
        });
        createDatePicker(binding.etEndDatePicker, selection -> {
            endDate = selection;
            binding.etEndDatePicker.setText(DatetimeUtil.getDate(endDate, null));
        });

        binding.btnSave.setOnClickListener(view -> {
            if(!validate()) return;
            String category = binding.ddTripCategories.getText().toString();
            String dest = binding.etDestination.getText().toString();
            String sDate = binding.etStartDatePicker.getText().toString();
            String eDate = binding.etEndDatePicker.getText().toString();
            String desc = binding.etDescription.getText().toString();
            boolean requiredRiskAssessment = binding.swRiskAssessment.isChecked();
            String msg = String.format("Trip Category: %s\nDestination: %s\nStart Date: %s\nEnd Date: %s\nNote: %s\nRequired Risk Assessment: %s",
                    category, dest, sDate, eDate, desc, requiredRiskAssessment ? "Yes" : "No");
            String title; int type;
            if(tripId == Constants.NEW_ID) {
                title = "Add Confirmation";
                type = AlertDialog.INFO;
            }else {
                title = "Update Confirmation";
                type = AlertDialog.WARNING;
            }
            displayAlertDialog(title, type, msg, (var1, var2) -> handleSave());
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
        ab.setTitle(tripId == Constants.NEW_ID ? R.string.trip_form_add : R.string.trip_form_edit);
        ab.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if(tripId != Constants.NEW_ID) {
            inflater.inflate(R.menu.menu_edit_delete, menu);
            menu.findItem(R.id.action_edit).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Navigation.findNavController(requireView()).navigateUp();
                return true;
            case R.id.action_delete:
                String msg = "All of the relevant expenses will also be deleted. Are you sure?";
                displayAlertDialog("Delete Confirmation", AlertDialog.DANGER, msg, (var1, var2) -> handleDelete());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean validate() {
        EditText category = binding.ddTripCategories;
        EditText destination = binding.etDestination;

        boolean isValidated = true;
        String emptyError = "This field cannot be empty.";

        binding.tilDestination.setError(null);
        binding.menuTripCategories.setError(null);
        binding.tilStartDatePicker.setError(null);
        binding.tilEndDatePicker.setError(null);

        if(category.getText().length() == 0) {
            binding.menuTripCategories.setError(emptyError);
            isValidated = false;
        }
        if(destination.getText().length() == 0) {
            binding.tilDestination.setError(emptyError);
            isValidated = false;
        }
        if(startDate == null) {
            binding.tilStartDatePicker.setError(emptyError);
            isValidated = false;
        }
        if(endDate == null) {
            binding.tilEndDatePicker.setError(emptyError);
            isValidated = false;
        }else {
            if(startDate > endDate)  {
                binding.tilEndDatePicker.setError("Cannot less than start date.");
                isValidated = false;
            }
        }

//        destination.setOnFocusChangeListener((view, b) -> {
//            if(destination.getText().length() == 0) {
//                binding.tilDestination.setError("Destination is required.");
//                isValidated = false;
//            }else {
//                binding.tilDestination.setError(null);
//                isValidated = true;
//            }
//        });

        return isValidated;
    }

    private void handleDelete() {
        boolean isSuccess = tripRepo.deleteOneById(tripId);
        Navigation.findNavController(requireView()).navigateUp();
        Navigation.findNavController(requireView()).navigateUp();
        if (isSuccess) {
            makeToast("Deleted trip successfully!");
        } else {
            makeToast("Deleted trip failed!");
        }
    }

    private void handleSave() {
        String dest = binding.etDestination.getText().toString();
        String desc = binding.etDescription.getText().toString();
        boolean riskAssessment = binding.swRiskAssessment.isChecked();
        TripCategoryEntity category = (TripCategoryEntity) binding.ddTripCategories.getTag();
        TripEntity trip = new TripEntity(tripId, dest, startDate, endDate, desc, riskAssessment, category);
        String msg;
        if(tripId == Constants.NEW_ID) {
            msg = tripRepo.insertOne(trip) ? "Added trip successfully!" : "Added trip failed!";
        } else {
            msg = tripRepo.updateOne(trip) ? "Updated trip successfully!" : "Updated trip failed";
            System.out.println(trip);
        }
        Navigation.findNavController(requireView()).navigateUp();
        makeToast(msg);
    }

    private void createDatePicker(EditText editText, MaterialPickerOnPositiveButtonClickListener<Long> onPositiveButtonClickListener) {
        // datePicker method is used to create a Builder that allows for choosing a single date.
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        datePicker.addOnPositiveButtonClickListener(onPositiveButtonClickListener);
        // Khi tab vào text field, nó sẽ gain focus(focusable=true) (flashing cursor, etc.). Khi click on text field firstly, nó sẽ có focus and display keyboard(editable=true). Khi click on text field secondly, the click event is now actually fired.

        editText.setOnFocusChangeListener((view, b) -> {
            if(b) datePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
        });

        editText.setOnClickListener(view -> {
            datePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void displayAlertDialog(String title, int type, String msg, DialogInterface.OnClickListener listener) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setIcon(type)
                .setMessage(msg)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void makeToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}