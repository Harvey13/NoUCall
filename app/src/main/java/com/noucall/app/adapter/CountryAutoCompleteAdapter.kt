package com.noucall.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.noucall.app.R
import com.noucall.app.data.Country
import com.noucall.app.data.CountryData

class CountryAutoCompleteAdapter(context: Context) : ArrayAdapter<Country>(context, R.layout.item_country_dropdown) {
    
    private val allCountries = CountryData.getAllCountries(context)
    private var filteredCountries = allCountries
    
    override fun getCount(): Int = filteredCountries.size
    
    override fun getItem(position: Int): Country = filteredCountries[position]
    
    override fun getItemId(position: Int): Long = filteredCountries[position].name.hashCode().toLong()
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_country_dropdown, parent, false)
        val country = filteredCountries[position]
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = "${country.prefix} ${country.name}"
        
        return view
    }
    
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                
                if (constraint == null || constraint.isEmpty()) {
                    results.values = allCountries
                    results.count = allCountries.size
                } else {
                    val filtered = CountryData.searchCountries(context, constraint.toString())
                    results.values = filtered
                    results.count = filtered.size
                }
                
                return results
            }
            
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredCountries = results?.values as? List<Country> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}
