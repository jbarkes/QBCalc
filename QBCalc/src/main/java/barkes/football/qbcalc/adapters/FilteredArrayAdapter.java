/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.football.qbcalc.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;

/**
 * Created by Jason Barkes on 12/2/2013.
 */
public class FilteredArrayAdapter extends ArrayAdapter<String> {
    private Context _context;
    private ArrayList<String> _allValues;
    private ArrayList<String> _values;
    private FILTER_TYPE _filterType = FILTER_TYPE.CONTAINS;

    public FilteredArrayAdapter(Context context, int resource, String[] values) {
        super(context, resource, values);
        _context = context;
        _values = new ArrayList<String>();
        _allValues = new ArrayList<String>();

        for (int i = 0; i < values.length; i++) {
            _values.add(values[i]);
        }

        _allValues.addAll(_values);
    }

    public static enum FILTER_TYPE {
        STARTS_WITH, CONTAINS
    }

    public FILTER_TYPE getFilterType() {
        return _filterType;
    }

    public void set_filterType(FILTER_TYPE filterType) {
        this._filterType = filterType;
    }

    @Override
    public int getCount() {
        return _values.size();
    }

    @Override
    public String getItem(int position) {
        return _values.get(position);
    }

    @Override
    public Filter getFilter() {
        return valueFilter;
    }

    Filter valueFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<String> matches = new ArrayList<String>();
            FilterResults filterResults = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                /* No filter - return the complete list */
                filterResults.values = _allValues;
                filterResults.count = _allValues.size();
            } else {
                for (String s : _allValues) { // TODO: Add Shared Pref for filter type.
                    switch (_filterType)
                    {
                        case STARTS_WITH:
                            if (s.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                                matches.add(s);
                            }
                            break;
                        case CONTAINS:
                            if (s.toLowerCase().contains(constraint.toString().toLowerCase())) {
                                matches.add(s);
                            }
                            break;
                    }
                }

                filterResults = new FilterResults();
                filterResults.values = matches.toArray(new String[0]);
                filterResults.count = matches.size();
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            /** Inform the adapter about the data changes */
            if (results.count == 0 || constraint == null || constraint.length() == 0) {
                notifyDataSetInvalidated();
            }
            else {
                _values.clear();
                for (int i = 0; i < results.count; i++) {
                    _values.add(((String[])results.values)[i]);
                }

                notifyDataSetChanged();
            }
        }
    };

}
