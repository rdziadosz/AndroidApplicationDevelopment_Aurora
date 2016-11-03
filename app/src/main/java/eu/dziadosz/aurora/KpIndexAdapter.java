package eu.dziadosz.aurora;

import android.widget.ArrayAdapter;

/**
 * Created by Radosław on 03.11.2016.
 */

/*public class KpIndexAdapter  extends ArrayAdapter<KpIndex> {

    private static class ViewHolder {
        private TextView itemView;
    }

    public MyClassAdapter(Context context, int textViewResourceId, ArrayList<MyClass> items) {
        super(context, textViewResourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.listview_association, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.ItemView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MyClass item = getItem(position);
        if (item!= null) {
            // My layout has only one TextView
            // do whatever you want with your string and long
            viewHolder.itemView.setText(String.format("%s %d", item.reason, item.long_val));
        }

        return convertView;
    }
}
*/