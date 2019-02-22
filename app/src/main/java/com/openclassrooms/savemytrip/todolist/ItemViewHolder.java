package com.openclassrooms.savemytrip.todolist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.openclassrooms.savemytrip.R;
import com.openclassrooms.savemytrip.models.Item;
import com.openclassrooms.savemytrip.utils.StorageUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.provider.Telephony.Mms.Part.FILENAME;
import static com.openclassrooms.savemytrip.provider.ItemContentProvider.AUTHORITY;
import static com.openclassrooms.savemytrip.tripbook.TripBookActivity.FOLDERNAME;

/**
 * Created by Philippe on 28/02/2018.
 */

public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    @BindView(R.id.activity_todo_list_item_text) TextView textView;
    @BindView(R.id.activity_todo_list_item_image) ImageView imageView;
    @BindView(R.id.activity_todo_list_item_remove) ImageButton imageButton;
    @BindView(R.id.activity_todo_list_selected_image) ImageView selectedImage;
    @BindView(R.id.activity_todo_list_share_button) ImageView shareButton;

    Context mContext;

    // FOR DATA
    private WeakReference<ItemAdapter.Listener> callbackWeakRef;

    public ItemViewHolder(View itemView, Context context) {
        super(itemView);
        this.mContext = context;
        ButterKnife.bind(this, itemView);
    }

    public void updateWithItem(Item item, ItemAdapter.Listener callback){
        this.callbackWeakRef = new WeakReference<>(callback);
        this.textView.setText(item.getText());
        this.imageButton.setOnClickListener(this);
        switch (item.getCategory()){
            case 0: // TO VISIT
                this.imageView.setBackgroundResource(R.drawable.ic_room_black_24px);
                break;
            case 1: // IDEAS
                this.imageView.setBackgroundResource(R.drawable.ic_lightbulb_outline_black_24px);
                break;
            case 2: // RESTAURANTS
                this.imageView.setBackgroundResource(R.drawable.ic_local_cafe_black_24px);
                break;
        }
        if (item.getSelected()){
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (item.getImage() == null){
            selectedImage.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            selectedImage.setImageURI(Uri.parse(item.getImage()));
        }
        shareButton.setOnClickListener(v -> {

/*
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, item.getImage());
            mContext.startActivity(Intent.createChooser(sharingIntent,"Sharing picture"));
*/
            Uri imageUri = Uri.parse("android.resource://" + mContext.getPackageName()
                    + "/drawable/" + "ic_image_black_24dp");
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mContext.startActivity(Intent.createChooser(shareIntent, "send"));

        });

    }

    @Override
    public void onClick(View view) {
        ItemAdapter.Listener callback = callbackWeakRef.get();
        if (callback != null) callback.onClickDeleteButton(getAdapterPosition());
    }
}
