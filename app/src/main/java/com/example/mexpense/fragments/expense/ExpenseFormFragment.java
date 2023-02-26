package com.example.mexpense.fragments.expense;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mexpense.R;
import com.example.mexpense.databinding.FragmentExpenseFormBinding;
import com.example.mexpense.entities.ExpenseCategoryEntity;
import com.example.mexpense.entities.ExpenseEntity;
import com.example.mexpense.entities.TripEntity;
import com.example.mexpense.repositories.ExpenseCategoryRepository;
import com.example.mexpense.repositories.ExpenseRepository;
import com.example.mexpense.repositories.TripRepository;
import com.example.mexpense.utilities.AlertDialog;
import com.example.mexpense.utilities.Constants;
import com.example.mexpense.utilities.DatetimeUtil;
import com.example.mexpense.utilities.Utilities;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

public class ExpenseFormFragment extends Fragment {
    private ExpenseFormViewModel mViewModel;
    private FragmentExpenseFormBinding binding;
    ArrayAdapter<ExpenseCategoryEntity> adapter;
    private final ExpenseRepository expenseRepo = new ExpenseRepository();
    private final TripRepository tripRepo = new TripRepository();
    private final ExpenseCategoryRepository categoryRepo = new ExpenseCategoryRepository();
    private long expenseId, tripId;
    private ExpenseCategoryEntity category;
    private String imgName, txtCost, txtComment;
    private boolean isSaved = false;
    private final String TAG = "ExpenseFormFragment";
    Long date;

    private FusedLocationProviderClient locationClient;
    private boolean isGranted = false;

    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ExpenseFormViewModel.class);
        expenseId = requireArguments().getLong("expenseId");
        if(expenseId == Constants.NEW_ID) {
            tripId = requireArguments().getLong("tripId");
        }
        if(expenseId != Constants.NEW_ID) {
            mViewModel.expenseLiveData.setValue(expenseRepo.getOneById(expenseId));
        }else mViewModel.tripLiveData.setValue(tripRepo.getOneById(tripId));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        requestPermission();
        configureActionBar();
        binding = FragmentExpenseFormBinding.inflate(inflater, container, false);

        if(!isSaved) {
            mViewModel.expenseLiveData.observe(getViewLifecycleOwner(), expense -> {
                if(expense == null) return;
                category = expense.getCategory();
                date = expense.getDate();
                imgName = expense.getImgName();
                txtCost = String.valueOf(expense.getCost());
                txtComment = expense.getComments();
                binding.etCost.setText(txtCost);
                binding.etComment.setText(txtComment);
                binding.ddExpenseCategories.setText(Utilities.capitalizeWords(category.getName()));
                binding.etDatePicker.setText(DatetimeUtil.getDate(date, null));
                if(imgName != null) {
                    displayExpenseImage();
                }
            });
        }
        if(imgName != null) {
            displayExpenseImage();
        }

        binding.ddExpenseCategories.setShowSoftInputOnFocus(false);

        mViewModel.expenseCategoryLiveData.observe(getViewLifecycleOwner(), categories -> {
            adapter = new ArrayAdapter<ExpenseCategoryEntity>(requireContext(), 0, categories) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    ExpenseCategoryEntity category = getItem(position);
                    if(convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.dropdown_item, parent, false);
                    }
                    ((TextView) convertView).setText(Utilities.capitalizeWords(category.getName()));
                    return convertView;
                }
            };
            binding.ddExpenseCategories.setAdapter(adapter);
        });
        mViewModel.expenseCategoryLiveData.setValue(categoryRepo.getAll());

        // parent The AdapterView where the click happened.
        // view The view within the AdapterView that was clicked (this will be a view provided by the adapter)
        // position The position of the view in the adapter
        // id The row id of the item that was clicked.
        binding.ddExpenseCategories.setOnItemClickListener((adapterView, view, i, l) -> {
            category = (ExpenseCategoryEntity) adapterView.getItemAtPosition(i);
            binding.ddExpenseCategories.setText(Utilities.capitalizeWords(category.getName()), false);
        });

        createDatePicker(binding.etDatePicker);

        binding.btnSave.setOnClickListener(view -> {
            if(!validate()) return;
            String txtDate = binding.etDatePicker.getText().toString();
            String txtCost = binding.etCost.getText().toString();
            String txtComment = binding.etComment.getText().toString();
            String msg = String.format("Expense category: %s\nDate: %s\nCost: %s\nNotes: %s", category.getName(), txtDate, txtCost, txtComment);
            String title;
            int type;
            if(expenseId == Constants.NEW_ID) {
                title = "Add Confirmation";
                type = AlertDialog.INFO;
            }else {
                title = "Update Confirmation";
                type = AlertDialog.WARNING;
            }
            displayAlertDialog(title, type, msg, (var1, var2) -> handleSave());
        });

        binding.btnImageAdd.setOnClickListener(view -> Navigation.findNavController(requireView()).navigate(R.id.cameraFragment));

        binding.btnImageDelete.setOnClickListener(view -> {
            deleteImageFile();
            if(imgName == null) {
                binding.btnImageDelete.setEnabled(false);
                binding.btnImageAdd.setText("Add Image");
                binding.imgExpense.setImageBitmap(null);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Navigation.findNavController(requireView()).getCurrentBackStackEntry().getSavedStateHandle().getLiveData("img_name")
                .observe(getViewLifecycleOwner(), result -> {
                    if(result == null) return;
                    deleteImageFile();
                    imgName = result.toString();
                    displayExpenseImage();
                }
        );
    }

    @Override
    public void onDestroyView() {
        txtCost = binding.etCost.getText().toString();
        txtComment = binding.etComment.getText().toString();
        isSaved = true;
        Utilities.hideKeyboard(requireView(), requireActivity());
        super.onDestroyView();
    }

    private Bitmap getBitmap() {
        try {
            File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(storageDir.getPath(), imgName);
            return BitmapFactory.decodeFile(file.getPath());
        }catch (Exception e) {
            Log.e(TAG, "Error getting bitmap from image file => " + e);
            return null;
        }
    }
    
    private void displayExpenseImage() {
        Bitmap bitmap = getBitmap();
        binding.imgExpense.setImageBitmap(bitmap);
        binding.btnImageAdd.setText("Change Image");
        binding.btnImageDelete.setEnabled(true);
    }

    private void deleteImageFile() {
        try {
            File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(storageDir.getPath(), imgName);
            if(file.delete()) {
                imgName = null;
            }
        }catch (Exception e) {
            Log.e(TAG, "Error deleting image file => " + e);
        }
    }

    public void requestPermission() {
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if(requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_FINE_LOCATION
            );
        }else {
            isGranted = true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission Granted!");
                isGranted = true;
            } else {
                Log.i(TAG, "Permission Denied!");
            }
        }
    }

    private void configureActionBar() {
        AppCompatActivity app = (AppCompatActivity) requireActivity();
        ActionBar ab = app.getSupportActionBar();
        ab.show();
        ab.setTitle(expenseId == Constants.NEW_ID ? R.string.expense_form_add : R.string.expense_form_edit);
        ab.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                ExpenseEntity expense = mViewModel.expenseLiveData.getValue();
                if(expenseId != Constants.NEW_ID && expense != null && expense.getImgName() != imgName) {
                    expense.setImgName(imgName);
                    expenseRepo.updateOne(expense);
                }
                Navigation.findNavController(requireView()).navigateUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean validate() {
        EditText cost = binding.etCost;
        TripEntity trip = expenseId == Constants.NEW_ID ? mViewModel.tripLiveData.getValue() : mViewModel.expenseLiveData.getValue().getTrip();

        boolean isValidated = true;
        String emptyError = "This field cannot be empty!";

        binding.menuExpenseCategories.setError(null);
        binding.tilDatePicker.setError(null);
        binding.tilCost.setError(null);

        if(category == null) {
            binding.menuExpenseCategories.setError(emptyError);
            isValidated = false;
        }
        if(date == null) {
            binding.tilDatePicker.setError(emptyError);
            isValidated = false;
        }else {
            long startDate = trip.getStartDate();
            long endDate = trip.getEndDate();
            String error = String.format("The start date is %s.\nThe end date is %s.", DatetimeUtil.getDate(startDate, null), DatetimeUtil.getDate(endDate, null));
            if(date < trip.getStartDate() || date > trip.getEndDate()) {
                binding.tilDatePicker.setError(error);
                isValidated = false;
            }
        }
        String costStr = cost.getText().toString();
        if(costStr.equals(Constants.EMPTY_STRING)) {
            binding.tilCost.setError(emptyError);
            isValidated = false;
        }else if(Double.parseDouble(costStr) < 0) {
            binding.tilCost.setError("Cost cannot less than 0.");
            isValidated = false;
        }

        return isValidated;
    }

    @SuppressLint("MissingPermission")
    private void handleSave() {
        double cost = Double.parseDouble(binding.etCost.getText().toString());
        String comment = binding.etComment.getText().toString();
        if(expenseId == Constants.NEW_ID) {
            TripEntity trip = mViewModel.tripLiveData.getValue();
            trip.setTotal(trip.getTotal() + cost);
            ExpenseEntity expense = new ExpenseEntity(date, cost, comment, null, null, imgName, category, trip);
            if (isGranted) {
                locationClient.getLastLocation().addOnSuccessListener(location -> {
                    Log.i(TAG, "Getting Last Known Location => " + location);
                    if (location != null) {
                        expense.setLatitude(location.getLatitude());
                        expense.setLongitude(location.getLongitude());
                    }
                    boolean isSuccess = expenseRepo.insertOne(expense);
                    String msg = isSuccess ? "Added expense successfully!" : "Added expense failed!";
                    Navigation.findNavController(requireView()).navigateUp();
                    makeToast(msg);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error determining location => " + e);
                });
            } else {
                boolean isSuccess = expenseRepo.insertOne(expense);
                String msg = isSuccess ? "Added expense successfully!" : "Added expense failed!";
                Navigation.findNavController(requireView()).navigateUp();
                makeToast(msg);
            }
        }else {
            ExpenseEntity oldExpense = mViewModel.expenseLiveData.getValue();
            TripEntity trip = oldExpense.getTrip();
            trip.setTotal(trip.getTotal() - oldExpense.getCost() + cost);
            ExpenseEntity expense = new ExpenseEntity(expenseId, date, cost, comment, oldExpense.getLatitude(), oldExpense.getLongitude(), imgName, category, trip);
            boolean isSuccess = expenseRepo.updateOne(expense);
            String msg = isSuccess ? "Updated expense successfully!" : "Updated expense failed!";
            Navigation.findNavController(requireView()).navigateUp();
            makeToast(msg);
        }
    }

    private void createDatePicker(EditText editText) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        editText.setShowSoftInputOnFocus(false);
        datePicker.addOnPositiveButtonClickListener(selection -> {
            editText.setText(datePicker.getHeaderText());
            date = selection;
        });

        editText.setOnFocusChangeListener((view, b) -> {
            if(b) datePicker.show(requireActivity().getSupportFragmentManager(), "DATE_PICKER");
        });

        editText.setOnClickListener(view -> {
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
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