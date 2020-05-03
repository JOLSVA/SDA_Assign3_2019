package com.example.sdaassign32019johndoe;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


/*
 * A simple {@link Fragment} subclass.
 * @author Chris Coughlan 2019
 */
public class ProductList extends Fragment {

    private static final String TAG = "RecyclerViewActivity";
    private ArrayList<FlavorAdapter> mFlavor = new ArrayList<>();

    public ProductList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_product_list, container, false);

        // Create an ArrayList of AndroidFlavor objects
        mFlavor.add(new FlavorAdapter("The Cool", "€20", R.drawable.the_cool));
        mFlavor.add(new FlavorAdapter("The Durable", "€10", R.drawable.the_durable));
        mFlavor.add(new FlavorAdapter("The Fit", "€15", R.drawable.the_fit));
        mFlavor.add(new FlavorAdapter("The Party", "€10", R.drawable.the_party));
        mFlavor.add(new FlavorAdapter("The Smart", "€20", R.drawable.the_smart));
        mFlavor.add(new FlavorAdapter("The Adventurous", "€10", R.drawable.the_adventurous));
        mFlavor.add(new FlavorAdapter("The Brave", "€25", R.drawable.the_brave));
        mFlavor.add(new FlavorAdapter("The Fashionable", "€30", R.drawable.the_fashionable));
        mFlavor.add(new FlavorAdapter("The Funny", "€15", R.drawable.the_funny));
        mFlavor.add(new FlavorAdapter("The Psychedelic", "€20", R.drawable.the_psychedelic));

        //start it with the view
        Log.d(TAG, "Starting recycler view");
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView_view);
        FlavorViewAdapter recyclerViewAdapter = new FlavorViewAdapter(getContext(), mFlavor);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return root;
    }
}
