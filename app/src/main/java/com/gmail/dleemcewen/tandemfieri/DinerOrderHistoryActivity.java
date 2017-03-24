package com.gmail.dleemcewen.tandemfieri;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.dleemcewen.tandemfieri.Adapters.DinerOrderHistoryArrayAdapter;
import com.gmail.dleemcewen.tandemfieri.Entities.Order;
import com.gmail.dleemcewen.tandemfieri.Entities.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DinerOrderHistoryActivity extends AppCompatActivity {
    private User user;
    private ArrayList<Order> ordersList;
    private DatabaseReference mDatabase;
    private ListView orderHistoryView;
    private DinerOrderHistoryArrayAdapter dinerOrderHistoryArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diner_order_history);

        orderHistoryView = (ListView)findViewById(R.id.diner_order_history_listview);

        initialize();
        retrieveData();

    }//end on create

    private void initialize(){
        Bundle bundle = this.getIntent().getExtras();
        user = (User) bundle.getSerializable("User");
        ordersList = new ArrayList<>();
        orderHistoryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openOrder((Order) parent.getItemAtPosition(position), position);
            }
        });
    }

    private void openOrder(Order order, int position){
        Bundle orderBundle = new Bundle();
        Intent intent = new Intent(DinerOrderHistoryActivity.this, ViewOrderActivity.class);
        orderBundle.putSerializable("Order", order);
        intent.putExtras(orderBundle);
        startActivity(intent);
    }

    private void retrieveData(){

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Order");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //everything to do with order list code here
                for(DataSnapshot owners: dataSnapshot.getChildren()){
                    //LogWriter.log(getApplicationContext(), Level.INFO, "owner #: " + owners.getKey());
                    for(DataSnapshot orders : owners.getChildren()){
                        //LogWriter.log(getApplicationContext(), Level.INFO, "order #: " + orders.getKey());
                        Order o = orders.getValue(Order.class);
                       //LogWriter.log(getApplicationContext(), Level.INFO, "customer #: " + o.getCustomerId());
                        if(o.getCustomerId() != null){
                            if(o.getCustomerId().equals(user.getAuthUserID())){
                                ordersList.add(o);
                            }
                        }
                    }

                }
                if(ordersList.isEmpty()){
                    Toast.makeText(getApplicationContext(), "You have no orders to display.", Toast.LENGTH_LONG).show();
                }else {

                    dinerOrderHistoryArrayAdapter = new DinerOrderHistoryArrayAdapter(getApplicationContext(), ordersList);
                    orderHistoryView.setAdapter(dinerOrderHistoryArrayAdapter);
                    }

            }//end on data change

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });//end listener
    }//end retrieve data
}//end activity
