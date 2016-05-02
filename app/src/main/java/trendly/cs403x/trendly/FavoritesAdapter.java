package trendly.cs403x.trendly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Anthony J. Ruffa on 5/2/2016.
 */
public class FavoritesAdapter extends BaseAdapter {
    private String[] result;
    private Context context;
    private static LayoutInflater inflater = null;

    public FavoritesAdapter(Context context, String[] itemList) {
        this.result = itemList;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        return result[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.favorite_item, null);
        Button addToCartButton = (Button) view.findViewById(R.id.add_favorite_to_cart_button);
        TextView itemText = (TextView) view.findViewById(R.id.favorite_item_name);

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add the item to the cart.
            }
        });

        itemText.setText(result[position]);


        return view;
    }
}
