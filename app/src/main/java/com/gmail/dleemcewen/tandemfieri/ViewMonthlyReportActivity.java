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
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.MonthlyReportArrayAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.gmail.dleemcewen.tandemfieri.Filters.CriteriaRestaurant;
import com.gmail.dleemcewen.tandemfieri.Logging.LogWriter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.logging.Level;

public class ViewMonthlyReportActivity extends AppCompatActivity {

    private ListView restaurantListView;
    private Spinner monthSpinner;
    private ListView displayListView;
    private Button executeButton;
    private User currentUser;
    private ArrayAdapter<String> restaurantAdapter;
    private MonthlyReportArrayAdapter monthlyReportArrayAdapter;
    private ArrayList<Order> orderList;
    private ArrayList<String> restaurantNamesList;
    private ArrayList<String> selectedRestaurants;
    private ArrayList<DisplayItem> displayList;
    private String monthSelected = "";

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
        displayList = new ArrayList<>();
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
        //clear display list
        if(!displayList.isEmpty()){
            displayList.clear();
        }
        //get the selected restaurants
        selectedRestaurants = getSelectedRestaurants();

        //create list of DisplayItem objects
        for(String name : selectedRestaurants){
            DisplayItem newItem = new DisplayItem(name);
            displayList.add(newItem);
        }

        //filter orders by selected restaurants
        ArrayList<Order> ordersSelected = new ArrayList<>();
        for(String s : selectedRestaurants){
            CriteriaRestaurant cr = new CriteriaRestaurant(s);
            ordersSelected.addAll((ArrayList<Order>) cr.meetCriteria(orderList));
        }

        //filter orders by month
       // ordersSelected = filterMonth(ordersSelected);

        //accumulate order totals
        for(DisplayItem current : displayList){
            for(Order o : ordersSelected){
                if(current.getName().equals(o.getRestaurantName())){
                    current.setTotal(current.getTotal() + o.getTotal());
                }
            }
        }

        //display view
        displayResults();
       /* LogWriter.log(getApplicationContext(), Level.INFO, "Month selected: " + monthSelected);
        for(String s : selectedRestaurants) {
            LogWriter.log(getApplicationContext(), Level.INFO, s);
        }*/
    }

    private void displayResults(){
        //set adapter
        for(DisplayItem d : displayList) {
            LogWriter.log(getApplicationContext(), Level.INFO, "Order: " + d.getName() + " " + d.getTotal());
        }

        monthlyReportArrayAdapter = new MonthlyReportArrayAdapter(getApplicationContext(), displayList, monthSelected);
        displayListView.setAdapter(monthlyReportArrayAdapter);

        if(displayList.isEmpty()){
            Toast.makeText(getApplicationContext(), "You have no orders to display.", Toast.LENGTH_LONG).show();
            displayListView.setVisibility(View.INVISIBLE);
            restaurantListView.setVisibility(View.VISIBLE);
        }

        //prepare view
        restaurantListView.setVisibility(View.INVISIBLE);
        displayListView.setVisibility(View.VISIBLE);
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
