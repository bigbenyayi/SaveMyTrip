package com.openclassrooms.savemytrip.todolist;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.openclassrooms.savemytrip.R;
import com.openclassrooms.savemytrip.base.BaseActivity;
import com.openclassrooms.savemytrip.injection.Injection;
import com.openclassrooms.savemytrip.injection.ViewModelFactory;
import com.openclassrooms.savemytrip.models.Item;
import com.openclassrooms.savemytrip.models.User;
import com.openclassrooms.savemytrip.utils.ItemClickSupport;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class TodoListActivity extends BaseActivity implements ItemAdapter.Listener {

    // FOR DESIGN
    @BindView(R.id.todo_list_activity_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.todo_list_activity_spinner)
    Spinner spinner;
    @BindView(R.id.todo_list_activity_edit_text)
    EditText editText;
    @BindView(R.id.todo_list_activity_header_profile_image)
    ImageView profileImage;
    @BindView(R.id.todo_list_activity_header_profile_text)
    TextView profileText;
    @BindView(R.id.imageSelection)
    ImageView imageSelection;

    Uri selectedPictureUri;

    // FOR DATA
    private ItemViewModel itemViewModel;
    private ItemAdapter adapter;
    private static int USER_ID = 1;

    @Override
    public int getLayoutContentViewID() {
        return R.layout.activity_todo_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.configureToolbar();
        this.configureSpinner();
        this.configueImageSelection();

        this.configureRecyclerView();
        this.configureViewModel();

        this.getCurrentUser(USER_ID);
        this.getItems(USER_ID);
    }

    private void configueImageSelection() {
        imageSelection.setOnClickListener(v -> {

            //Get data from phone and select a PHOTO
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri chosenImageUri = data.getData();

            Bitmap mBitmap = null;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //   imageSelection.setImageBitmap(mBitmap);
            imageSelection.setImageURI(chosenImageUri);
            selectedPictureUri = chosenImageUri;
        }

    }

    // -------------------
    // ACTIONS
    // -------------------

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.todo_list_activity_button_add)
    public void onClickAddButton() {
        this.createItem();
    }

    @Override
    public void onClickDeleteButton(int position) {
        this.deleteItem(this.adapter.getItem(position));
    }

    // -------------------
    // DATA
    // -------------------

    private void configureViewModel() {
        ViewModelFactory mViewModelFactory = Injection.provideViewModelFactory(this);
        this.itemViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ItemViewModel.class);
        this.itemViewModel.init(USER_ID);
    }

    // ---

    private void getCurrentUser(int userId) {
        this.itemViewModel.getUser(userId).observe(this, this::updateHeader);
    }

    // ---

    private void getItems(int userId) {
        this.itemViewModel.getItems(userId).observe(this, this::updateItemsList);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createItem() {
        Item item;
        if (selectedPictureUri == null) {
            item = new Item(this.editText.getText().toString(), this.spinner.getSelectedItemPosition(), USER_ID, null);
        } else {
            item = new Item(this.editText.getText().toString(), this.spinner.getSelectedItemPosition(), USER_ID, this.selectedPictureUri.toString());
        }
        this.editText.setText("");
        imageSelection.setImageDrawable(getDrawable(R.drawable.ic_image_black_24dp));
        selectedPictureUri = Uri.parse("");
        this.itemViewModel.createItem(item);
    }

    private void deleteItem(Item item) {
        this.itemViewModel.deleteItem(item.getId());
    }

    private void updateItem(Item item) {
        item.setSelected(!item.getSelected());
        this.itemViewModel.updateItem(item);
    }

    // -------------------
    // UI
    // -------------------

    private void configureSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void configureRecyclerView() {
        this.adapter = new ItemAdapter(this);
        this.recyclerView.setAdapter(this.adapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemClickSupport.addTo(recyclerView, R.layout.activity_todo_list_item)
                .setOnItemClickListener((recyclerView1, position, v) -> this.updateItem(this.adapter.getItem(position)));
    }

    private void updateHeader(User user) {
        this.profileText.setText(user.getUsername());
        Glide.with(this).load(user.getUrlPicture()).apply(RequestOptions.circleCropTransform()).into(this.profileImage);
    }

    private void updateItemsList(List<Item> items) {
        this.adapter.updateData(items);
    }

}
