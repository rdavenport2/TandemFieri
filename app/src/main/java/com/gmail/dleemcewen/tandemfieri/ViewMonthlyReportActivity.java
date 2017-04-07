package com.gmail.dleemcewen.tandemfieri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.logging.Level;

public class ViewMonthlyReportActivity extends AppCompatActivity {

    ListView restaurantListView;
    Spinner monthSpinner;
    ListView displayListView;
    Button executeButton;
    User currentUser;
    ArrayAdapter<String> restaurantAdapter;
    ArrayList<Order> orderList;
    ArrayList<String> restaurantNamesList;
    ArrayList<String> selectedRestaurants;
    String monthSelected = "";

    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_monthly_report);

        findViewsById();
        initialize();
        retrieveData();
    }//end on Create

    private void findViewsById(){
        restaurantListView = (ListView)findViewById(R.id.restaurant_name_spinner);
        monthSpinner = (Spinner) findViewById(R.id.month_spinner);
        displayListView = (ListView)findViewById(R.id.display_sales_report);
        executeButton = (Button)findViewById(R.id.go_button);
    }

    private void initialize() {
        Bundle bundle = this.getIntent().getExtras();
        currentUser = (User)bundle.getSerializable("User");
        orderList = new ArrayList<>();
        restaurantNamesList = new ArrayList<>();
        selectedRestaurants = new ArrayList<>();
        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeSearch();
            }
        });

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.month, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(typeAdapter);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected.
                monthSelected = parent.getItemAtPosition(pos).toString();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void retrieveData(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Order").child(currentUser.getAuthUserID());

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get list of orders for this owner
                for(DataSnapshot orders : dataSnapshot.getChildren()){
                    Order o = orders.getValue(Order.class);
                    orderList.add(o);
                }

                //populate spinner adapter
                for(Order order: orderList){
                    if(!restaurantNamesList.contains(order.getRestaurantName())) {
                        restaurantNamesList.add(order.getRestaurantName());
                    }
                }
               restaurantAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_multiple_choice, restaurantNamesList);
                restaurantListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                restaurantListView.setAdapter(restaurantAdapter);
                restaurantListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void executeSearch(){
        selectedRestaurants = getSelectedRestaurants();
        LogWriter.log(getApplicationContext(), Level.INFO, "Month selected: " + monthSelected);
        for(String s : selectedRestaurants) {
            LogWriter.log(getApplicationContext(), Level.INFO, s);
        }
    }

    private ArrayList<String> getSelectedRestaurants(){
        SparseBooleanArray checked = restaurantListView.getCheckedItemPositions();
        ArrayList<String> results = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i))
                results.add(restaurantAdapter.getItem(position));
        }

        return results;
    }
}//end activity
