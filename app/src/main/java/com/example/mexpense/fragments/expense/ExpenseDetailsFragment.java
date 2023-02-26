package com.example.mexpense.fragments.expense;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mexpense.R;
import com.example.mexpense.databinding.FragmentExpenseDetailsBinding;
import com.example.mexpense.entities.ExpenseEntity;
import com.example.mexpense.repositories.ExpenseRepository;
import com.example.mexpense.utilities.AlertDialog;
import com.example.mexpense.utilities.DatetimeUtil;
import com.example.mexpense.utilities.Utilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

public class ExpenseDetailsFragment extends Fragment {

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    };

    private FragmentExpenseDetailsBinding binding;
    private ExpenseDetailsViewModel mViewModel;
    private final ExpenseRepository repo = new ExpenseRepository();
    private ExpenseEntity expense;
    private final String TAG = "ExpenseDetailsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(ExpenseDetailsViewModel.class);
        binding = FragmentExpenseDetailsBinding.inflate(inflater, container, false);
        long expenseId = requireArguments().getLong("expenseId");

        configureActionBar();

        mViewModel.expenseLiveData.setValue(repo.getOneById(expenseId));

        mViewModel.expenseLiveData.observe(getViewLifecycleOwner(), expense -> {
            this.expense = expense;
            binding.imgCategory.setImageResource(expense.getCategory().getIconId());
            binding.tvCategoryName.setText(Utilities.capitalizeWords(expense.getCategory().getName()));
            binding.tvDate.setText(DatetimeUtil.getDate(expense.getDate(), null));
            binding.tvCost.setText(String.format("%s", expense.getCost()));
            if(expense.getComments().compareTo("") != 0) {
                binding.tvComments.setText(expense.getComments());
            }
            if(expense.getImgName() == null) {
                binding.imgExpense.setImageResource(R.drawable.ic_baseline_image_24);
            }else {
                Bitmap bitmap = getBitmap();
                binding.imgExpense.setImageBitmap(bitmap);
                binding.tvImgName.setText(expense.getImgName());
                binding.tvImgName.setVisibility(View.VISIBLE);
            }
            if(expense.getLatitude() == null) return;
            binding.llLocation.setVisibility(View.GONE);
            binding.map.setVisibility(View.VISIBLE);
            ((SupportMapFragment) binding.map.getFragment()).getMapAsync(googleMap -> {
                LatLng pos = new LatLng(expense.getLatitude(), expense.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(pos).title(""));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17f));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(true);
            });
        });

        return binding.getRoot();
    }

    private void configureActionBar() {
        AppCompatActivity app = (AppCompatActivity) requireActivity();
        ActionBar ab = app.getSupportActionBar();
        ab.setTitle(R.string.expense_details);
        ab.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
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
                if(expense != null) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("expenseId", expense.getId());
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.expenseFormFragment, bundle);
                }
                return true;
            case R.id.action_delete:
                if(expense != null) {
                    displayAlertDialog("Delete Confirmation", AlertDialog.DANGER, "The expense will be deleted. Are you sure?", (var1, var2) -> handleDelete());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleDelete() {
        expense.getTrip().setTotal(expense.getTrip().getTotal() - expense.getCost());
        boolean isSuccess = repo.deleteOneById(expense);
        Navigation.findNavController(requireView()).navigateUp();
        if(isSuccess) {
            makeToast("Deleted expense successfully!");
        }else {
            makeToast("Deleted expense failed!");
        }
    }

    private Bitmap getBitmap() {
        try {
            File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(storageDir.getPath(), expense.getImgName());
            return BitmapFactory.decodeFile(file.getPath());
        }catch (Exception e) {
            Log.e(TAG, "Error getting bitmap from image file => " + e);
            return null;
        }
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