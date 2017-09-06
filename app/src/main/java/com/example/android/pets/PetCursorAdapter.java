package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.android.pets.data.PetContract.PetEntry;
import static android.R.id.list;

/**
 * Created by Administrator on 7/12/2017.
 */

public class PetCursorAdapter extends CursorAdapter{
    public PetCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,viewGroup,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        View listView = view;

        int indexColumnName = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int indexColumnBreed = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);


        String name = cursor.getString(indexColumnName);
        String breed = cursor.getString(indexColumnBreed);



        TextView name_text_view = (TextView) listView.findViewById(R.id.name);

        name_text_view.setText(name);

        TextView breed_text_view = (TextView) listView.findViewById(R.id.summary);

        if (breed.isEmpty()) {
            breed = "Unknown breed";
        }
        breed_text_view.setText(breed);

    }
}
